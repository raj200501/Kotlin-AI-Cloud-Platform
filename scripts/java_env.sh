#!/usr/bin/env bash
set -euo pipefail

if [ -n "${JAVA_HOME:-}" ]; then
  export PATH="$JAVA_HOME/bin:$PATH"
  exit 0
fi

PREFERRED_JAVA_HOMES=(
  "/root/.local/share/mise/installs/java/17.0.2"
  "/root/.local/share/mise/installs/java/17.0"
  "/root/.local/share/mise/installs/java/21.0.2"
  "/root/.local/share/mise/installs/java/21.0"
)

for candidate in "${PREFERRED_JAVA_HOMES[@]}"; do
  if [ -x "$candidate/bin/java" ]; then
    export JAVA_HOME="$candidate"
    export PATH="$JAVA_HOME/bin:$PATH"
    exit 0
  fi
done

if command -v java >/dev/null 2>&1; then
  exit 0
fi

echo "JAVA_HOME is not set and no compatible JDK was found." >&2
exit 1
