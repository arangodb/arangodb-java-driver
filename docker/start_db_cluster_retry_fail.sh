#!/bin/bash

# USAGE:
#   export ARANGO_LICENSE_KEY=<arangodb-enterprise-license>
#   ./docker/start_db_cluster_retry_fail.sh <dockerImage>

# EXAMPLE:
#   ./docker/start_db_cluster_retry_fail.sh docker.io/arangodb/arangodb:3.7.1

./docker/start_db_cluster.sh "$1"
while [ $? -ne 0 ]; do
  echo "=== === ==="
  echo "cluster startup failed, retrying ..."
  ./docker/clean_cluster.sh
  ./docker/start_db_cluster.sh "$1"
done
