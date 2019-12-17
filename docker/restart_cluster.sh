#!/bin/bash

LOCATION=$(pwd)/$(dirname "$0")
IMAGE=docker.io/arangodb/enterprise-preview:3.6.0-rc.1

docker stop \
  agent1 \
  agent2 \
  agent3 \
  dbserver1 \
  dbserver2 \
  dbserver3 \
  coordinator1 \
  coordinator2

docker rm -f \
  coordinator1 \
  coordinator2

docker start \
  agent1 \
  agent2 \
  agent3

sleep 5
docker start \
  dbserver1 \
  dbserver2 \
  dbserver3

sleep 5
docker run -d -v "$LOCATION"/jwtSecret:/jwtSecret -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 --name coordinator1 "$IMAGE" arangodb --starter.id coordinator1 --cluster.start-dbserver false --cluster.start-coordinator true --starter.join agent1 --auth.jwt-secret /jwtSecret
docker run -d -v "$LOCATION"/jwtSecret:/jwtSecret -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.2 --name coordinator2 "$IMAGE" arangodb --starter.id coordinator2 --cluster.start-dbserver false --cluster.start-coordinator true --starter.join agent1 --auth.jwt-secret /jwtSecret


