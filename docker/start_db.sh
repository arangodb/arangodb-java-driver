#!/bin/bash

# Configuration environment variables:
#   STARTER_MODE:             (single|cluster|activefailover), default single
#   DOCKER_IMAGE:             ArangoDB docker image, default gcr.io/gcr-for-testing/arangodb/arangodb:latest
#   SSL:                      (true|false), default false
#   DATABASE_EXTENDED_NAMES:  (true|false), default false
#   ARANGO_LICENSE_KEY:       only required for ArangoDB Enterprise

# EXAMPLE:
# STARTER_MODE=cluster SSL=true ./start_db.sh

STARTER_MODE=${STARTER_MODE:=single}
DOCKER_IMAGE=${DOCKER_IMAGE:=gcr.io/gcr-for-testing/arangodb/arangodb:latest}
SSL=${SSL:=false}
DATABASE_EXTENDED_NAMES=${DATABASE_EXTENDED_NAMES:=false}

STARTER_DOCKER_IMAGE=gcr.io/gcr-for-testing/arangodb/arangodb-starter:latest

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
COORDINATORS=("172.17.0.1:8529" "172.17.0.1:8539" "172.17.0.1:8549")

if [ "$STARTER_MODE" == "single" ]; then
  COORDINATORS=("172.17.0.1:8529")
fi

if [ "$SSL" == "true" ]; then
    STARTER_ARGS="$STARTER_ARGS --ssl.keyfile=server.pem"
    SCHEME=https
    ARANGOSH_SCHEME=http+ssl
fi

if [ "$DATABASE_EXTENDED_NAMES" == "true" ]; then
    STARTER_ARGS="${STARTER_ARGS} --all.database.extended-names-databases=true"
fi

docker run -d \
    --name=adb \
    -p 8528:8528 \
    -v "$LOCATION"/server.pem:/server.pem \
    -v "$LOCATION"/jwtSecret:/jwtSecret \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" \
    $STARTER_DOCKER_IMAGE \
    $STARTER_ARGS \
    --docker.container=adb \
    --auth.jwt-secret=/jwtSecret \
    --starter.address=172.17.0.1 \
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
    docker run --rm ${DOCKER_IMAGE} arangosh --server.endpoint="$ARANGOSH_SCHEME://$a" --server.authentication=false --javascript.execute-string='require("org/arangodb/users").update("root", "test")'
done
set -e

for a in ${COORDINATORS[*]} ; do
    echo ""
    curl -u root:test --insecure --fail "$SCHEME://$a/_api/version"
done

echo ""
echo ""
echo "Done, your deployment is reachable at: "
for a in ${COORDINATORS[*]} ; do
    echo "$SCHEME://$a"
    echo ""
done
