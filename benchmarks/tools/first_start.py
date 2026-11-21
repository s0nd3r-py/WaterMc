"""Первый запуск в новой папке: время до Done и хронология консоли. Ядра идут в заданном порядке.

  python benchmarks/tools/first_start.py --core leaf=leaf.jar --core water=watermc.jar [--jfr]
"""
import argparse
import json
import shutil
import subprocess
import time
from pathlib import Path

JAVA = r'C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe'


def run(name: str, jar: Path, directory: Path, jfr: bool) -> dict:
    if directory.exists():
        shutil.rmtree(directory)
    directory.mkdir(parents=True)
    for f in ('eula.txt', 'server.properties'):
        shutil.copy2(Path(r'D:\water-mc\run\base') / f, directory / f)
    flags = ['-Xms2G', '-Xmx2G']
    if jfr:
        flags += ['-XX:+UnlockDiagnosticVMOptions', '-XX:+DebugNonSafepoints',
                  f'-XX:StartFlightRecording=filename={directory / "startup.jfr"},settings=profile,dumponexit=true']
    begin = time.time()
    proc = subprocess.Popen([JAVA, *flags, '-jar', str(jar), '--nogui'], cwd=directory, stdin=subprocess.PIPE,
                            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, encoding='cp1251', errors='replace')
    timeline = []
    done = None
    for line in proc.stdout:
        t = round(time.time() - begin, 2)
        timeline.append(f'{t:7.2f} {line.rstrip()}')
        if 'Done (' in line and done is None:
            done = t
            proc.stdin.write('stop\n')
            proc.stdin.flush()
        if time.time() - begin > 900:
            proc.kill()
            break
    proc.wait(timeout=180)
    (directory / 'timeline.txt').write_text('\n'.join(timeline), encoding='utf-8')
    return {'core': name, 'done_s': done}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--core', action='append', required=True)
    parser.add_argument('--root', default=r'D:\water-mc\run\firststart')
    parser.add_argument('--jfr', action='store_true')
    args = parser.parse_args()
    for spec in args.core:
        name, jar = spec.split('=', 1)
        print(json.dumps(run(name, Path(jar).resolve(), Path(args.root) / name, args.jfr)), flush=True)


if __name__ == '__main__':
    main()
