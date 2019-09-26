#!/bin/bash

# USAGE:
#   export ARANGO_LICENSE_KEY=<arangodb-enterprise-license>
#   ./start_db_single.sh <dockerImage>

# EXAMPLE:
#   ./start_db_single.sh docker.io/arangodb:3.5.0

docker pull "$1"

docker run -d -e ARANGO_ROOT_PASSWORD=test -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" -p 8529:8529 "$1"

echo "waiting for arangodb ..."

# shellcheck disable=SC2091
until $(curl --output /dev/null --silent --head --fail -i -u root:test 'http://localhost:8529/_api/version'); do
    printf '.'
    sleep 1
done
echo "READY!"
