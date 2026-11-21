#!/usr/bin/env bash
# Свежий прогон через общий Python-стенд. Старые миры и результаты не удаляются.
set -euo pipefail
if [[ "${1:-}" == "--help" || $# -lt 5 ]]; then
  echo 'Использование: run-bench.sh <метка> <server.jar> <java> "<JVM-флаги>" "<сценарии>" [клиенты] [каталог конфигов]'
  echo 'Переменные: RUN_ROOT, TEMPLATE, BOTS_DIR, HEAP, TIMEOUT, PYTHON.'
  [[ "${1:-}" == "--help" ]] && exit 0
  exit 2
fi
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPOSITORY="$(cd "$SCRIPT_DIR/.." && pwd)"
if [[ -d /d/water-mc/run ]]; then DEFAULT_ROOT=/d/water-mc/run; else DEFAULT_ROOT="$REPOSITORY/build/benchmark-runs"; fi
RUN_ROOT="${RUN_ROOT:-$DEFAULT_ROOT}"
if [[ -d /d/water-mc/bench/bots ]]; then DEFAULT_BOTS=/d/water-mc/bench/bots; else DEFAULT_BOTS="$SCRIPT_DIR/bots"; fi
if [[ "$OSTYPE" == msys* || "$OSTYPE" == cygwin* ]]; then DEFAULT_PYTHON=python; else DEFAULT_PYTHON=python3; fi
PYTHON_BIN="${PYTHON:-$DEFAULT_PYTHON}"
if ! command -v "$PYTHON_BIN" >/dev/null 2>&1; then PYTHON_BIN=python; fi
arguments=("$SCRIPT_DIR/run-native.py" "$1" --jar "$2" --java "$3" "--jvm-flags=$4" --spec "$5"
  --bots "${6:-0}" --root "$RUN_ROOT" --template "${TEMPLATE:-$RUN_ROOT/base}"
  --node-modules "${BOTS_DIR:-$DEFAULT_BOTS}/node_modules" --heap "${HEAP:-4G}" --timeout "${TIMEOUT:-7200}"
  --plugin "$SCRIPT_DIR/waterbench/build/libs/waterbench-1.0.0.jar")
if [[ -n "${7:-}" ]]; then arguments+=(--overrides "$7"); fi
exec "$PYTHON_BIN" "${arguments[@]}"
