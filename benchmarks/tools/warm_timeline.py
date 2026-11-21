"""Хронология тёплого запуска: время от старта процесса до каждой строки консоли, затем stop."""
import subprocess
import sys
import time

JAVA = r'C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe'
jar, directory = sys.argv[1], sys.argv[2]
extra = sys.argv[3:]
begin = time.time()
proc = subprocess.Popen([JAVA, '-Xms2G', '-Xmx2G', *extra, '-jar', jar, '--nogui'], cwd=directory, stdin=subprocess.PIPE,
                        stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, encoding='cp1251', errors='replace')
for line in proc.stdout:
    t = time.time() - begin
    if 'PDH' in line or 'registry if this' in line:
        continue
    print(f'{t:7.2f} {line.rstrip()[:150]}', flush=True)
    if 'Done (' in line:
        proc.stdin.write('stop\n')
        proc.stdin.flush()
proc.wait(timeout=300)
print(f'{time.time() - begin:7.2f} exited {proc.returncode}', flush=True)
