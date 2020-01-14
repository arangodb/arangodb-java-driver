#!/bin/bash

# USAGE:
#   export ARANGO_LICENSE_KEY=<arangodb-enterprise-license>
#   ./start_db_cluster.sh <dockerImage>

# EXAMPLE:
#   ./start_db_cluster.sh docker.io/arangodb/arangodb:3.5.3

docker pull "$1"

LOCATION=$(pwd)/$(dirname "$0")

docker network create arangodb --subnet 172.28.0.0/16

echo "Averysecretword" > "$LOCATION"/jwtSecret
docker run --rm -v "$LOCATION"/jwtSecret:/jwtSecret "$1" arangodb auth header --auth.jwt-secret /jwtSecret > "$LOCATION"/jwtHeader
AUTHORIZATION_HEADER=$(cat "$LOCATION"/jwtHeader)

echo "Starting containers..."

docker run -d -v "$LOCATION"/jwtSecret:/jwtSecret -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.1.1 --name agent1 "$1" arangodb --cluster.start-dbserver false --cluster.start-coordinator false --auth.jwt-secret /jwtSecret
docker run -d -v "$LOCATION"/jwtSecret:/jwtSecret -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.1.2 --name agent2 "$1" arangodb --cluster.start-dbserver false --cluster.start-coordinator false --starter.join agent1 --auth.jwt-secret /jwtSecret
docker run -d -v "$LOCATION"/jwtSecret:/jwtSecret -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.1.3 --name agent3 "$1" arangodb --cluster.start-dbserver false --cluster.start-coordinator false --starter.join agent1 --auth.jwt-secret /jwtSecret

docker run -d -v "$LOCATION"/jwtSecret:/jwtSecret -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.2.1 --name dbserver1 "$1" arangodb --cluster.start-dbserver true --cluster.start-coordinator false --starter.join agent1 --auth.jwt-secret /jwtSecret
docker run -d -v "$LOCATION"/jwtSecret:/jwtSecret -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.2.2 --name dbserver2 "$1" arangodb --cluster.start-dbserver true --cluster.start-coordinator false --starter.join agent1 --auth.jwt-secret /jwtSecret

docker run -d -v "$LOCATION"/jwtSecret:/jwtSecret -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 --name coordinator1 "$1" arangodb --cluster.start-dbserver false --cluster.start-coordinator true --starter.join agent1 --auth.jwt-secret /jwtSecret
docker run -d -v "$LOCATION"/jwtSecret:/jwtSecret -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.2 --name coordinator2 "$1" arangodb --cluster.start-dbserver false --cluster.start-coordinator true --starter.join agent1 --auth.jwt-secret /jwtSecret

debug_container() {
  running=$(docker inspect -f '{{.State.Running}}' "$1")

  if [ "$running" = false ]
  then
    echo "$1 is not running!"
    echo "---"
    docker logs "$1"
    echo "---"
    exit 1
  fi
}

debug() {
  for c in agent1 \
           agent2 \
           agent3 \
           dbserver1 \
           dbserver2 \
           coordinator1 \
           coordinator2 ; do
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
for a in 172.28.1.1:8531 \
         172.28.1.2:8531 \
         172.28.1.3:8531 \
         172.28.2.1:8530 \
         172.28.2.2:8530 \
         172.28.3.1:8529 \
         172.28.3.2:8529 ; do
    wait_server $a
done

docker exec coordinator1 arangosh --server.authentication=false --javascript.execute-string='require("org/arangodb/users").update("root", "test")'

#rm "$LOCATION"/jwtHeader "$LOCATION"/jwtSecret

echo "Done, your cluster is ready."
