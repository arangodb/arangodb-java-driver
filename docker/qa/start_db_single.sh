#!/bin/bash

## ---
## community
## ---

docker run --rm -it -e ARANGO_ROOT_PASSWORD=test -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 docker.io/arangodb/arangodb-preview:3.6.0-rc.1 \
  --server.scheduler-queue-size=-10 --server.prio1-size=10 --server.maximal-queue-size=10

docker run --rm -it -e ARANGO_ROOT_PASSWORD=test -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 docker.io/arangodb/arangodb-preview:3.6.0-rc.1 \
  --server.scheduler-queue-size=10 --server.prio1-size=-10 --server.maximal-queue-size=10

docker run --rm -it -e ARANGO_ROOT_PASSWORD=test -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 docker.io/arangodb/arangodb-preview:3.6.0-rc.1 \
  --server.scheduler-queue-size=10 --server.prio1-size=10 --server.maximal-queue-size=-10

docker run --rm -it -e ARANGO_ROOT_PASSWORD=test -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 docker.io/arangodb/arangodb-preview:3.6.0-rc.1 \
  --server.scheduler-queue-size=10.1 --server.prio1-size=10 --server.maximal-queue-size=10

docker run --rm -it -e ARANGO_ROOT_PASSWORD=test -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 docker.io/arangodb/arangodb-preview:3.6.0-rc.1 \
  --server.scheduler-queue-size=10 --server.prio1-size=10.1 --server.maximal-queue-size=10

docker run --rm -it -e ARANGO_ROOT_PASSWORD=test -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 docker.io/arangodb/arangodb-preview:3.6.0-rc.1 \
  --server.scheduler-queue-size=10 --server.prio1-size=10 --server.maximal-queue-size=10.1

docker run --rm -it -e ARANGO_ROOT_PASSWORD=test -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 docker.io/arangodb/arangodb-preview:3.6.0-rc.1 \
  --server.scheduler-queue-size=xxx --server.prio1-size=10 --server.maximal-queue-size=10

docker run --rm -it -e ARANGO_ROOT_PASSWORD=test -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 docker.io/arangodb/arangodb-preview:3.6.0-rc.1 \
  --server.scheduler-queue-size=10 --server.prio1-size=xxx --server.maximal-queue-size=10

docker run --rm -it -e ARANGO_ROOT_PASSWORD=test -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 docker.io/arangodb/arangodb-preview:3.6.0-rc.1 \
  --server.scheduler-queue-size=10 --server.prio1-size=10 --server.maximal-queue-size=xxx


## ---
## enterprise
## ---

docker run --rm -it -e ARANGO_ROOT_PASSWORD=test -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 docker.io/arangodb/enterprise-preview:3.6.0-rc.1 \
  --server.scheduler-queue-size=-10 --server.prio1-size=10 --server.maximal-queue-size=10

docker run --rm -it -e ARANGO_ROOT_PASSWORD=test -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 docker.io/arangodb/enterprise-preview:3.6.0-rc.1 \
  --server.scheduler-queue-size=10 --server.prio1-size=-10 --server.maximal-queue-size=10

docker run --rm -it -e ARANGO_ROOT_PASSWORD=test -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 docker.io/arangodb/enterprise-preview:3.6.0-rc.1 \
  --server.scheduler-queue-size=10 --server.prio1-size=10 --server.maximal-queue-size=-10

docker run --rm -it -e ARANGO_ROOT_PASSWORD=test -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 docker.io/arangodb/enterprise-preview:3.6.0-rc.1 \
  --server.scheduler-queue-size=10.1 --server.prio1-size=10 --server.maximal-queue-size=10

docker run --rm -it -e ARANGO_ROOT_PASSWORD=test -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 docker.io/arangodb/enterprise-preview:3.6.0-rc.1 \
  --server.scheduler-queue-size=10 --server.prio1-size=10.1 --server.maximal-queue-size=10

docker run --rm -it -e ARANGO_ROOT_PASSWORD=test -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 docker.io/arangodb/enterprise-preview:3.6.0-rc.1 \
  --server.scheduler-queue-size=10 --server.prio1-size=10 --server.maximal-queue-size=10.1

docker run --rm -it -e ARANGO_ROOT_PASSWORD=test -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 docker.io/arangodb/enterprise-preview:3.6.0-rc.1 \
  --server.scheduler-queue-size=xxx --server.prio1-size=10 --server.maximal-queue-size=10

docker run --rm -it -e ARANGO_ROOT_PASSWORD=test -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 docker.io/arangodb/enterprise-preview:3.6.0-rc.1 \
  --server.scheduler-queue-size=10 --server.prio1-size=xxx --server.maximal-queue-size=10

docker run --rm -it -e ARANGO_ROOT_PASSWORD=test -e ARANGO_LICENSE_KEY="$ARANGO_LICENSE_KEY" --network arangodb --ip 172.28.3.1 docker.io/arangodb/enterprise-preview:3.6.0-rc.1 \
  --server.scheduler-queue-size=10 --server.prio1-size=10 --server.maximal-queue-size=xxx


echo "ALL FAILED! (as expected)"
