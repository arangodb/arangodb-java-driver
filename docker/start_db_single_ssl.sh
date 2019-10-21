#!/bin/bash

# USAGE:
#   export ARANGO_LICENSE_KEY=<arangodb-enterprise-license>
#   ./start_db_single_ssl.sh <dockerImage>

# EXAMPLE:
#   ./start_db_single_ssl.sh docker.io/arangodb/arangodb:3.5.1

docker pull "$1"

docker run -d -p 8529:8529 -e ARANGO_ROOT_PASSWORD=test -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" -v $(pwd)/server.pem:/server.pem "$1" arangod --ssl.keyfile /server.pem --server.endpoint ssl://0.0.0.0:8529

echo "waiting for arangodb ..."

# shellcheck disable=SC2091
until $(curl --output /dev/null --silent --head --fail -i --insecure -u root:test 'https://127.0.0.1:8529/_api/version'); do
    printf '.'
    sleep 1
done
echo "READY!"
