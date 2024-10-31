#!/bin/bash

docker run -d \
  -e LOG_LEVEL=Info \
  -e AUTH_USER=user \
  -e AUTH_PASSWORD=password \
  --network=arangodb --ip=172.28.0.100 \
  docker.io/kalaksi/tinyproxy:1.7
