"""Turn watermc-server/src/minecraft into per-file patches against Leaf's generated source, so the public
repository carries no Mojang code. Self-check: the patches applied to a clean copy of the base must reproduce
our tree byte for byte.

  python -I scripts/make-minecraft-patches.py <leaf-src-minecraft> <watermc-src-minecraft> <out-patches-dir> <work-dir>
"""
import filecmp
import os
import shutil
import subprocess
import sys
from pathlib import Path

base, ours, out, work = (Path(a).resolve() for a in sys.argv[1:5])
ENV = {'GIT_AUTHOR_NAME': 'gen', 'GIT_AUTHOR_EMAIL': 'gen@localhost', 'GIT_COMMITTER_NAME': 'gen',
       'GIT_COMMITTER_EMAIL': 'gen@localhost'}
NUL = bytes([0])
CRLF = bytes([13, 10])
LF = bytes([10])


def git(*args, cwd, out_bytes=False):
    r = subprocess.run(['git', '-c', 'core.autocrlf=false', '-c', 'core.safecrlf=false', *args], cwd=cwd,
                       env={**os.environ, **ENV}, capture_output=True, check=True)
    return r.stdout if out_bytes else r.stdout.decode('utf-8')


def copy_tree(src: Path, dst: Path):
    shutil.copytree(src, dst, ignore=shutil.ignore_patterns('.git'), dirs_exist_ok=True)
    # git on Windows checks text out as CRLF; patches and the self-check work on LF, as stored in git
    for f in dst.rglob('*'):
        if f.is_file():
            data = f.read_bytes()
            if NUL not in data and CRLF in data:
                f.write_bytes(data.replace(CRLF, LF))


def force_remove(func, path, _exc):
    os.chmod(path, 0o666)  # git marks object files read-only on Windows
    func(path)


repo = work / 'diff'
if work.exists():
    shutil.rmtree(work, onexc=force_remove)
repo.mkdir(parents=True)
copy_tree(ours, work / 'ours')
ours = work / 'ours'
git('init', '-q', cwd=repo)
copy_tree(base, repo)
git('add', '-A', cwd=repo)
git('commit', '-q', '-m', 'base', cwd=repo)
for child in repo.iterdir():
    if child.name != '.git':
        shutil.rmtree(child) if child.is_dir() else child.unlink()
copy_tree(ours, repo)
git('add', '-A', cwd=repo)

shutil.rmtree(out / 'minecraft', ignore_errors=True)
(out / 'minecraft').mkdir(parents=True)
deleted = []
count = 0
fields = git('diff', '--cached', '-M', '--name-status', '-z', cwd=repo).split('\0')
i = 0
while i < len(fields) - 1:
    status = fields[i]
    if status.startswith('R'):
        paths, name = [fields[i + 1], fields[i + 2]], fields[i + 2]
        i += 3
    else:
        paths, name = [fields[i + 1]], fields[i + 1]
        i += 2
    if status == 'D':
        deleted.append(name)  # no preimage: a deleted Mojang file must not be published
        continue
    patch = git('diff', '--cached', '-M', '--binary', '--full-index', '--', *paths, cwd=repo, out_bytes=True)
    target = out / 'minecraft' / (name + '.patch')
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(patch)
    count += 1
(out / 'minecraft-deleted.txt').write_text(''.join(p + '\n' for p in sorted(deleted)), encoding='utf-8', newline='\n')
print(f'{count} patches, {len(deleted)} deleted files')

# self-check
check = work / 'check'
copy_tree(base, check)
for p in deleted:
    (check / p).unlink()
    parent = (check / p).parent
    while parent != check and not any(parent.iterdir()):  # git keeps no empty directories
        parent.rmdir()
        parent = parent.parent
combined = work / 'all.patch'
with open(combined, 'wb') as f:
    for p in sorted((out / 'minecraft').rglob('*.patch')):
        f.write(p.read_bytes())
subprocess.run(['git', '-c', 'core.autocrlf=false', 'apply', '--unsafe-paths', '--whitespace=nowarn', '-p1', str(combined)],
               cwd=check, check=True)


def same(a: Path, b: Path) -> list[str]:
    cmp = filecmp.dircmp(a, b, ignore=['.git'])
    bad = [str(a / n) for n in cmp.left_only + cmp.right_only]
    bad += [str(a / n) for n in cmp.common_files if not filecmp.cmp(a / n, b / n, shallow=False)]
    for d in cmp.common_dirs:
        bad += same(a / d, b / d)
    return bad


diff = same(check, ours)
print('self-check:', 'OK, patches reproduce the tree exactly' if not diff else f'{len(diff)} mismatches, e.g. {diff[:5]}')
sys.exit(1 if diff else 0)
