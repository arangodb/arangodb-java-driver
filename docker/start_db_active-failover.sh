#!/bin/bash

# USAGE:
#   export ARANGO_LICENSE_KEY=<arangodb-enterprise-license>
#   ./start_active-failover.sh <dockerImage>

# EXAMPLE:
#   ./start_active-failover.sh docker.io/arangodb/arangodb:3.7.10

docker pull "$1"

LOCATION=$(pwd)/$(dirname "$0")

docker network create arangodb --subnet 172.28.0.0/16

echo "Averysecretword" >"$LOCATION"/jwtSecret
docker run --rm -v "$LOCATION"/jwtSecret:/jwtSecret "$1" arangodb auth header --auth.jwt-secret /jwtSecret >"$LOCATION"/jwtHeader
AUTHORIZATION_HEADER=$(cat "$LOCATION"/jwtHeader)

echo "Starting containers..."

docker run -d -v "$LOCATION"/jwtSecret:/jwtSecret -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 --name server1 "$1" sh -c 'arangodb --starter.address=$(hostname -i) --starter.mode=activefailover --starter.join server1,server2,server3 --auth.jwt-secret /jwtSecret'
docker run -d -v "$LOCATION"/jwtSecret:/jwtSecret -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.2 --name server2 "$1" sh -c 'arangodb --starter.address=$(hostname -i) --starter.mode=activefailover --starter.join server1,server2,server3 --auth.jwt-secret /jwtSecret'
docker run -d -v "$LOCATION"/jwtSecret:/jwtSecret -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.3 --name server3 "$1" sh -c 'arangodb --starter.address=$(hostname -i) --starter.mode=activefailover --starter.join server1,server2,server3 --auth.jwt-secret /jwtSecret'

debug_container() {
  running=$(docker inspect -f '{{.State.Running}}' "$1")

  if [ "$running" = false ]; then
    echo "$1 is not running!"
    echo "---"
    docker logs "$1"
    echo "---"
    exit 1
  fi
}

debug() {
  for c in server1 \
    server2 \
    server3; do
    debug_container $c
  done
}

wait_server() {
  # shellcheck disable=SC2091
  until $(curl --output /dev/null --silent --head --fail -i -H "$AUTHORIZATION_HEADER" "http://$1/_api/version"); do
    printf '.'
    debug
    sleep 1
  done
}

echo "Waiting..."

# Wait for agents:
for a in 172.28.3.1:8529 \
  172.28.3.2:8529 \
  172.28.3.3:8529; do
  wait_server $a
done

docker exec server1 arangosh --server.authentication=false --javascript.execute-string='require("org/arangodb/users").update("root", "test")'
docker exec server2 arangosh --server.authentication=false --javascript.execute-string='require("org/arangodb/users").update("root", "test")'
docker exec server3 arangosh --server.authentication=false --javascript.execute-string='require("org/arangodb/users").update("root", "test")'

#rm "$LOCATION"/jwtHeader "$LOCATION"/jwtSecret

echo "Done, your cluster is ready."
