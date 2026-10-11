#!/usr/bin/env sh
set -eu

MAVEN_VERSION=3.9.9
MAVEN_BASE="${TMPDIR:-/tmp}/courseproject-maven-${MAVEN_VERSION}"
MAVEN_HOME="${MAVEN_BASE}/apache-maven-${MAVEN_VERSION}"

if [ ! -x "${MAVEN_HOME}/bin/mvn" ]; then
  mkdir -p "${MAVEN_BASE}"
  ARCHIVE="${MAVEN_BASE}/apache-maven-${MAVEN_VERSION}-bin.zip"
  curl -fsSL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.zip" -o "${ARCHIVE}"
  unzip -q -o "${ARCHIVE}" -d "${MAVEN_BASE}"
  rm -f "${ARCHIVE}"
fi

exec "${MAVEN_HOME}/bin/mvn" "$@"
