#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

"$ROOT_DIR/scripts/java_env.sh"
source "$ROOT_DIR/scripts/java_env.sh"

cd "$ROOT_DIR"

echo "==> Environment diagnostics"
java -version

"$ROOT_DIR/scripts/bootstrap_gradle_wrapper.sh"

./gradlew --version

echo "==> Feature toggle inspection"
./gradlew run --console=plain --args="--explain"
