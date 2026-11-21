import json
from pathlib import Path
import subprocess
import time

base = Path(__file__).resolve().parent
variants = [
    ('temurin21-g1', Path('C:/Program Files/Eclipse Adoptium/jdk-21.0.12.101-hotspot/bin/java.exe'), ['-XX:+UseG1GC']),
    ('temurin25-g1', Path('D:/water-mc/jdks/jdk-25.0.5+8/bin/java.exe'), ['-XX:+UseG1GC']),
    ('graal25-g1', Path('D:/water-mc/jdks/graalvm-jdk-25.0.4+7.1/bin/java.exe'), ['-XX:+UseG1GC']),
    ('graal25-zgc', Path('D:/water-mc/jdks/graalvm-jdk-25.0.4+7.1/bin/java.exe'), ['-XX:+UseZGC']),
    ('graal25-meow-g1-windows', Path('D:/water-mc/jdks/graalvm-jdk-25.0.4+7.1/bin/java.exe'),
     (base / 'meowice-g1-windows-fx4100.flags').read_text().split()),
]
results = []
for label, java, flags in variants:
    command = [str(java), '-Xms1G', '-Xmx1G', *flags, '-version']
    start = time.monotonic()
    try:
        result = subprocess.run(command, capture_output=True, text=True, timeout=120)
        row = dict(label=label, command=command, returncode=result.returncode,
                   elapsed_seconds=time.monotonic() - start, stdout=result.stdout, stderr=result.stderr)
    except subprocess.TimeoutExpired as failure:
        row = dict(label=label, command=command, returncode=None, error=str(failure))
    results.append(row)
    print(label, row['returncode'], flush=True)
out = base.parent / 'results/2026-11-08-native/jvm-startup.json'
out.parent.mkdir(parents=True, exist_ok=True)
out.write_text(json.dumps(results, indent=2))
if any(row['returncode'] != 0 for row in results):
    raise SystemExit(1)
