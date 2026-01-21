#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
"$ROOT_DIR/scripts/java_env.sh"
source "$ROOT_DIR/scripts/java_env.sh"

scripts/bootstrap_gradle_wrapper.sh

./gradlew build --console=plain

./gradlew run --console=plain > /tmp/kaicp-smoke.log 2>&1 &
APP_PID=$!
trap 'kill $APP_PID >/dev/null 2>&1 || true' EXIT

for _ in {1..10}; do
  if curl -sf http://localhost:8080/users >/dev/null; then
    break
  fi
  sleep 1
done

curl -sf -X POST -H "Content-Type: application/json" \
  -d '{"name":"Smoke","email":"smoke@example.com"}' http://localhost:8080/users >/tmp/kaicp-user.json
curl -sf http://localhost:8080/users >/tmp/kaicp-users.json

echo "Smoke test created user: $(cat /tmp/kaicp-user.json)"
echo "Current users payload: $(cat /tmp/kaicp-users.json)"
