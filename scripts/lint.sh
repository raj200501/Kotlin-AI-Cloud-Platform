#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

"$ROOT_DIR/scripts/java_env.sh"
source "$ROOT_DIR/scripts/java_env.sh"

cd "$ROOT_DIR"

echo "==> Running basic Gradle check"
./gradlew check --console=plain
