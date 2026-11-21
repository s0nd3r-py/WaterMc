"""Память под загруженные чанки: один и тот же мир в двух ядрах, гистограмма кучи после полной сборки.

python benchmarks/tools/chunk_memory.py generate <server.jar> <dir> <java>
python benchmarks/tools/chunk_memory.py measure  <server.jar> <world-source-dir> <dir> <java> <label>
python benchmarks/tools/chunk_memory.py compare  <histogram-a.txt> <histogram-b.txt>

generate — новый обычный мир с фиксированным сидом и forceload 1024 чанков (4 квадрата 16×16).
measure  — копия мира из world-source-dir, старт, ожидание, `jcmd GC.class_histogram` (полная сборка).
"""
import os
import re
import shutil
import subprocess
import sys
import threading
import time

BELOW_NORMAL = 0x00004000
SEED = '20261119'
# четыре несмежных квадрата 16×16 чанков (forceload принимает до 256 чанков за команду)
AREAS = [(0, 0), (512, 0), (0, 512), (512, 512)]
INTERESTING = [
    '[J', '[I', '[B', '[S', '[C', '[Ljava.lang.Object;',
    'me.s0nd3r.watermc.storage.WaterSectionStorage',
    'net.minecraft.util.SimpleBitStorage', 'net.minecraft.util.ZeroBitStorage',
    'net.minecraft.world.level.chunk.PalettedContainer', 'net.minecraft.world.level.chunk.LevelChunkSection',
    'net.minecraft.world.level.chunk.LevelChunk', 'net.minecraft.world.level.chunk.HashMapPalette',
    'net.minecraft.world.level.chunk.LinearPalette', 'net.minecraft.world.level.chunk.SingleValuePalette',
    'net.minecraft.world.level.chunk.PalettedContainer$Data',
]


def server_properties(port):
    return '\n'.join([
        'online-mode=false', 'level-type=minecraft\\:normal', f'level-seed={SEED}', 'generate-structures=true',
        'spawn-protection=0', 'view-distance=4', 'simulation-distance=4', 'max-players=10', 'sync-chunk-writes=false',
        f'server-port={port}', f'query.port={port}', 'enable-rcon=false', 'motd=chunk-memory', 'difficulty=peaceful',
    ]) + '\n'


def start(java, jar, cwd, log_name, heap='4G'):
    log = open(os.path.join(cwd, log_name), 'w', encoding='utf-8')
    proc = subprocess.Popen([java, f'-Xms{heap}', f'-Xmx{heap}', '-XX:+UseG1GC', '-jar', jar, 'nogui'], cwd=cwd,
                            stdin=subprocess.PIPE, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                            creationflags=BELOW_NORMAL if os.name == 'nt' else 0, text=True, encoding='utf-8', errors='replace')
    done = threading.Event()
    lines = []

    def reader():
        for line in proc.stdout:
            lines.append(line)
            log.write(line)
            log.flush()
            if 'Done (' in line:
                done.set()
    threading.Thread(target=reader, daemon=True).start()
    return proc, done, lines, log


def command(proc, text):
    proc.stdin.write(text + '\n')
    proc.stdin.flush()


def stop(proc, log):
    command(proc, 'stop')
    try:
        proc.wait(600)
    except subprocess.TimeoutExpired:
        proc.kill()
        proc.wait()
    log.close()


def generate(jar, run_dir, java):
    if os.path.exists(run_dir):
        raise SystemExit('exists: ' + run_dir)
    os.makedirs(run_dir)
    open(os.path.join(run_dir, 'eula.txt'), 'w').write('eula=true\n')
    open(os.path.join(run_dir, 'server.properties'), 'w').write(server_properties(25621))
    # Только для генерации: синхронная загрузка forceload на медленной машине длится дольше стандартного watchdog.
    open(os.path.join(run_dir, 'spigot.yml'), 'w').write('settings:\n  timeout-time: 3600\n  restart-on-crash: false\n')
    proc, done, lines, log = start(java, jar, run_dir, 'generate.log')
    if not done.wait(1800):
        raise SystemExit('server did not start')
    # порции 8×8 чанков, чтобы один тик не генерировал сотни чанков
    for x0, z0 in AREAS:
        for dx in range(0, 16, 8):
            for dz in range(0, 16, 8):
                bx, bz = (x0 + dx) * 16, (z0 + dz) * 16
                command(proc, f'forceload add {bx} {bz} {bx + 127} {bz + 127}')
                time.sleep(25)
    time.sleep(60)
    command(proc, 'forceload query')
    command(proc, 'save-all flush')
    time.sleep(30)
    stop(proc, log)
    print('generated; exit', proc.returncode)


