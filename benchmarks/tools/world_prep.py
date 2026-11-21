"""Время подготовки нового мира (обычный мир, фиксированный сид) при тёплых библиотеках.

Для каждого ядра своя папка; перед каждым замером удаляются только папки миров. Ядра чередуются по раундам
(A B A B ...), чтобы фоновая нагрузка машины делилась поровну. Первый запуск каждого ядра — прогрев
(распаковка библиотек, кэш ремапера) и в результат не входит.

  python benchmarks/tools/world_prep.py --core before=a.jar --core after=b.jar --rounds 3
"""
import argparse
import json
import re
import shutil
import statistics
import subprocess
import time
from pathlib import Path

JAVA = r'C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe'
WORLDS = ('world', 'world_nether', 'world_the_end', 'worlds')  # WaterMc keeps its worlds under worlds/
PROPS = """eula=true
level-seed={seed}
level-type=minecraft\\:normal
online-mode=false
server-port={port}
view-distance=10
spawn-protection=0
"""
DONE_LEVEL = re.compile(r'Done preparing level "[^"]+" \(([\d.,]+)s\)')
DONE = re.compile(r'Done \(([\d.,]+)s\)')


def run_once(jar: Path, directory: Path, timeout: int, jvm_args: list[str] = ()) -> dict:
    for w in WORLDS:
        shutil.rmtree(directory / w, ignore_errors=True)
    begin = time.time()
    proc = subprocess.Popen([JAVA, '-Xms2G', '-Xmx2G', '-Dwatermc.console.language=en', *jvm_args, '-jar', str(jar), '--nogui'],
                            cwd=directory, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                            text=True, encoding='utf-8', errors='replace')
    result = {'spawn_select_s': None, 'level_s': None, 'done_s': None, 'wall_s': None, 'workers': None}
    select_start = None
    for line in proc.stdout:
        t = time.time() - begin
        if "Selecting spawn point for world 'minecraft:overworld'" in line:
            select_start = t
        elif select_start is not None and result['spawn_select_s'] is None and 'Selecting spawn point' in line:
            result['spawn_select_s'] = round(t - select_start, 2)
        if 'chunk worker threads' in line or 'worker threads,' in line:
            result['workers'] = (result['workers'] or '') + line.strip().split(']: ', 1)[-1] + ' | '
        m = DONE_LEVEL.search(line)
        if m:
            result['level_s'] = float(m.group(1).replace(',', '.'))
        m = DONE.search(line)
        if m and result['done_s'] is None:
            result['done_s'] = float(m.group(1).replace(',', '.'))
            result['wall_s'] = round(t, 2)
            proc.stdin.write('stop\n')
            proc.stdin.flush()
        if t > timeout:
            proc.kill()
            break
    proc.wait(timeout=180)
    return result


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--core', action='append', required=True, help='name=path/to.jar[,-Dkey=value...]')
    p.add_argument('--rounds', type=int, default=3)
    p.add_argument('--seed', default='1234567890')
    p.add_argument('--root', type=Path, default=Path(r'D:\water-mc\run\worldprep'))
    p.add_argument('--timeout', type=int, default=900)
    args = p.parse_args()
    cores = []
    for i, spec in enumerate(args.core):
        name, rest = spec.split('=', 1)
        jar, *jvm_args = rest.split(',')
        directory = args.root / name
        directory.mkdir(parents=True, exist_ok=True)
        (directory / 'server.properties').write_text(PROPS.format(seed=args.seed, port=25600 + i), encoding='utf-8')
        (directory / 'eula.txt').write_text('eula=true\n', encoding='utf-8')
        cores.append((name, Path(jar).resolve(), directory, jvm_args))
    for name, jar, directory, jvm_args in cores:
        print(f'warm-up {name}: {run_once(jar, directory, args.timeout, jvm_args)}', flush=True)
    results = {name: [] for name, *_ in cores}
    for r in range(args.rounds):
        for name, jar, directory, jvm_args in (cores if r % 2 == 0 else list(reversed(cores))):
            res = run_once(jar, directory, args.timeout, jvm_args)
            results[name].append(res)
            print(f'round {r + 1} {name}: {res}', flush=True)
    summary = {}
    for name, runs in results.items():
        summary[name] = {k: statistics.median([x[k] for x in runs if x[k] is not None]) if any(x[k] is not None for x in runs) else None
                         for k in ('spawn_select_s', 'level_s', 'done_s', 'wall_s')}
    print(json.dumps({'median': summary, 'runs': results}, ensure_ascii=False, indent=1))


if __name__ == '__main__':
    main()
