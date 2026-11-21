"""Сравнение тёплого запуска двух ядер: время до Done, память процесса и heap после запуска.

Для каждого ядра создаётся своя папка из шаблона (eula, server.properties без плагинов). Первый запуск
прогревочный: распаковка библиотек, создание мира. Затем ядра перезапускаются поочерёдно N раз.

Пример:
  python benchmarks/tools/startup_compare.py --core water=watermc.jar --core leaf=leaf.jar --runs 3
"""
import argparse
import json
import shutil
import subprocess
import time
from pathlib import Path

JAVA = r'C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe'
JCMD = r'C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\jcmd.exe'


def process_memory(pid: int) -> dict:
    out = subprocess.run(['powershell', '-NoProfile', '-Command',
                          f'$p = Get-Process -Id {pid}; "$($p.WorkingSet64) $($p.PrivateMemorySize64)"'],
                         capture_output=True, text=True).stdout.split()
    return {'working_set_mib': int(out[0]) / 2**20, 'private_mib': int(out[1]) / 2**20} if len(out) == 2 else {}


def heap_used(pid: int) -> float | None:
    out = subprocess.run([JCMD, str(pid), 'GC.heap_info'], capture_output=True, text=True, errors='replace').stdout
    for line in out.splitlines():
        line = line.strip()
        if line.startswith('garbage-first heap'):
            # garbage-first heap   total 4194304K, used 512000K [...]
            used = line.split('used')[1].split('K')[0].strip()
            return int(used) / 1024
    return None


def start(jar: Path, directory: Path, heap: str, extra: list[str], gc_before_heap: bool) -> dict:
    begin = time.time()
    proc = subprocess.Popen([JAVA, f'-Xms{heap}', f'-Xmx{heap}', *extra, '-jar', str(jar), '--nogui'], cwd=directory,
                            stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                            text=True, encoding='cp1251', errors='replace')
    result = {}
    log = []
    for line in proc.stdout:
        log.append(line.rstrip())
        if 'Done (' in line and 'done_s' not in result:
            result['done_s'] = round(time.time() - begin, 2)
            try:
                result['reported_s'] = float(line.split('Done (')[1].split('s)')[0].replace(',', '.'))
            except (IndexError, ValueError):
                pass
            time.sleep(5)
            result.update(process_memory(proc.pid))
            if gc_before_heap:
                subprocess.run([JCMD, str(proc.pid), 'GC.run'], capture_output=True)
            result['heap_used_mib'] = heap_used(proc.pid)
            proc.stdin.write('stop\n')
            proc.stdin.flush()
        if time.time() - begin > 900:
            proc.kill()
            result['timeout'] = True
            break
    proc.wait(timeout=180)
    (directory / 'startup-console.log').write_text('\n'.join(log), encoding='utf-8')
    return result


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--core', action='append', required=True, help='name=path/to.jar')
    parser.add_argument('--runs', type=int, default=3)
    parser.add_argument('--heap', default='2G')
    parser.add_argument('--root', default=r'D:\water-mc\run\startup')
    parser.add_argument('--template', default=r'D:\water-mc\run\base')
    parser.add_argument('--out', default=None)
    args = parser.parse_args()

    cores = [(c.split('=', 1)[0], Path(c.split('=', 1)[1]).resolve()) for c in args.core]
    root = Path(args.root)
    dirs = {}
    for name, jar in cores:
        d = root / name
        if d.exists():
            shutil.rmtree(d)
        d.mkdir(parents=True)
        for f in ('eula.txt', 'server.properties'):
            shutil.copy2(Path(args.template) / f, d / f)
        dirs[name] = d
        warm = start(jar, d, args.heap, [], False)
        print(json.dumps({'core': name, 'warmup': warm}), flush=True)

    results = {name: [] for name, _ in cores}
    for i in range(args.runs):
        for name, jar in (cores if i % 2 == 0 else list(reversed(cores))):
            r = start(jar, dirs[name], args.heap, [], True)
            results[name].append(r)
            print(json.dumps({'core': name, 'run': i + 1, **r}), flush=True)

    summary = {}
    for name, runs in results.items():
        ok = [r for r in runs if 'done_s' in r]
        def avg(key):
            vals = [r[key] for r in ok if r.get(key) is not None]
            return round(sum(vals) / len(vals), 2) if vals else None
        summary[name] = {k: avg(k) for k in ('done_s', 'reported_s', 'working_set_mib', 'private_mib', 'heap_used_mib')}
        summary[name]['runs'] = len(ok)
    print(json.dumps({'summary': summary}, ensure_ascii=False, indent=1))
    if args.out:
        Path(args.out).write_text(json.dumps({'runs': results, 'summary': summary}, indent=1), encoding='utf-8')


if __name__ == '__main__':
    main()
