"""Запустить изолированный JMH; фоновый smoke не является сравнением скорости."""
import argparse
import datetime
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import time

p = argparse.ArgumentParser(description=__doc__)
p.add_argument('label')
p.add_argument('--java', type=Path, required=True)
p.add_argument('--functional-only', action='store_true')
p.add_argument('--containers')
p.add_argument('--scenarios')
a = p.parse_args()
if not re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9_.-]*', a.label): p.error('Нужна простая новая метка')
base = Path(__file__).resolve().parent
repo = base.parent.parent
out = repo / 'benchmarks/results' / (datetime.datetime.now(datetime.timezone.utc).date().isoformat() + '-micro') / a.label
if out.exists(): p.error('Прежние результаты не перезаписываются: ' + str(out))
classpath = (base / 'build/runtime-classpath.txt').read_text().strip()
runtime = Path((base / 'build/runtime-location.txt').read_text().strip())
index = json.loads((runtime / 'runtime-index.json').read_text())
containers = a.containers or ('64' if a.functional_only else '64,4096,16384')
scenarios = a.scenarios or ('FUTURE,MIXED' if a.functional_only else 'FUTURE,MIXED,DISABLED,DUE_BUDGET0')
for item in containers.split(','):
    if not item.isdigit() or not 1 <= int(item) <= 16384: p.error('Некорректное число контейнеров')
if not set(scenarios.split(',')) <= {'FUTURE', 'MIXED', 'DISABLED', 'DUE_BUDGET0'}: p.error('Неизвестный сценарий')
out.mkdir(parents=True)
command = [str(a.java.resolve()), '-Xms1G', '-Xmx1G', '-Dstdout.encoding=UTF-8', '-Dstderr.encoding=UTF-8',
           '-cp', classpath, 'org.openjdk.jmh.Main', 'TickSelectionBenchmark', '-t', '1',
           '-p', 'containers=' + containers, '-p', 'scenario=' + scenarios, '-prof', 'gc',
           '-rf', 'json', '-rff', str(out / 'jmh.json')]
if a.functional_only:
    command += ['-f', '0', '-wi', '1', '-i', '1', '-w', '100ms', '-r', '100ms']
metadata = {'command': command, 'functional_only': a.functional_only,
            'bundle_sha256': index['bundle_sha256'], 'started_utc': time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime()),
            'benchmark_source_sha256': hashlib.sha256((base / 'src/main/java/org/watermc/micro/TickSelectionBenchmark.java').read_bytes()).hexdigest(),
            'reference_source_sha256': hashlib.sha256((repo / 'watermc-server/src/test/java/org/watermc/ticks/reference/ReferenceLevelTicks.java').read_bytes()).hexdigest()}
(out / 'run-metadata.json').write_text(json.dumps(metadata, indent=2) + '\n')
env = os.environ.copy()
if os.name == 'nt' and Path('D:/water-mc/tmp').is_dir(): env.update(TMP='D:/water-mc/tmp', TEMP='D:/water-mc/tmp')
flags = subprocess.CREATE_NO_WINDOW if os.name == 'nt' else 0
if a.functional_only:
    if os.name == 'nt': flags |= subprocess.BELOW_NORMAL_PRIORITY_CLASS
    else: os.nice(5)
with open(out / 'jmh.log', 'w', encoding='utf-8') as log:
    result = subprocess.run(command, cwd=base, env=env, stdout=log, stderr=log, creationflags=flags, timeout=7200)
status = {'passed': False, 'functional_only': a.functional_only, 'exit_code': result.returncode}
if result.returncode == 0 and (out / 'jmh.json').is_file():
    rows = json.loads((out / 'jmh.json').read_text())
    expected = 2 * len(containers.split(',')) * len(scenarios.split(','))
    status['results'] = len(rows)
    status['passed'] = len(rows) == expected and all('primaryMetric' in row for row in rows)
(out / 'status.json').write_text(json.dumps(status, indent=2) + '\n')
print(json.dumps({'output': str(out), **status}), flush=True)
raise SystemExit(0 if status['passed'] else 1)
