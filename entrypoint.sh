#!/bin/sh
set -e

# Rootless runtime: user and writable dirs are prepared at image build time.
# For mounted volumes, surface permission issues early with a clear warning.
mkdir -p /app/data /bookdrop 2>/dev/null || true

CURRENT_UID="$(id -u)"
CURRENT_GID="$(id -g)"

if [ ! -w /app/data ] || [ ! -w /bookdrop ]; then
    echo "Warning: /app/data or /bookdrop is not writable by the current non-root user (uid=${CURRENT_UID}, gid=${CURRENT_GID})." >&2
    echo "Ensure host mounts are writable by uid/gid ${CURRENT_UID}:${CURRENT_GID} or set ACLs accordingly." >&2
fi

exec "$@"
