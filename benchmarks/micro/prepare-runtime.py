"""Подготовить отдельный classpath из полного JAR без удаления прежних runtime."""
import argparse
import hashlib
import json
from pathlib import Path, PurePosixPath
import zipfile

p = argparse.ArgumentParser(description=__doc__)
p.add_argument('--jar', type=Path, required=True)
a = p.parse_args()
base = Path(__file__).resolve().parent
source_hash = hashlib.sha256(a.jar.read_bytes()).hexdigest()
destination = base / 'build/runtime' / source_hash
index_file = destination / 'runtime-index.json'
if destination.exists():
    if not index_file.is_file():
        raise RuntimeError('Незавершённый runtime сохранён; выберите новый JAR или проверьте каталог: ' + str(destination))
    index = json.loads(index_file.read_text())
    assert index['bundle_sha256'] == source_hash
    for entry in index['files']:
        assert hashlib.sha256((destination / entry['path']).read_bytes()).hexdigest() == entry['sha256']
else:
    destination.mkdir(parents=True)
    entries = []
    with zipfile.ZipFile(a.jar) as archive:
        for table, prefix, output_prefix in [('META-INF/versions.list', 'META-INF/versions/', 'server/'),
                                             ('META-INF/libraries.list', 'META-INF/libraries/', 'libraries/')]:
            for line in archive.read(table).decode().splitlines():
                if not line.strip(): continue
                digest, identifier, name = line.split('\t')
                relative = PurePosixPath(name)
                if relative.is_absolute() or '..' in relative.parts:
                    raise ValueError('Некорректный путь в JAR')
                target = (destination / output_prefix).joinpath(*relative.parts)
                if not target.resolve().is_relative_to(destination.resolve()):
                    raise ValueError('Путь выходит из каталога runtime')
                payload = archive.read(prefix + name)
                assert hashlib.sha256(payload).hexdigest() == digest
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_bytes(payload)
                entries.append({'path': target.relative_to(destination).as_posix(), 'sha256': digest})
    index = {'bundle': str(a.jar.resolve()), 'bundle_sha256': source_hash, 'files': entries}
    index_file.write_text(json.dumps(index, indent=2) + '\n')
(base / 'build/runtime-location.txt').write_text(str(destination.resolve()) + '\n')
print(json.dumps({'directory': str(destination.resolve()), 'bundle_sha256': source_hash, 'jars': len(index['files'])}))
