#!/bin/bash

# USAGE:
#   export ARANGO_LICENSE_KEY=<arangodb-enterprise-license>
#   ./docker/start_db_active-failover_retry_fail.sh <dockerImage>

# EXAMPLE:
#   ./docker/start_db_active-failover_retry_fail.sh docker.io/arangodb/arangodb:3.7.10

./docker/start_db_active-failover.sh "$1"
while [ $? -ne 0 ]; do
  echo "=== === ==="
  echo "active-failover startup failed, retrying ..."
  ./docker/clean_active-failover.sh
  ./docker/start_db_active-failover.sh "$1"
done
