#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
WRAPPER_DIR="$ROOT_DIR/gradle/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/gradle-wrapper.jar"
PROPERTIES_FILE="$WRAPPER_DIR/gradle-wrapper.properties"

if ls "$WRAPPER_DIR"/gradle-wrapper*.jar >/dev/null 2>&1; then
  echo "Gradle wrapper jars already present in $WRAPPER_DIR"
  exit 0
fi

if [ ! -f "$PROPERTIES_FILE" ]; then
  echo "Missing $PROPERTIES_FILE; cannot determine Gradle version" >&2
  exit 1
fi

DIST_URL=$(grep "^distributionUrl" "$PROPERTIES_FILE" | cut -d= -f2- | tr -d '\\')
if [ -z "$DIST_URL" ]; then
  echo "Unable to read distributionUrl from $PROPERTIES_FILE" >&2
  exit 1
fi

VERSION=$(basename "$DIST_URL" | sed -E 's/gradle-([0-9]+\.[0-9]+(\.[0-9]+)?)-bin\.zip/\1/')
if [ -z "$VERSION" ]; then
  echo "Unable to parse Gradle version from distributionUrl: $DIST_URL" >&2
  exit 1
fi

DIRECT_JAR_URL=${GRADLE_WRAPPER_JAR_URL:-https://services.gradle.org/distributions/gradle-${VERSION}-wrapper.jar}
mkdir -p "$WRAPPER_DIR"

download_file() {
  local url=$1
  local dest=$2
  if command -v curl >/dev/null 2>&1; then
    curl -fL "$url" -o "$dest"
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$dest" "$url"
  else
    echo "Neither curl nor wget is available to download files" >&2
    return 1
  fi
}

cleanup_temp() {
  if [ -n "${TMP_DIST_FILE:-}" ] && [ -f "$TMP_DIST_FILE" ]; then
    rm -f "$TMP_DIST_FILE"
  fi
}
trap cleanup_temp EXIT

if [ -n "${GRADLE_WRAPPER_JAR_URL:-}" ]; then
  echo "Downloading Gradle wrapper jar from custom URL: $DIRECT_JAR_URL"
  download_file "$DIRECT_JAR_URL" "$WRAPPER_JAR"
  echo "Gradle wrapper jar downloaded to $WRAPPER_JAR"
  exit 0
fi

echo "Attempting to download Gradle wrapper jar from $DIRECT_JAR_URL..."
if download_file "$DIRECT_JAR_URL" "$WRAPPER_JAR"; then
  echo "Gradle wrapper jar downloaded to $WRAPPER_JAR"
  exit 0
else
  echo "Direct download failed; checking local Gradle installation for wrapper jars" >&2
fi

if command -v gradle >/dev/null 2>&1; then
  GRADLE_BIN_PATH=$(readlink -f "$(command -v gradle)")
  GRADLE_HOME=$(cd "$(dirname "$GRADLE_BIN_PATH")/.." && pwd)
  LOCAL_JARS=$(find "$GRADLE_HOME/lib" -maxdepth 2 -type f -name "gradle-wrapper*${VERSION}*.jar" | tr '\n' ' ' || true)
  if [ -n "${LOCAL_JARS:-}" ]; then
    echo "Copying wrapper jars from local Gradle installation: $LOCAL_JARS"
    for jar in $LOCAL_JARS; do
      cp "$jar" "$WRAPPER_DIR/"
    done
    if [ ! -f "$WRAPPER_JAR" ]; then
      MAIN_JAR=$(ls "$WRAPPER_DIR"/gradle-wrapper-main* 2>/dev/null | head -n1 || true)
      if [ -n "$MAIN_JAR" ]; then
        ln -sf "$(basename "$MAIN_JAR")" "$WRAPPER_JAR"
      fi
    fi
    echo "Gradle wrapper jars copied into $WRAPPER_DIR"
    exit 0
  fi
fi

echo "Falling back to extracting from distribution $DIST_URL" >&2

TMP_DIST_FILE=$(mktemp)

echo "Downloading Gradle distribution to $TMP_DIST_FILE..."
download_file "$DIST_URL" "$TMP_DIST_FILE"

if ! command -v unzip >/dev/null 2>&1; then
  echo "The 'unzip' command is required to extract the wrapper jar from the distribution" >&2
  exit 1
fi

JAR_IN_ZIP="gradle-${VERSION}/lib/plugins/gradle-wrapper-${VERSION}.jar"
echo "Extracting $JAR_IN_ZIP to $WRAPPER_JAR"
if unzip -p "$TMP_DIST_FILE" "$JAR_IN_ZIP" > "$WRAPPER_JAR"; then
  echo "Gradle wrapper jar extracted to $WRAPPER_JAR"
else
  echo "Failed to extract $JAR_IN_ZIP from the distribution; check the Gradle version and distribution URL" >&2
  exit 1
fi
