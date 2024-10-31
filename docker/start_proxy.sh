#!/bin/bash

docker run -d \
  -e LOG_LEVEL=Info \
  -e AUTH_USER=user \
  -e AUTH_PASSWORD=password \
  --network=arangodb -p 8888:8888 \
  docker.io/kalaksi/tinyproxy:1.7
