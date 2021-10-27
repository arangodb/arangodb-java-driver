#!/bin/bash

for c in agent1 \
  agent2 \
  agent3 \
  dbserver1 \
  dbserver2 \
  coordinator1 \
  coordinator2; do
  docker rm -f $c
done
