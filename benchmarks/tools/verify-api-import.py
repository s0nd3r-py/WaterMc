"""Compare API class bytes with the pre-import Leaf API build."""
import hashlib
import json
from pathlib import Path
import zipfile

repo = Path(__file__).resolve().parents[2]
old = Path('D:/water-mc/Water/leaf-api/build/libs/leaf-api-1.21.11-R0.1-SNAPSHOT.jar')
new = repo / 'watermc-api/build/libs/watermc-api-1.21.11-R0.1-SNAPSHOT.jar'

def classes(path):
    with zipfile.ZipFile(path) as jar:
        return {name: hashlib.sha256(jar.read(name)).hexdigest()
                for name in jar.namelist() if name.endswith('.class')}

before, after = classes(old), classes(new)
removed = sorted(before.keys() - after.keys())
added = sorted(after.keys() - before.keys())
changed = sorted(name for name in before.keys() & after.keys() if before[name] != after[name])
report = dict(old_jar=str(old), new_jar=str(new), original_classes=len(before),
              imported_classes=len(after), removed=removed, added=added, changed=changed)
(repo / 'docs/api-import-verification.json').write_text(json.dumps(report, indent=2) + '\n')
print(json.dumps(report), flush=True)
if removed or added or changed:
    raise SystemExit(1)
