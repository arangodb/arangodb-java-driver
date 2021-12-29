#!/bin/bash

COORDINATORS=("172.17.0.1:8529" "172.17.0.1:8539" "172.17.0.1:8549")

for a in ${COORDINATORS[*]} ; do
    if curl -u root:test --silent --fail "http://$a"; then
        echo "$a"
        exit 0
    fi
done

echo "Could not find any active endpoint!"
exit 1
