"""Serial local A/B and JVM runs. Refuses existing labels through run-native.py."""
import argparse
from pathlib import Path
import subprocess
import sys

p = argparse.ArgumentParser()
p.add_argument('--prefix', required=True)
p.add_argument('--stage', choices=['particles', 'load', 'clients'], required=True)
p.add_argument('--repeats', type=int, default=3)
p.add_argument('--functional-only', action='store_true')
p.add_argument('--cases', help='Comma-separated case names to run from the selected stage')
args = p.parse_args()
base = Path(__file__).resolve().parent
repo = base.parent
java21 = Path('C:/Program Files/Eclipse Adoptium/jdk-21.0.12.101-hotspot/bin/java.exe')
java25 = Path('D:/water-mc/jdks/jdk-25.0.5+8/bin/java.exe')
graal = Path('D:/water-mc/jdks/graalvm-jdk-25.0.4+7.1/bin/java.exe')
native = repo / 'watermc-server/build/libs/watermc-1.21.11.jar'
plugin = base / 'waterbench/build/libs/waterbench-1.0.0.jar'
leaf = Path('D:/water-mc/run/leaf-baseline.jar')

if args.stage == 'particles':
    cases = [('native-off', native, java21, 'g1.flags', None),
             ('native-on', native, java21, 'g1.flags', base / 'configs/watermc-particle-limit')]
    spec = 'particles:10:600'
else:
    cases = [('leaf-g1', leaf, java21, 'g1.flags', None),
             ('native-g1', native, java21, 'g1.flags', None),
             ('native-temurin25', native, java25, 'g1.flags', None),
             ('native-graal-g1', native, graal, 'g1.flags', None),
             ('native-graal-zgc', native, graal, 'zgc25.flags', None),
             ('native-graal-meow', native, graal, 'meowice-g1-windows-fx4100.flags', None)]
    spec = 'particles:10:600' if args.stage == 'clients' else 'tntburst:1:500,entities:300:20,chunkgen:2'

if args.functional_only:
    spec = 'particles:3:200' if args.stage in {'particles', 'clients'} else 'tntburst:1:100,entities:40:2,chunkgen:1'

if args.cases:
    selected = set(args.cases.split(','))
    unknown = selected - {case[0] for case in cases}
    if unknown:
        p.error('unknown case(s): ' + ', '.join(sorted(unknown)))
    cases = [case for case in cases if case[0] in selected]

for repeat in range(args.repeats):
    # Rotate order to reduce systematic cold-cache and thermal bias.
    order = cases[repeat % len(cases):] + cases[:repeat % len(cases)]
    for label, jar, java, flags, overrides in order:
        name = f'{args.prefix}-{label}-{repeat + 1}'
        command = [sys.executable, str(base / 'run-native.py'), name, '--jar', str(jar),
                   '--java', str(java), '--flags-file', str(base / 'jvm' / flags),
                   '--spec', spec, '--bots', '2', '--plugin', str(plugin)]
        if overrides:
            command += ['--overrides', str(overrides)]
        if args.functional_only:
            command += ['--functional-only', '--heap', '2G']
        print('START', name, flush=True)
        subprocess.run(command, check=True)
        print('COMPLETE', name, flush=True)
