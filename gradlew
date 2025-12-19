#!/usr/bin/env bash
set -euo pipefail

DIR="$(cd "$(dirname "$0")" && pwd)"
BOOTSTRAP_SCRIPT="$DIR/scripts/bootstrap_gradle_wrapper.sh"
WRAPPER_DIR="$DIR/gradle/wrapper"
JAVA_BIN="${JAVA_HOME:-}/bin/java"

if [ -x "$BOOTSTRAP_SCRIPT" ]; then
  "$BOOTSTRAP_SCRIPT" || echo "Gradle wrapper bootstrap failed; attempting to continue with any available Gradle installation."
fi

if command -v gradle >/dev/null 2>&1; then
  cd "$DIR"
  exec gradle "$@"
fi

shopt -s nullglob
WRAPPER_LIBS=("$WRAPPER_DIR"/gradle-wrapper*.jar)
shopt -u nullglob

if [ ${#WRAPPER_LIBS[@]} -eq 0 ]; then
  echo "No Gradle wrapper jars found in $WRAPPER_DIR" >&2
  echo "Please run scripts/bootstrap_gradle_wrapper.sh to download them." >&2
  exit 1
fi

if [ -x "$JAVA_BIN" ]; then
  JAVA_CMD="$JAVA_BIN"
elif command -v java >/dev/null 2>&1; then
  JAVA_CMD="$(command -v java)"
else
  echo "Java is required to run Gradle but was not found in PATH." >&2
  exit 1
fi

CLASSPATH=$(IFS=:; echo "${WRAPPER_LIBS[*]}")
cd "$DIR"
exec "$JAVA_CMD" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
