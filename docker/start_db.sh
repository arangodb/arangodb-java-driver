#!/bin/bash

# Configuration environment variables:
#   STARTER_MODE:             (single|cluster), default single
#   DOCKER_IMAGE:             ArangoDB docker image, default docker.io/arangodb/enterprise:latest
#   TOOLS_DOCKER_IMAGE:       ArangoDB client-tools docker image, default docker.io/arangodb/client-tools-preview:4-nightly
#   STARTER_DOCKER_IMAGE:     ArangoDB Starter docker image
#   SSL:                      (true|false), default false
#   ARANGO_LICENSE_KEY:       only required for ArangoDB Enterprise
#   RBAC:                     (true|false), default false; demo uses a local RBAC server
# EXAMPLE:
# STARTER_MODE=cluster SSL=true ./start_db.sh
set -e
STARTER_MODE=${STARTER_MODE:=single}
RBAC=${RBAC:-false}
case "$RBAC" in true|false) ;; *) echo "RBAC must be true or false" >&2; exit 1 ;; esac
LOCATION=$(cd -- "$(dirname -- "$0")" && pwd)
if [ "$RBAC" == true ]; then
    [ "$STARTER_MODE" = single ] || { echo "RBAC demo requires single mode" >&2; exit 1; }
    source "$LOCATION/rbac_common.sh"
    rbac_configure_host
    rbac_require_running || { echo "Local RBAC server is not ready. Run docker/start_rbac.sh." >&2; exit 1; }
fi
DOCKER_IMAGE=${DOCKER_IMAGE:=docker.io/arangodb/enterprise:latest}
STARTER_VERSION=$(docker run --rm -e ARANGO_NO_AUTH=1 --entrypoint arangodb ${DOCKER_IMAGE} --version | { read -r first rest; echo "${rest%%,*}"; })
ARANGO_VERSION=$(docker run --rm --entrypoint arangod ${DOCKER_IMAGE} --version | awk '/^server-version:/ {print $2}')
ARANGO_MAJOR_VERSION=$(echo "$ARANGO_VERSION" | cut -d'.' -f1)
ARANGO_MINOR_VERSION=$(echo "$ARANGO_VERSION" | cut -d'.' -f1,2)
echo "arangod version: $ARANGO_VERSION"
echo "arangod major version: $ARANGO_MAJOR_VERSION"
echo "arangod minor version: $ARANGO_MINOR_VERSION"
STARTER_DOCKER_IMAGE=${STARTER_DOCKER_IMAGE:=docker.io/arangodb/arangodb-starter:$STARTER_VERSION}
TOOLS_DOCKER_IMAGE=${TOOLS_DOCKER_IMAGE:=docker.io/arangodb/client-tools-preview:4-nightly}

if [ "$ARANGO_MAJOR_VERSION" == "3" ]; then
  TOOLS_DOCKER_IMAGE=$DOCKER_IMAGE
fi

echo "starter docker image: $STARTER_DOCKER_IMAGE"
echo "tools docker image: $TOOLS_DOCKER_IMAGE"
SSL=${SSL:=false}
COMPRESSION=${COMPRESSION:=false}

GW=172.28.0.1
if ! docker network inspect arangodb >/dev/null 2>&1; then
    docker network create arangodb --subnet 172.28.0.0/16 --gateway "$GW"
fi

# exit when any command fails
set -e

docker pull $STARTER_DOCKER_IMAGE
docker pull $DOCKER_IMAGE
docker pull $TOOLS_DOCKER_IMAGE

AUTHORIZATION_HEADER=$(cat "$LOCATION"/jwtHeader)

RBAC_ARGS=()
if [ "$RBAC" == true ]; then
    RBAC_ARGS+=("--all.server.external-rbac-service=$RBAC_URL")
fi
STARTER_ARGS=
SCHEME=http
ARANGOSH_SCHEME=http+tcp
COORDINATORS=("$GW:8529" "$GW:8539" "$GW:8549")

if [ "$STARTER_MODE" == "single" ]; then
  COORDINATORS=("$GW:8529")
fi

if [ "$SSL" == "true" ]; then
    STARTER_ARGS="$STARTER_ARGS --ssl.keyfile=/data/server.pem"
    SCHEME=https
    ARANGOSH_SCHEME=http+ssl
fi

if [ "$COMPRESSION" == "true" ]; then
    STARTER_ARGS="${STARTER_ARGS} --all.http.compress-response-threshold=1"
fi

if [ "$ARANGO_MINOR_VERSION" == "3.12" ]; then
    STARTER_ARGS="${STARTER_ARGS} --all.experimental-vector-index=true"
fi

# data volume
docker create -v /data --name arangodb-data alpine:3 /bin/true
docker cp "$LOCATION"/jwtSecret arangodb-data:/data
docker cp "$LOCATION"/server.pem arangodb-data:/data

docker run -d \
    --name=adb \
    -p 8528:8528 \
    --volumes-from arangodb-data \
    -v /var/run/docker.sock:/var/run/docker.sock \
    --security-opt label=disable \
    -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" \
    $STARTER_DOCKER_IMAGE \
    $STARTER_ARGS \
    "${RBAC_ARGS[@]}" \
    --docker.net-mode=default \
    --docker.container=adb \
    --auth.jwt-secret=/data/jwtSecret \
    --starter.address="${GW}" \
    --docker.image="${DOCKER_IMAGE}" \
    --starter.local --starter.mode=${STARTER_MODE} --all.log.level=debug --all.log.output=+ --log.verbose \
    --all.server.descriptors-minimum=1024 --all.javascript.allow-admin-execute=true --all.server.maximal-threads=128 \
    --all.query.tracking=true --all.query.tracking-slow-queries=true


wait_server() {
    local deadline=$((SECONDS + 180))
    until curl --output /dev/null --insecure --fail --silent --max-time 3 \
        -H "$AUTHORIZATION_HEADER" "$SCHEME://$1/_api/version"; do
        if (( SECONDS >= deadline )); then
            echo "Database readiness timed out for $1. Inspect docker logs adb and the RBAC service logs." >&2
            return 1
        fi
        printf '.'
        sleep 1
    done
}

echo "Waiting..."

for a in ${COORDINATORS[*]} ; do
    wait_server "$a"
done

if [ "$RBAC" == true ]; then
    # Set the root password with the bootstrap JWT. The demo signs in afterward.
    curl --silent --show-error --insecure --fail --max-time 15 \
        -H "$AUTHORIZATION_HEADER" -H 'Content-Type: application/json' \
        -X PATCH -d '{"passwd":"test"}' \
        "$SCHEME://${COORDINATORS[0]}/_db/_system/_api/user/root" >/dev/null
else
    set +e
    for a in ${COORDINATORS[*]} ; do
        echo ""
        echo "Setting username and password..."
        docker run --rm ${TOOLS_DOCKER_IMAGE} arangosh --server.endpoint="$ARANGOSH_SCHEME://$a" --server.authentication=false --javascript.execute-string='require("org/arangodb/users").update("root", "test")'
    done
    set -e

    for a in ${COORDINATORS[*]} ; do
        echo ""
        echo "Requesting endpoint version..."
        curl -u root:test --insecure --fail "$SCHEME://$a/_api/version"
    done
fi

echo ""
echo ""
echo "Copying test ML models into containers..."
for c in $(docker ps -a -f name=adb-.* -q) ; do
    docker cp "$LOCATION"/foo.bin "$c":/tmp
done

echo ""
echo ""
echo "Done, your deployment is reachable at: "
for a in ${COORDINATORS[*]} ; do
    echo "$SCHEME://$a"
    echo ""
done
