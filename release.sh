#!/usr/bin/env bash
#
# Releases rings: first runs common's release.sh, then tags master with the
# pom.xml version, pushes the tag, deploys with mvn deploy -DskipTests, and
# moves every pom (and <common.version>) to the next version. Exits 0 without
# changing anything when there is nothing new since the last release.
#
# The procedure and its options (--dry-run, --force, --next-version) are in
# common's scripts/release-lib.sh. The common checkout is expected beside this
# one; set COMMON_DIR to use another location.

set -euo pipefail

RELEASE_REPO_DIR=$(cd "$(dirname "$0")" && pwd -P)
COMMON_DIR=$(cd "${COMMON_DIR:-$RELEASE_REPO_DIR/../common}" && pwd -P)
export COMMON_DIR

RELEASE_UPSTREAM=$COMMON_DIR
RELEASE_DEPENDENCIES="common.version=$COMMON_DIR"

. "$COMMON_DIR/scripts/release-lib.sh"
release_main "$@"
