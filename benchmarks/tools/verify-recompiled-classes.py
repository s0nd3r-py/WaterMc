"""Сравнить javap-код и дескрипторы пересобранных классов без debug-имён/индексов CP."""
import argparse
import difflib
import hashlib
import io
import json
from pathlib import Path
import re
import subprocess
import tempfile
import zipfile

p = argparse.ArgumentParser(description=__doc__)
p.add_argument('--baseline', type=Path, required=True)
p.add_argument('--current', type=Path, required=True)
p.add_argument('--javap', type=Path, required=True)
p.add_argument('--delta', type=Path, required=True)
p.add_argument('--output', type=Path, required=True)
p.add_argument('--allow-code-change', action='append', default=['net/minecraft/world/ticks/LevelTicks.class'])
a = p.parse_args()
delta = json.loads(a.delta.read_text())
expected_changes = set(a.allow_code_change)
names = [name[:-6].replace('/', '.') for name in delta['changed'] if name not in expected_changes]
with zipfile.ZipFile(a.current) as archive:
    row = archive.read('META-INF/versions.list').decode().strip().split('\t')
    payload = archive.read('META-INF/versions/' + row[2])
    assert hashlib.sha256(payload).hexdigest() == row[0]
tempbase = Path('D:/water-mc/tmp')
with tempfile.NamedTemporaryFile(dir=tempbase if tempbase.is_dir() else None, suffix='.jar', delete=False) as temporary:
    tempjar = Path(temporary.name)
    temporary.write(payload)
try:
    outputs = []
    for jar in [a.baseline, tempjar]:
        result = subprocess.run([str(a.javap), '-classpath', str(jar), '-p', '-c', '-s', *names],
                                capture_output=True, text=True, check=True, timeout=120)
        outputs.append(re.sub(r'#\d+', '#CP', result.stdout))
    equal = outputs[0] == outputs[1]
    report = {'compared_classes': names, 'javap_code_and_descriptors_match': equal,
              'excluded_expected_code_changes': sorted(expected_changes),
              'comparison_omits': ['debug tables/parameter names', 'constant pool indices'],
              'baseline_server_sha256': delta['baseline_server_sha256'],
              'current_server_sha256': row[0]}
    a.output.write_text(json.dumps(report, indent=2) + '\n')
    if not equal:
        print(''.join(difflib.unified_diff(outputs[0].splitlines(True), outputs[1].splitlines(True))))
        raise SystemExit(1)
    print(json.dumps({'compared_class_count': len(names), 'javap_code_and_descriptors_match': equal}), flush=True)
finally:
    tempjar.unlink()
