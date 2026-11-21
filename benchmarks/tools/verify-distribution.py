"""Verify the full bundle's launcher, server identity, source revision and core classes."""
import hashlib
import io
import json
from pathlib import Path
import subprocess
import zipfile

repo = Path(__file__).resolve().parents[2]
jar = repo / 'watermc-server/build/libs/watermc-1.21.11.jar'
runtime_inputs = ['watermc-server/src', 'watermc-api/src', 'watermc-server/build.gradle.kts',
                  'watermc-api/build.gradle.kts', 'build.gradle.kts', 'settings.gradle.kts',
                  'gradle.properties', 'gradle']
with zipfile.ZipFile(jar) as bundle:
    main = bundle.read('META-INF/main-class').decode().strip()
    assert main == 'org.bukkit.craftbukkit.Main', main
    version_row = bundle.read('META-INF/versions.list').decode().strip().split('\t')
    server_path = 'META-INF/versions/' + version_row[2]
    payload = bundle.read(server_path)
    assert hashlib.sha256(payload).hexdigest() == version_row[0]
    assert 'META-INF/patches.list' not in bundle.namelist()
    with zipfile.ZipFile(io.BytesIO(payload)) as server:
        manifest = server.read('META-INF/MANIFEST.MF').decode()
        assert 'Brand-Name: WaterMc' in manifest
        assert 'Brand-Id: watermc:watermc' in manifest
        revision = next(line.split(': ', 1)[1] for line in manifest.splitlines() if line.startswith('Git-Commit: '))
        subprocess.run(['git', 'rev-parse', '--verify', revision + '^{commit}'], cwd=repo,
                       check=True, capture_output=True)
        subprocess.run(['git', 'diff', '--quiet', revision, '--', *runtime_inputs], cwd=repo, check=True)
        assert 'org/watermc/network/ParticleBudget.class' in server.namelist()
        assert 'org/watermc/network/ExplosionPacketLimiter$Pending.class' not in server.namelist()
        report = dict(jar=str(jar), bytes=jar.stat().st_size,
                      sha256=hashlib.sha256(jar.read_bytes()).hexdigest(),
                      main_class=main, binary_deltas_required=False,
                      source_revision=revision,
                      current_runtime_sources_match=True,
                      server_sha256=version_row[0], server_manifest=manifest,
                      server_classes=sum(name.endswith('.class') for name in server.namelist()))
out = repo / 'docs/distribution-verification.json'
out.write_text(json.dumps(report, indent=2) + '\n')
print(json.dumps({key: value for key, value in report.items() if key != 'server_manifest'}), flush=True)
