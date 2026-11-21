"""Побайтовое сравнение классов двух серверов, включая полный bundler."""
import argparse
import hashlib
import io
import json
from pathlib import Path
import zipfile


def server_bytes(path):
    payload = path.read_bytes()
    with zipfile.ZipFile(io.BytesIO(payload)) as archive:
        if 'META-INF/versions.list' in archive.namelist():
            row = archive.read('META-INF/versions.list').decode().strip().split('\t')
            payload = archive.read('META-INF/versions/' + row[2])
            assert hashlib.sha256(payload).hexdigest() == row[0]
    return payload


def classes(payload):
    with zipfile.ZipFile(io.BytesIO(payload)) as archive:
        return {name: archive.read(name) for name in archive.namelist() if name.endswith('.class')}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--baseline', type=Path, required=True)
    parser.add_argument('--current', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    args = parser.parse_args()
    before = server_bytes(args.baseline)
    after = server_bytes(args.current)
    a, b = classes(before), classes(after)
    changed = sorted(name for name in a.keys() & b.keys() if a[name] != b[name])
    report = {'baseline': str(args.baseline), 'current': str(args.current),
              'baseline_server_sha256': hashlib.sha256(before).hexdigest(),
              'current_server_sha256': hashlib.sha256(after).hexdigest(),
              'added': sorted(b.keys() - a.keys()), 'removed': sorted(a.keys() - b.keys()),
              'changed': changed, 'unchanged_class_count': len(a.keys() & b.keys()) - len(changed)}
    args.output.write_text(json.dumps(report, indent=2) + '\n')
    print(json.dumps(report), flush=True)


if __name__ == '__main__':
    main()
