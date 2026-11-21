"""Горячие точки из JFR-записи: CPU (собственное/полное время) и аллокации.

python benchmarks/tools/jfr_hotspots.py <recording.jfr> [--jfr <path-to-jfr-tool>] [--top 40] [--thread "Server thread"]

Использует штатную утилиту JDK `jfr print --json`. Результат — текстовый отчёт в stdout.
Сэмплы JFR показывают распределение времени внутри одного прогона; для сравнения ядер
нужны отдельные контролируемые прогоны.
"""
import argparse
import collections
import json
import os
import subprocess
import sys


def frames_of(event):
    stack = (event.get('values') or {}).get('stackTrace') or {}
    out = []
    for frame in stack.get('frames') or []:
        method = frame.get('method') or {}
        type_name = (method.get('type') or {}).get('name', '?')
        out.append(f"{type_name}.{method.get('name', '?')}")
    return out


def thread_of(event):
    values = event.get('values') or {}
    thread = values.get('sampledThread') or values.get('eventThread') or {}
    return thread.get('javaName') or thread.get('osName') or '?'


def load(jfr_tool, recording, events):
    raw = subprocess.run([jfr_tool, 'print', '--json', '--events', events, recording],
                         capture_output=True, check=True).stdout
    return json.loads(raw.decode('utf-8', errors='replace'))['recording']['events']


def pct(n, total):
    return f'{100.0 * n / total:5.1f}%' if total else '  n/a'


def report_cpu(events, title, thread_filter, top):
    samples = [e for e in events if thread_filter(thread_of(e))]
    total = len(samples)
    self_counts = collections.Counter()
    total_counts = collections.Counter()
    for e in samples:
        frames = frames_of(e)
        if not frames:
            continue
        self_counts[frames[0]] += 1
        for method in set(frames):
            total_counts[method] += 1
    print(f'\n=== CPU: {title} — {total} samples ===')
    print('-- self (method itself is on top of the stack)')
    for method, n in self_counts.most_common(top):
        print(f'{pct(n, total)}  {n:6d}  {method}')
    print('-- total (method anywhere on the stack), excluding thread entry frames')
    shown = 0
    for method, n in total_counts.most_common(top * 3):
        if n == total or method.startswith(('java.lang.Thread.', 'java.util.concurrent.')):
            continue
        print(f'{pct(n, total)}  {n:6d}  {method}')
        shown += 1
        if shown >= top:
            break


def report_threads(events, top):
    counts = collections.Counter(thread_of(e) for e in events)
    total = sum(counts.values())
    print(f'\n=== CPU samples by thread — {total} samples ===')
    for name, n in counts.most_common(top):
        print(f'{pct(n, total)}  {n:6d}  {name}')


def report_alloc(events, top):
    by_class = collections.Counter()
    by_site = collections.Counter()
    total = 0
    for e in events:
        values = e.get('values') or {}
        weight = values.get('weight') or 0
        if isinstance(weight, str):
            weight = int(''.join(ch for ch in weight if ch.isdigit()) or 0)
        cls = (values.get('objectClass') or {}).get('name', '?')
        frames = frames_of(e)
        site = next((f for f in frames if not f.startswith(('java.', 'jdk.', 'sun.', 'it.unimi.'))), frames[0] if frames else '?')
        by_class[cls] += weight
        by_site[site] += weight
        total += weight
    print(f'\n=== Allocations (sampled weight) — {total / 1e9:.2f} GB estimated ===')
    print('-- by object type')
    for cls, w in by_class.most_common(top):
        print(f'{pct(w, total)}  {w / 1e6:10.1f} MB  {cls}')
    print('-- by first non-JDK allocation site')
    for site, w in by_site.most_common(top):
        print(f'{pct(w, total)}  {w / 1e6:10.1f} MB  {site}')


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('recording')
    parser.add_argument('--jfr', default=os.environ.get('JFR_TOOL', 'jfr'))
    parser.add_argument('--top', type=int, default=40)
    parser.add_argument('--thread', default='Server thread')
    args = parser.parse_args()
    sys.stdout.reconfigure(encoding='utf-8')

    cpu = load(args.jfr, args.recording, 'jdk.ExecutionSample')
    report_threads(cpu, 25)
    report_cpu(cpu, f'thread "{args.thread}"', lambda t: t == args.thread, args.top)
    report_cpu(cpu, 'all threads', lambda t: True, args.top)
    alloc = load(args.jfr, args.recording, 'jdk.ObjectAllocationSample')
    report_alloc(alloc, args.top)


if __name__ == '__main__':
    main()
