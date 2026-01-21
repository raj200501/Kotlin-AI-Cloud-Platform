#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

"$ROOT_DIR/scripts/java_env.sh"
source "$ROOT_DIR/scripts/java_env.sh"

"$ROOT_DIR/scripts/bootstrap_gradle_wrapper.sh"

cd "$ROOT_DIR"

echo "==> Running unit tests"
./gradlew test --console=plain

echo "==> Running build"
./gradlew assemble --console=plain

echo "==> Running smoke test"
./scripts/smoke_test.sh

if [ -x "$ROOT_DIR/scripts/lint.sh" ]; then
  echo "==> Running lint"
  "$ROOT_DIR/scripts/lint.sh"
fi
