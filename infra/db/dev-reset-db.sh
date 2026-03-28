#!/usr/bin/env bash
set -euo pipefail

# This script resets the development database by removing the Postgres container and its data volume.
# It is intentionally destructive and must only be used for local development.

SCRIPT_DIR="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd -- "${SCRIPT_DIR}/../.." && pwd)"
COMPOSE_FILE="${REPO_ROOT}/infra/compose/compose.yaml"

podman compose -f "${COMPOSE_FILE}" down --remove-orphans || true
podman rm -f placemat-postgres || true

# Volume name is derived from the compose file ("placemat_pgdata").
# Depending on the compose provider, the actual stored name is often prefixed (e.g. "compose_placemat_pgdata").
podman volume rm placemat_pgdata 2>/dev/null || true
podman volume rm compose_placemat_pgdata 2>/dev/null || true

podman compose -f "${COMPOSE_FILE}" up -d
echo "Dev database reset done. Start the backend to re-run Flyway and seeders."
