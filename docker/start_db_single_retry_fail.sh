#!/bin/bash

# USAGE:
#   export ARANGO_LICENSE_KEY=<arangodb-enterprise-license>
#   ./docker/start_db_single_retry_fail.sh <dockerImage>

# EXAMPLE:
#   ./docker/start_db_single_retry_fail.sh docker.io/arangodb/arangodb:3.7.1

./docker/start_db_single.sh "$1"
while [ $? -ne 0 ]; do
  echo "=== === ==="
  echo "single startup failed, retrying ..."
  ./docker/clean_single.sh
  ./docker/start_db_single.sh "$1"
done
