#!/usr/bin/env python3
"""One fresh, non-destructive Windows/local benchmark; never reuses world state."""
import argparse
import datetime
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import socket
import subprocess
import sys
import time


def main():
    repository = Path(__file__).resolve().parent.parent
    local_runs = Path('D:/water-mc/run')
    local_modules = Path('D:/water-mc/bench/bots/node_modules')
    p = argparse.ArgumentParser()
    p.add_argument('label')
    p.add_argument('--jar', type=Path, required=True)
    p.add_argument('--java', type=Path, required=True)
    flag_source = p.add_mutually_exclusive_group()
    flag_source.add_argument('--flags-file', type=Path)
    flag_source.add_argument('--jvm-flags', help='Space-separated flags; use --jvm-flags=...')
    p.add_argument('--spec', default='particles:10:600')
    p.add_argument('--bots', type=int, default=2)
    p.add_argument('--port', type=int, default=0, help='0 selects an available loopback port')
    p.add_argument('--heap', default='4G')
    p.add_argument('--overrides', type=Path)
    p.add_argument('--root', type=Path, default=local_runs if local_runs.is_dir() else repository / 'build/benchmark-runs')
    p.add_argument('--template', type=Path, help='Defaults to <root>/base')
    p.add_argument('--seed-cache', type=Path, help='Optional library/cache seed; defaults to <root>/ab-leaf-1')
    p.add_argument('--node-modules', type=Path, default=local_modules if local_modules.is_dir() else repository / 'benchmarks/bots/node_modules')
    p.add_argument('--results-root', type=Path, default=repository / 'benchmarks/results' / (datetime.datetime.now(datetime.timezone.utc).date().isoformat() + '-native'))
    p.add_argument('--plugin', type=Path, required=True)
    p.add_argument('--timeout', type=int, default=1800)
    p.add_argument('--jfr-seconds', type=int, default=0, help='Record JFR after measurement begins (0 disables)')
    p.add_argument('--functional-only', action='store_true',
                   help='Run at reduced priority and exclude timing metrics from performance conclusions')
    args = p.parse_args()
    if not 0 <= args.jfr_seconds <= 3600:
        p.error('jfr-seconds must be between 0 and 3600')
    if len(args.label) > 100 or not re.fullmatch(r'[A-Za-z0-9][A-Za-z0-9_.-]*', args.label):
        p.error('label must be a simple name, not a path')
    directory = args.root.resolve() / args.label
    out = args.results_root.resolve() / args.label
    template = args.template or args.root / 'base'
    seed_cache = args.seed_cache or args.root / 'ab-leaf-1'
    if out.exists():
        p.error(f'refusing to overwrite archived results: {out}')
    if directory.exists():
        p.error(f'refusing to overwrite existing run: {directory}')
    with socket.socket() as port_probe:
        port_probe.bind(('127.0.0.1', args.port))
        args.port = port_probe.getsockname()[1]
    shutil.copytree(template, directory)
    for name in ['cache', 'libraries']:
        cached = seed_cache / name
        if cached.is_dir():
            shutil.copytree(cached, directory / name)
    for old in (directory / 'plugins').glob('*.jar'):
        if old.stem.lower().startswith('waterbench'):
            old.unlink()
    shutil.copy2(args.plugin, directory / 'plugins' / args.plugin.name)
    if args.overrides:
        shutil.copytree(args.overrides, directory, dirs_exist_ok=True)
    functional_watchdog_timeout = None
    if args.functional_only:
        spigot = directory / 'spigot.yml'
        if not spigot.exists():
            spigot.write_text('settings:\n  timeout-time: 300\n  restart-on-crash: false\n')
            functional_watchdog_timeout = 300
        paper = directory / 'config/paper-global.yml'
        if not paper.exists():
            paper.parent.mkdir(parents=True, exist_ok=True)
            paper.write_text('watchdog:\n  early-warning-every: 300000\n  early-warning-delay: 300000\n')
    properties = directory / 'server.properties'
    lines = [line for line in properties.read_text().splitlines()
             if not line.startswith(('server-port=', 'server-ip=', 'enable-query=', 'enable-rcon='))]
    lines += [f'server-port={args.port}', 'server-ip=127.0.0.1', 'enable-query=false', 'enable-rcon=false']
    properties.write_text('\n'.join(lines) + '\n')

    flags = args.jvm_flags.split() if args.jvm_flags is not None else (args.flags_file.read_text().split() if args.flags_file else ['-XX:+UseG1GC'])
    omitted_flags = []
    if args.functional_only:
        elevated = {'-XX:+UseCriticalJavaThreadPriority', '-XX:+UseThreadPriorities'}
        omitted_flags = [flag for flag in flags if flag in elevated]
        flags = [flag for flag in flags if flag not in elevated]
    command = [str(args.java.resolve()), f'-Xms{args.heap}', f'-Xmx{args.heap}', *flags,
               '-Dterminal.jline=false', '-Dterminal.ansi=false',
               '-Dstdout.encoding=UTF-8', '-Dstderr.encoding=UTF-8',
               f'-Xlog:gc*,safepoint:file=gc.log:time,uptime,level,tags',
               f'-Dwaterbench.auto={args.spec}', f'-Dwaterbench.label={args.label}',
               f'-Dwaterbench.players={args.bots}', '-Dwaterbench.shutdown=true',
               '-jar', str(args.jar.resolve()), 'nogui']
    if args.bots:
        command.insert(1, '-Dwaterbench.probe-acks=true')
    if args.functional_only:
        command.insert(1, '-Dpaper.playerconnection.keepalive=300')
    metadata = {'label': args.label, 'command': command, 'spec': args.spec,
                'functional_only': args.functional_only,
                'jfr_seconds': args.jfr_seconds,
                'functional_watchdog_timeout_seconds': functional_watchdog_timeout,
                'functional_omitted_priority_flags': omitted_flags,
                'port': args.port,
                'jar_sha256': hashlib.sha256(args.jar.read_bytes()).hexdigest(),
                'plugin_sha256': hashlib.sha256(args.plugin.read_bytes()).hexdigest(),
                'template': str(template.resolve()),
                'node_modules': str(args.node_modules.resolve()),
                'bot_package_versions': {name: json.loads((args.node_modules / name / 'package.json').read_text())['version']
                                         for name in ['minecraft-protocol', 'minecraft-data']
                                         if args.bots and (args.node_modules / name / 'package.json').is_file()},
                'started_utc': time.strftime('%Y-%m-%dT%H:%M:%SZ', time.gmtime())}
    (directory / 'run-metadata.json').write_text(json.dumps(metadata, indent=2))
    env = os.environ.copy()
    if os.name == 'nt' and Path('D:/water-mc/tmp').is_dir():
        env.update(TMP='D:/water-mc/tmp', TEMP='D:/water-mc/tmp')
    hidden = subprocess.CREATE_NO_WINDOW if os.name == 'nt' else 0
    if args.functional_only and os.name == 'nt':
        hidden |= subprocess.BELOW_NORMAL_PRIORITY_CLASS
    bots = None
    jfr_started = False
    with open(directory / 'server.log', 'w') as log, open(directory / 'bots.log', 'w') as bot_log:
        server = subprocess.Popen(command, cwd=directory, env=env, stdin=subprocess.PIPE,
                                  stdout=log, stderr=log, creationflags=hidden)
        metadata['server_pid'] = server.pid
        (directory / 'run-metadata.json').write_text(json.dumps(metadata, indent=2))
        started = time.monotonic()
        try:
            while server.poll() is None:
                if time.monotonic() - started > args.timeout:
                    raise TimeoutError(f'benchmark timed out: {args.label}')
                content = (directory / 'server.log').read_text(errors='replace')
                if 'BENCHMARK_FAILED' in content:
                    raise RuntimeError(f'invalid benchmark; see {directory / "server.log"}')
                if args.jfr_seconds and not jfr_started and 'BENCHMARK_MEASUREMENT_START ' in content:
                    jcmd = args.java.resolve().with_name('jcmd.exe' if os.name == 'nt' else 'jcmd')
                    profile = directory / 'profile.jfr'
                    jfr_command = [str(jcmd), str(server.pid), 'JFR.start', 'name=watermc_workload',
                                   'settings=profile', f'duration={args.jfr_seconds}s', f'filename="{profile}"']
                    with open(directory / 'jfr-start.log', 'w', encoding='utf-8') as jfr_log:
                        subprocess.run(jfr_command, env=env, stdout=jfr_log, stderr=jfr_log,
                                       creationflags=hidden, timeout=30, check=True)
                    metadata['jfr_command'] = jfr_command
                    (directory / 'run-metadata.json').write_text(json.dumps(metadata, indent=2))
                    jfr_started = True
                if args.bots and bots is None and 'Done (' in content:
                    bot_script = Path(__file__).resolve().parent / 'bots/packet-counter.js'
                    bot_env = env.copy()
                    bot_env['NODE_PATH'] = str(args.node_modules.resolve())
                    bots = subprocess.Popen(['node', str(bot_script), '127.0.0.1', str(args.port),
                                             str(args.bots), str(args.timeout), str(directory / 'packets.json')],
                                            cwd=directory, env=bot_env, stdout=bot_log, stderr=bot_log,
                                            creationflags=hidden)
                time.sleep(1)
            if server.returncode != 0:
                raise RuntimeError(f'server exited {server.returncode}; see {directory / "server.log"}')
            final_log = (directory / 'server.log').read_text(errors='replace')
            if 'BENCHMARK_FAILED' in final_log:
                raise RuntimeError('benchmark failed during final scenario/cleanup')
            completion = re.search(r'BENCHMARK_SCENARIOS_COMPLETE epoch_ms=(\d+)', final_log)
            if completion is None:
                raise RuntimeError('missing successful scenario completion marker')
            completed_at = int(completion.group(1))
            if bots is not None:
                bots.wait(timeout=30)
                if bots.returncode != 0:
                    raise RuntimeError(f'bots exited {bots.returncode}')
                received = json.loads((directory / 'packets.json').read_text())
                clients = received.get('clients', [])
                if received['total']['count'] <= 0 or len(clients) != args.bots or any(
                    not client.get('reachedPlay') or client.get('count', 0) <= 0 or client.get('reason') == 'keepAliveError'
                    or client.get('firstPlayAtUnixMs', completed_at + 1) > completed_at
                    or client.get('endedAtUnixMs', 0) < completed_at
                    for client in clients
                ):
                    raise RuntimeError('incomplete client participation; this run is not a passed client check')
                if 'was kicked due to keepalive timeout' in (directory / 'server.log').read_text(errors='replace'):
                    raise RuntimeError('server-side probe timeout; this run is not a passed client check')
        finally:
            if server.poll() is None:
                server.terminate()
                try:
                    server.wait(timeout=30)
                except subprocess.TimeoutExpired:
                    server.kill()
                    server.wait()
            if bots is not None and bots.poll() is None:
                bots.terminate()
                bots.wait(timeout=30)
            if server.stdin is not None:
                server.stdin.close()
            # Сохраняем также неудачные/частичные прогоны. Успех отмечается только
            # после всех проверок клиентов, итогов и маркеров сценария ниже.
            failure = sys.exc_info()[1]
            archive_run(directory, out, {
                'passed': False,
                'functional_only': args.functional_only,
                'state': 'failed' if failure is not None else 'pending_validation',
                'reason': str(failure) if failure is not None else 'Final result validation has not completed',
            })

    try:
        summary = directory / 'plugins/WaterBench/results' / args.label / 'summary.jsonl'
        if not summary.exists():
            raise RuntimeError(f'no results; see {directory / "server.log"}')
        lines = [json.loads(line) for line in summary.read_text().splitlines() if line]
        expected = len(args.spec.split(','))
        if len(lines) != expected or any(row['ticks'] <= 0 for row in lines):
            raise RuntimeError(f'incomplete benchmark: {len(lines)} of {expected} scenarios')
        scenario_names = [part.split(':')[0] for part in args.spec.split(',')]
        if [row['label'] for row in lines] != [args.label + '-' + name for name in scenario_names]:
            raise RuntimeError('scenario result names/order do not match the requested workload')
        if args.bots:
            for client in received['clients']:
                phases = client.get('scenarios', [])
                if client.get('phaseErrors') or [phase['name'] for phase in phases] != scenario_names or [phase.get('sequence') for phase in phases] != list(range(1, len(scenario_names) + 1)) or any(
                    phase['label'] != args.label or not phase.get('endedAtUnixMs') for phase in phases
                ):
                    raise RuntimeError(f'incomplete scenario packet boundaries for client {client["index"]}')
                if any(error.get('completedPhases', 0) < len(scenario_names) or error.get('activePhase') is not None for error in client.get('errors', [])):
                    raise RuntimeError(f'client transport error before the last scenario ended: {client["index"]}')
            if 'particles' in scenario_names:
                matches = re.findall(r'PARTICLE_RESULT normal_sent=(\d+) normal_requested=\d+ forced_sent=(\d+) always_sent=(\d+) vibration_sent=(\d+) targeted_sent=(\d+) api_calls=(\d+)', final_log)
                if len(matches) != scenario_names.count('particles'):
                    raise RuntimeError('missing particle delivery accounting')
                for occurrence, counts in enumerate(matches):
                    values = list(map(int, counts))
                    expected = sum(values[:5]) + values[5] * args.bots
                    actual = 0
                    for client in received['clients']:
                        phase = [row for row in client['scenarios'] if row['name'] == 'particles'][occurrence]
                        actual += phase['byType'].get('world_particles', {}).get('count', 0)
                    if actual != expected:
                        raise RuntimeError(f'particle probe did not receive every expected packet: {actual} of {expected}')
        if 'explosionpackets' in scenario_names:
            received = json.loads((directory / 'packets.json').read_text())
            expected = [(9000, None), (9001, {'x': 0.1, 'y': 0.2, 'z': 0.3}),
                        (9003, {'x': -0.4, 'y': 0.01, 'z': 0.0625}), (9004, {'x': 0, 'y': 0, 'z': 0})]
            for client in received['clients']:
                packets = [(packet['center']['x'], packet['playerKnockback'])
                           for packet in client.get('explosions', []) if 9000 <= packet['center']['x'] < 9006]
                if packets != expected:
                    raise RuntimeError(f'wrong explosion packet order/knockback for client {client["index"]}: {packets}')
        if args.jfr_seconds and (not jfr_started or not (directory / 'profile.jfr').is_file() or (directory / 'profile.jfr').stat().st_size == 0):
            raise RuntimeError('requested JFR recording is missing')
        if 'soak' in scenario_names:
            specs = [part.split(':') for part in args.spec.split(',') if part.split(':')[0] == 'soak']
            markers = re.findall(r'SOAK_CHECKS measured_ticks=(\d+) wall_millis=(\d+) alive=(\d+)', final_log)
            rows = [row for row in lines if row['label'] == args.label + '-soak']
            if len(markers) != len(specs): raise RuntimeError('missing soak duration/population checks')
            for spec, values, row in zip(specs, markers, rows):
                measured, elapsed, alive = map(int, values)
                requested_count = int(spec[1]) if len(spec) > 1 else 40
                requested_seconds = int(spec[2]) if len(spec) > 2 else 3600
                if measured != row['ticks'] or elapsed < requested_seconds * 1000 or alive != requested_count:
                    raise RuntimeError('soak did not retain its population for the requested duration')
        if 'soak' in scenario_names and 'SOAK_CHECKS ' not in (directory / 'server.log').read_text(errors='replace'):
            raise RuntimeError('stable-population soak did not complete its checks')
        if 'compat' in scenario_names and 'COMPAT_CHECKS ' not in (directory / 'server.log').read_text(errors='replace'):
            raise RuntimeError('compatibility scenario did not emit its completion checks')
        archive_run(directory, out, {'passed': True, 'functional_only': args.functional_only,
                                     'state': 'passed', 'scenarios': scenario_names})
        print(json.dumps({'label': args.label, 'results': lines, 'output': str(out)}), flush=True)

    except Exception as failure:
        archive_run(directory, out, {'passed': False, 'functional_only': args.functional_only,
                                     'state': 'failed_validation', 'reason': str(failure)})
        raise


def archive_run(directory, out, status):
    out.mkdir(parents=True, exist_ok=True)
    for name in ['run-metadata.json', 'packets.json', 'server.log', 'bots.log', 'gc.log', 'profile.jfr', 'jfr-start.log']:
        if (directory / name).exists():
            shutil.copy2(directory / name, out / name)
    metrics = directory / 'plugins/WaterBench/results' / directory.name
    if metrics.is_dir():
        shutil.copytree(metrics, out / 'metrics', dirs_exist_ok=True)
    (out / 'status.json').write_text(json.dumps(status, indent=2) + '\n')


if __name__ == '__main__':
    main()
