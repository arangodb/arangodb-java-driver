#!/usr/bin/env bash
# Start ArangoDB with RBAC and show a live policy change for a disposable user.
set -euo pipefail

ARANGO_USERNAME="rbac-demo"
ARANGO_PASSWORD="test"
ARANGO_URL="http://172.28.0.1:8529"
RBAC_ADMIN_TOKEN="driver-rbac-test-only"
HERE=$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)
REPO_ROOT=$(cd -- "$HERE/.." && pwd)
source "$REPO_ROOT/docker/rbac_common.sh"
rbac_configure_host

echo "Starting the RBAC server"
"$REPO_ROOT/docker/start_rbac.sh"

echo "Starting ArangoDB with external RBAC"
RBAC=true DOCKER_IMAGE=docker.io/arangodb/enterprise:3.12.11 \
    "$REPO_ROOT/docker/start_db.sh"

echo "Creating $ARANGO_USERNAME with root access"
root_jwt=$(curl --noproxy '*' --silent --show-error --fail-with-body --max-time 10 \
    -H 'Content-Type: application/json' \
    --data '{"username":"root","password":"test"}' \
    "$ARANGO_URL/_open/auth" |
    python3 -c 'import json,sys; print(json.load(sys.stdin)["jwt"])')
curl --noproxy '*' --silent --show-error --fail-with-body --max-time 10 \
    -H "Authorization: Bearer $root_jwt" -H 'Content-Type: application/json' \
    --data "{\"user\":\"$ARANGO_USERNAME\",\"passwd\":\"$ARANGO_PASSWORD\",\"active\":true}" \
    "$ARANGO_URL/_api/user" >/dev/null
curl --noproxy '*' --silent --show-error --fail-with-body --max-time 10 \
    -H "X-Rbac-Test-Admin: $RBAC_ADMIN_TOKEN" -H 'Content-Type: application/json' \
    -X PUT \
    --data "{\"username\":\"$ARANGO_USERNAME\",\"policy\":{\"defaultEffect\":\"Allow\",\"rules\":[]}}" \
    'http://127.0.0.1:18081/_test/policy' >/dev/null

echo "Obtaining a JWT for $ARANGO_USERNAME"
jwt=$(curl --noproxy '*' --silent --show-error --fail-with-body --max-time 10 \
    -H 'Content-Type: application/json' \
    -X POST \
    --data "{\"username\":\"$ARANGO_USERNAME\",\"password\":\"$ARANGO_PASSWORD\"}" \
    "$ARANGO_URL/_open/auth" |
    python3 -c 'import json,sys; print(json.load(sys.stdin)["jwt"])')
[ -n "$jwt" ] || { echo "ArangoDB returned an empty JWT" >&2; exit 1; }

echo "Using that JWT to GET /_api/version (expected: success)"
curl --noproxy '*' --silent --show-error --fail-with-body --max-time 10 \
    -H "Authorization: Bearer $jwt" \
    "$ARANGO_URL/_api/version"
echo

echo "Changing $ARANGO_USERNAME to default-deny, allowing only API-version access"
curl --noproxy '*' --silent --show-error --fail-with-body --max-time 10 \
    -H "X-Rbac-Test-Admin: $RBAC_ADMIN_TOKEN" \
    -H 'Content-Type: application/json' \
    -X PUT \
    --data "{\"username\":\"$ARANGO_USERNAME\",\"policy\":{\"defaultEffect\":\"Deny\",\"rules\":[{\"action\":\"db:UseApiVersion\",\"resource\":\"*\",\"effect\":\"Allow\"}]}}" \
    "http://127.0.0.1:18081/_test/policy" >/dev/null

echo "Reusing the same JWT to GET /_api/version (expected: RBAC denial)"
curl --noproxy '*' --silent --show-error --max-time 10 \
    -H "Authorization: Bearer $jwt" \
    "$ARANGO_URL/_api/version"

echo
echo "DONE"
