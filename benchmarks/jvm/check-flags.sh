#!/usr/bin/env bash
# Checks each JVM flag on its own against a given JVM:
#   OK      - JVM starts, no warning about the flag
#   WARN    - JVM starts but prints a warning (obsolete / ignored / not supported here)
#   FAIL    - JVM refuses to start with this flag
# usage: check-flags.sh <java-binary> <flags-file> > report.tsv
set -u
JAVA="$1"
FLAGS_FILE="$2"

# options that need an unlock switch to be recognized at all
UNLOCK="-XX:+UnlockExperimentalVMOptions -XX:+UnlockDiagnosticVMOptions"

printf 'flag\tresult\tmessage\n'
tr ' ' '\n' < "$FLAGS_FILE" | sed '/^$/d' | while read -r flag; do
  case "$flag" in
    -XX:+UnlockExperimentalVMOptions|-XX:+UnlockDiagnosticVMOptions) printf '%s\tOK\tunlock switch\n' "$flag"; continue;;
  esac
  # SoftMaxHeapSize in the original set is a shell expression; test with a concrete value
  flag="${flag//\$((Memory - 2048))M/6144M}"
  extra=""
  case "$flag" in -Djdk.graal.*) extra="-XX:+EagerJVMCI";; esac
  out=$("$JAVA" $UNLOCK -Xmx8g $extra "$flag" -version 2>&1)
  rc=$?
  msg=$(printf '%s' "$out" | grep -iE 'warning|error|unrecognized|could not|obsolete|ignoring|not supported|invalid|improperly' | head -1 | tr '\t' ' ')
  if [ $rc -ne 0 ]; then
    printf '%s\tFAIL\t%s\n' "$flag" "${msg:-exit $rc}"
  elif [ -n "$msg" ]; then
    printf '%s\tWARN\t%s\n' "$flag" "$msg"
  else
    printf '%s\tOK\t\n' "$flag"
  fi
done
