#!/bin/bash

# USAGE:
#   export ARANGO_LICENSE_KEY=<arangodb-enterprise-license>
#   ./start_db_single.sh <dockerImage>

# EXAMPLE:
#   ./start_db_single.sh docker.io/arangodb/arangodb:3.7.1

docker pull "$1"

docker network create arangodb --subnet 172.28.0.0/16

docker run -d -e ARANGO_ROOT_PASSWORD=test -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 --name arangodb "$1"

debug_container() {
  if [ ! "$(docker ps -aqf name="$1")" ]; then
    echo "$1 container not found!"
    exit 1
  fi

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

echo "waiting for arangodb ..."

# shellcheck disable=SC2091
until $(curl --output /dev/null --silent --head --fail -i -u root:test 'http://172.28.3.1:8529/_api/version'); do
    printf '.'
    debug_container arangodb
    sleep 1
done
echo "READY!"
