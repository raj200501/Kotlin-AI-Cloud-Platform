#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

"$ROOT_DIR/scripts/java_env.sh"
source "$ROOT_DIR/scripts/java_env.sh"

"$ROOT_DIR/scripts/bootstrap_gradle_wrapper.sh"

cd "$ROOT_DIR"

echo "==> Running demo"
./gradlew run --console=plain --args="--demo"
