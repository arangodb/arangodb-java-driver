#!/bin/bash

# Configuration environment variables:
#   STARTER_MODE:             (single|cluster|activefailover), default single
#   DOCKER_IMAGE:             ArangoDB docker image, default docker.io/arangodb/arangodb:latest
#   SSL:                      (true|false), default false
#   DATABASE_EXTENDED_NAMES:  (true|false), default false
#   ARANGO_LICENSE_KEY:       only required for ArangoDB Enterprise

# EXAMPLE:
# STARTER_MODE=cluster SSL=true ./start_db.sh

STARTER_MODE=${STARTER_MODE:=single}
DOCKER_IMAGE=${DOCKER_IMAGE:=docker.io/arangodb/arangodb:latest}
SSL=${SSL:=false}
DATABASE_EXTENDED_NAMES=${DATABASE_EXTENDED_NAMES:=false}

STARTER_DOCKER_IMAGE=docker.io/arangodb/arangodb-starter:latest
GW=172.28.0.1
docker network create arangodb --subnet 172.28.0.0/16

# exit when any command fails
set -e

docker pull $STARTER_DOCKER_IMAGE
docker pull $DOCKER_IMAGE

LOCATION=$(pwd)/$(dirname "$0")

echo "Averysecretword" > "$LOCATION"/jwtSecret
docker run --rm -v "$LOCATION"/jwtSecret:/jwtSecret "$STARTER_DOCKER_IMAGE" auth header --auth.jwt-secret /jwtSecret > "$LOCATION"/jwtHeader
AUTHORIZATION_HEADER=$(cat "$LOCATION"/jwtHeader)

STARTER_ARGS=
SCHEME=http
ARANGOSH_SCHEME=http+tcp
COORDINATORS=("$GW:8529" "$GW:8539" "$GW:8549")

if [ "$STARTER_MODE" == "single" ]; then
  COORDINATORS=("$GW:8529")
fi

if [ "$SSL" == "true" ]; then
    STARTER_ARGS="$STARTER_ARGS --ssl.keyfile=server.pem"
    SCHEME=https
    ARANGOSH_SCHEME=http+ssl
fi

if [ "$DATABASE_EXTENDED_NAMES" == "true" ]; then
    STARTER_ARGS="${STARTER_ARGS} --all.database.extended-names-databases=true"
fi

if [ "$USE_MOUNTED_DATA" == "true" ]; then
    STARTER_ARGS="${STARTER_ARGS} --starter.data-dir=/data"
    MOUNT_DATA="-v $LOCATION/data:/data"
    echo $MOUNT_DATA
fi

docker run -d \
    --name=adb \
    -p 8528:8528 \
    -v "$LOCATION"/server.pem:/server.pem \
    -v "$LOCATION"/jwtSecret:/jwtSecret \
    $MOUNT_DATA \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" \
    $STARTER_DOCKER_IMAGE \
    $STARTER_ARGS \
    --docker.container=adb \
    --auth.jwt-secret=/jwtSecret \
    --starter.address="${GW}" \
    --docker.image="${DOCKER_IMAGE}" \
    --starter.local --starter.mode=${STARTER_MODE} --all.log.level=debug --all.log.output=+ --log.verbose


wait_server() {
    # shellcheck disable=SC2091
    until $(curl --output /dev/null --insecure --fail --silent --head -i -H "$AUTHORIZATION_HEADER" "$SCHEME://$1/_api/version"); do
        printf '.'
        sleep 1
    done
}

echo "Waiting..."

for a in ${COORDINATORS[*]} ; do
    wait_server "$a"
done

set +e
for a in ${COORDINATORS[*]} ; do
    echo ""
    echo "Setting username and password..."
    docker run --rm ${DOCKER_IMAGE} arangosh --server.endpoint="$ARANGOSH_SCHEME://$a" --server.authentication=false --javascript.execute-string='require("org/arangodb/users").update("root", "test")'
done
set -e

for a in ${COORDINATORS[*]} ; do
    echo ""
    echo "Requesting endpoint version..."
    curl -u root:test --insecure --fail "$SCHEME://$a/_api/version"
done

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

if [ "$STARTER_MODE" == "activefailover" ]; then
  LEADER=$("$LOCATION"/find_active_endpoint.sh)
  echo "Leader: $SCHEME://$LEADER"
  echo ""
fi
