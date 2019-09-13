#!/bin/bash

# USAGE:
#   export ARANGO_LICENSE_KEY=<arangodb-enterprise-license>
#   ./start_db_cluster.sh <dockerImage>

# EXAMPLE:
#   ./start_db_cluster.sh docker.io/arangodb:3.5.0

docker pull "$1"

LOCATION=$(pwd)/$(dirname "$0")

docker network create arangodb --subnet 172.28.0.0/16

echo "Averysecretword" > "$LOCATION"/jwtSecret
docker run --rm -v "$LOCATION"/jwtSecret:/jwtSecret "$1" arangodb auth header --auth.jwt-secret /jwtSecret > "$LOCATION"/jwtHeader
AUTHORIZATION_HEADER=$(cat "$LOCATION"/jwtHeader)

docker run -d --rm -v "$LOCATION"/jwtSecret:/jwtSecret -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.1.1 --name agent1 "$1" arangodb --cluster.start-dbserver false --cluster.start-coordinator false --auth.jwt-secret /jwtSecret
docker run -d --rm -v "$LOCATION"/jwtSecret:/jwtSecret -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.1.2 --name agent2 "$1" arangodb --cluster.start-dbserver false --cluster.start-coordinator false --starter.join agent1 --auth.jwt-secret /jwtSecret
docker run -d --rm -v "$LOCATION"/jwtSecret:/jwtSecret -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.1.3 --name agent3 "$1" arangodb --cluster.start-dbserver false --cluster.start-coordinator false --starter.join agent1 --auth.jwt-secret /jwtSecret

docker run -d --rm -v "$LOCATION"/jwtSecret:/jwtSecret -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.2.1 --name dbserver1 "$1" arangodb --cluster.start-dbserver true --cluster.start-coordinator false --starter.join agent1 --auth.jwt-secret /jwtSecret
docker run -d --rm -v "$LOCATION"/jwtSecret:/jwtSecret -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.2.2 --name dbserver2 "$1" arangodb --cluster.start-dbserver true --cluster.start-coordinator false --starter.join agent1 --auth.jwt-secret /jwtSecret
docker run -d --rm -v "$LOCATION"/jwtSecret:/jwtSecret -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.2.3 --name dbserver3 "$1" arangodb --cluster.start-dbserver true --cluster.start-coordinator false --starter.join agent1 --auth.jwt-secret /jwtSecret

docker run -d --rm -v "$LOCATION"/jwtSecret:/jwtSecret -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 --name coordinator1 -p 8529:8529 "$1" arangodb --cluster.start-dbserver false --cluster.start-coordinator true --starter.join agent1 --auth.jwt-secret /jwtSecret
docker run -d --rm -v "$LOCATION"/jwtSecret:/jwtSecret -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.2 --name coordinator2 "$1" arangodb --cluster.start-dbserver false --cluster.start-coordinator true --starter.join agent1 --auth.jwt-secret /jwtSecret

wait_server() {
    # shellcheck disable=SC2091
    until $(curl --output /dev/null --silent --head --fail -i -H "$AUTHORIZATION_HEADER" "http://$1/_api/version"); do
        printf '.'
        sleep 1
    done
}

# Wait for agents:
for a in 172.28.1.1:8531 \
         172.28.1.2:8531 \
         172.28.1.3:8531 \
         172.28.2.1:8530 \
         172.28.2.2:8530 \
         172.28.2.3:8530 \
         172.28.3.1:8529 \
         172.28.3.2:8529 ; do
    wait_server $a
done

docker exec coordinator1 arangosh --server.authentication=false --javascript.execute-string='require("org/arangodb/users").update("root", "test")'

rm "$LOCATION"/jwtHeader "$LOCATION"/jwtSecret

echo "Done, your cluster is ready."
