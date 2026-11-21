"""Сколько времени уходит до запуска main-класса ядра (работа загрузчика) на тёплом запуске."""
import subprocess
import sys
import time

JAVA = r'C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe'
jar, directory, marker = sys.argv[1], sys.argv[2], sys.argv[3]
times = []
for _ in range(int(sys.argv[4]) if len(sys.argv) > 4 else 3):
    begin = time.time()
    proc = subprocess.Popen([JAVA, '-Xmx2G', '-jar', jar, '--nogui'], cwd=directory, stdin=subprocess.PIPE,
                            stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True, encoding='cp1251', errors='replace')
    for line in proc.stdout:
        if marker in line:
            times.append(round(time.time() - begin, 2))
            proc.kill()
            break
    proc.wait()
print(times)
