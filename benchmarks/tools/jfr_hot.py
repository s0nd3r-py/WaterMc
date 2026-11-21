"""Aggregate JFR execution samples for one thread: self time and inclusive time per method.
usage: jfr print --json --events jdk.ExecutionSample rec.jfr > s.json ; python jfr_hot.py s.json "Server thread" [from_hhmmss] [to_hhmmss]"""
import json, sys, collections
data = json.load(open(sys.argv[1], encoding='utf-8'))
thread = sys.argv[2]
t_from = sys.argv[3] if len(sys.argv) > 3 else None
t_to = sys.argv[4] if len(sys.argv) > 4 else None
self_c, incl_c = collections.Counter(), collections.Counter()
n = 0
for ev in data['recording']['events']:
    v = ev['values']
    if v['sampledThread']['javaName'] != thread:
        continue
    t = v['startTime'][11:19]
    if t_from and t < t_from: continue
    if t_to and t > t_to: continue
    frames = v['stackTrace']['frames'] if v['stackTrace'] else []
    if not frames: continue
    n += 1
    def name(f):
        m = f['method']; return m['type']['name'].split('.')[-1] + '.' + m['name']
    self_c[name(frames[0])] += 1
    for m in set(name(f) for f in frames):
        incl_c[m] += 1
print(f'samples: {n}')
print('--- self ---')
for m, c in self_c.most_common(30): print(f'{100*c/n:6.2f}%  {m}')
print('--- inclusive ---')
for m, c in incl_c.most_common(60): print(f'{100*c/n:6.2f}%  {m}')
