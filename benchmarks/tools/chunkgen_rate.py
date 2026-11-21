"""Скорость генерации новых чанков: после старта сервер синхронно генерирует forceload-область 16×16 (256 чанков)
вдали от спавна; время до ответа команды → чанков в секунду. Ядра чередуются по раундам, мир каждый раз новый.

  python benchmarks/tools/chunkgen_rate.py --core leaf=a.jar --core water=b.jar --rounds 2
"""
import argparse
import json
import shutil
import statistics
import subprocess
import time
from pathlib import Path

JAVA = r'C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe'
WORLDS = ('world', 'world_nether', 'world_the_end', 'worlds')  # WaterMc keeps its worlds under worlds/
PROPS = """level-seed={seed}
level-type=minecraft\\:normal
online-mode=false
server-port={port}
view-distance=4
spawn-protection=0
"""
CHUNKS = 256


def run_once(jar: Path, directory: Path, area: int, timeout: int, jvm_args) -> float | None:
    for w in WORLDS:
        shutil.rmtree(directory / w, ignore_errors=True)
    proc = subprocess.Popen([JAVA, '-Xms2G', '-Xmx2G', '-Dwatermc.console.language=en', *jvm_args, '-jar', str(jar), '--nogui'],
                            cwd=directory, stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                            text=True, encoding='utf-8', errors='replace')
    begin = time.time()
    sent = None
    rate = None
    for line in proc.stdout:
        if sent is None and 'Done (' in line:
            time.sleep(5)  # let spawn-area follow-up work settle
            proc.stdin.write(f'forceload add {area} {area} {area + 255} {area + 255}\n')
            proc.stdin.flush()
            sent = time.time()
        elif sent is not None and 'Marked' in line:  # "Marked 256 chunks ... to be force loaded", printed once all are generated
            rate = round(CHUNKS / (time.time() - sent), 2)
            proc.stdin.write('stop\n')
            proc.stdin.flush()
            break
        if time.time() - begin > timeout:
            break
    if rate is None:
        proc.kill()
    try:
        proc.communicate(timeout=180)
    except subprocess.TimeoutExpired:
        proc.kill()
    return rate


def main():
    p = argparse.ArgumentParser()
    p.add_argument('--core', action='append', required=True, help='name=path/to.jar[,-Dkey=value...]')
    p.add_argument('--rounds', type=int, default=2)
    p.add_argument('--seed', default='1234567890')
    p.add_argument('--root', type=Path, default=Path(r'D:\water-mc\run\chunkgen'))
    p.add_argument('--timeout', type=int, default=1800)
    args = p.parse_args()
    cores = []
    for i, spec in enumerate(args.core):
        name, rest = spec.split('=', 1)
        jar, *jvm_args = rest.split(',')
        directory = args.root / name
        directory.mkdir(parents=True, exist_ok=True)
        (directory / 'server.properties').write_text(PROPS.format(seed=args.seed, port=25650 + i), encoding='utf-8')
        (directory / 'eula.txt').write_text('eula=true\n', encoding='utf-8')
        # forceload generates synchronously on the main thread; keep the watchdog from killing a slow machine
        (directory / 'spigot.yml').write_text('settings:\n  timeout-time: 3600\n  restart-on-crash: false\n', encoding='utf-8')
        cores.append((name, Path(jar).resolve(), directory, jvm_args))
    results = {name: [] for name, *_ in cores}
    for r in range(args.rounds):
        area = 2048 + r * 4096  # fresh terrain every round
        for name, jar, directory, jvm_args in (cores if r % 2 == 0 else list(reversed(cores))):
            rate = run_once(jar, directory, area, args.timeout, jvm_args)
            results[name].append(rate)
            print(f'round {r + 1} {name}: {rate} chunks/s', flush=True)
    median = {name: statistics.median([x for x in runs if x is not None]) if any(x is not None for x in runs) else None
              for name, runs in results.items()}
    print(json.dumps({'median_chunks_per_s': median, 'runs': results}, ensure_ascii=False, indent=1))


if __name__ == '__main__':
    main()
