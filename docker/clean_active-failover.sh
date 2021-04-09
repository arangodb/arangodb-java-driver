#!/bin/bash

for c in server1 \
  server2 \
  server3; do
  docker rm -f $c
done
