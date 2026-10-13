#!/bin/sh
set -eu

MAVEN_VERSION=3.9.9
MAVEN_CACHE="${HOME:?HOME must be set}/.m2/wrapper/dists/apache-maven-${MAVEN_VERSION}"
MAVEN_HOME="${MAVEN_CACHE}/apache-maven-${MAVEN_VERSION}"
MAVEN_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.zip"

if [ ! -x "${MAVEN_HOME}/bin/mvn" ]; then
    command -v curl >/dev/null 2>&1 || {
        echo "curl is required to download Maven ${MAVEN_VERSION}." >&2
        exit 1
    }
    command -v unzip >/dev/null 2>&1 || {
        echo "unzip is required to unpack Maven ${MAVEN_VERSION}." >&2
        exit 1
    }

    mkdir -p "${MAVEN_CACHE}"
    WRAPPER_TMP="$(mktemp -d "${TMPDIR:-/tmp}/exercise-hash-map-maven.XXXXXX")"
    trap 'rm -rf "${WRAPPER_TMP}"' EXIT HUP INT TERM
    curl --fail --location --silent --show-error "${MAVEN_URL}" --output "${WRAPPER_TMP}/maven.zip"
    unzip -q "${WRAPPER_TMP}/maven.zip" -d "${WRAPPER_TMP}"
    rm -rf "${MAVEN_HOME}"
    mv "${WRAPPER_TMP}/apache-maven-${MAVEN_VERSION}" "${MAVEN_HOME}"
fi

exec "${MAVEN_HOME}/bin/mvn" "$@"
