#!/bin/bash

docker run -d \
  -e LOG_LEVEL=Info \
  -e AUTH_USER=user \
  -e AUTH_PASSWORD=password \
  --network=arangodb -p 8888:8888 \
  docker.io/kalaksi/tinyproxy@sha256:6eddb7eba70227000b2a8948e84ecbf88db87bc910a54682ebef58cef9eb3887