def copy_world(source, target):
    os.makedirs(target)
    open(os.path.join(target, 'eula.txt'), 'w').write('eula=true\n')
    open(os.path.join(target, 'server.properties'), 'w').write(server_properties(25622))
    open(os.path.join(target, 'spigot.yml'), 'w').write('settings:\n  timeout-time: 3600\n  restart-on-crash: false\n')
    for name in ('world', 'world_nether', 'world_the_end'):
        src = os.path.join(source, name)
        if os.path.isdir(src):
            shutil.copytree(src, os.path.join(target, name), ignore=shutil.ignore_patterns('session.lock'))
    for name in ('cache', 'libraries', 'versions'):
        src = os.path.join(source, name)
        if os.path.isdir(src):
            shutil.copytree(src, os.path.join(target, name))


def measure(jar, source, run_dir, java, label):
    if os.path.exists(run_dir):
        raise SystemExit('exists: ' + run_dir)
    copy_world(source, run_dir)
    proc, done, lines, log = start(java, jar, run_dir, 'measure.log')
    if not done.wait(1800):
        raise SystemExit('server did not start')
    command(proc, 'forceload query')
    time.sleep(90)  # загрузка форсированных чанков и прогрев
    jcmd = os.path.join(os.path.dirname(java), 'jcmd.exe' if os.name == 'nt' else 'jcmd')
    hist = subprocess.run([jcmd, str(proc.pid), 'GC.class_histogram'], capture_output=True, text=True).stdout
    heap = subprocess.run([jcmd, str(proc.pid), 'GC.heap_info'], capture_output=True, text=True).stdout
    open(os.path.join(run_dir, f'histogram-{label}.txt'), 'w', encoding='utf-8').write(hist)
    open(os.path.join(run_dir, f'heap-{label}.txt'), 'w', encoding='utf-8').write(heap)
    forced = [l for l in lines if 'force loaded' in l.lower() or 'forceloaded' in l.lower()]
    stop(proc, log)
    print(label, 'histogram:', os.path.join(run_dir, f'histogram-{label}.txt'))
    print(''.join(forced[-3:]))
    summarize(hist, label)


def parse(hist_text):
    rows = {}
    total = 0
    for line in hist_text.splitlines():
        m = re.match(r'\s*\d+:\s+(\d+)\s+(\d+)\s+(\S+)', line)
        if m:
            rows[m.group(3)] = (int(m.group(1)), int(m.group(2)))
        t = re.match(r'Total\s+(\d+)\s+(\d+)', line.strip())
        if t:
            total = int(t.group(2))
    return rows, total


def summarize(hist_text, label):
    rows, total = parse(hist_text)
    chunks = rows.get('net.minecraft.world.level.chunk.LevelChunk', (0, 0))[0]
    print(f'== {label}: live heap after full GC {total / 2**20:.1f} MiB, LevelChunk instances {chunks}')
    for name in INTERESTING:
        n, b = rows.get(name, (0, 0))
        print(f'  {name:60s} {n:9d} objects {b / 2**20:9.1f} MiB')


def compare(a_path, b_path):
    a_rows, a_total = parse(open(a_path, encoding='utf-8').read())
    b_rows, b_total = parse(open(b_path, encoding='utf-8').read())
    a_chunks = a_rows.get('net.minecraft.world.level.chunk.LevelChunk', (1, 0))[0] or 1
    b_chunks = b_rows.get('net.minecraft.world.level.chunk.LevelChunk', (1, 0))[0] or 1
    print(f'{"":60s} {"A":>12s} {"B":>12s}')
    print(f'{"live heap (MiB)":60s} {a_total / 2**20:12.1f} {b_total / 2**20:12.1f}')
    print(f'{"LevelChunk instances":60s} {a_chunks:12d} {b_chunks:12d}')
    print(f'{"live heap per loaded chunk (KiB)":60s} {a_total / a_chunks / 1024:12.1f} {b_total / b_chunks / 1024:12.1f}')
    names = sorted(set(a_rows) | set(b_rows), key=lambda n: -abs(b_rows.get(n, (0, 0))[1] - a_rows.get(n, (0, 0))[1]))
    print('-- largest differences (MiB, B - A)')
    for name in names[:20]:
        da = a_rows.get(name, (0, 0))[1]
        db = b_rows.get(name, (0, 0))[1]
        print(f'{name[:60]:60s} {da / 2**20:12.1f} {db / 2**20:12.1f}  {(db - da) / 2**20:+.1f}')


if __name__ == '__main__':
    mode = sys.argv[1]
    if mode == 'generate':
        generate(*sys.argv[2:5])
    elif mode == 'measure':
        measure(*sys.argv[2:7])
    elif mode == 'compare':
        compare(*sys.argv[2:4])
    else:
        raise SystemExit(__doc__)
