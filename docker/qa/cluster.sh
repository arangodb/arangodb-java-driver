#!/bin/bash

sudo "$ARANGO_3_6"/arangod --server.endpoint tcp://0.0.0.0:5001 \
  --agency.my-address=tcp://127.0.0.1:5001 \
  --server.authentication false \
  --agency.activate true \
  --agency.size 3 \
  --agency.endpoint tcp://127.0.0.1:5001 \
  --agency.supervision true \
  --server.scheduler-queue-size=-10 --server.prio1-size=10 --server.maximal-queue-size=10 \
  --database.directory agent1

sudo "$ARANGO_3_6"/arangod --server.endpoint tcp://0.0.0.0:5001 \
  --agency.my-address=tcp://127.0.0.1:5001 \
  --server.authentication false \
  --agency.activate true \
  --agency.size 3 \
  --agency.endpoint tcp://127.0.0.1:5001 \
  --agency.supervision true \
  --server.scheduler-queue-size=10 --server.prio1-size=-10 --server.maximal-queue-size=10 \
  --database.directory agent1

sudo "$ARANGO_3_6"/arangod --server.endpoint tcp://0.0.0.0:5001 \
  --agency.my-address=tcp://127.0.0.1:5001 \
  --server.authentication false \
  --agency.activate true \
  --agency.size 3 \
  --agency.endpoint tcp://127.0.0.1:5001 \
  --agency.supervision true \
  --server.scheduler-queue-size=10 --server.prio1-size=10 --server.maximal-queue-size=-10 \
  --database.directory agent1

sudo "$ARANGO_3_6"/arangod --server.endpoint tcp://0.0.0.0:5001 \
  --agency.my-address=tcp://127.0.0.1:5001 \
  --server.authentication false \
  --agency.activate true \
  --agency.size 3 \
  --agency.endpoint tcp://127.0.0.1:5001 \
  --agency.supervision true \
  --server.scheduler-queue-size=10.1 --server.prio1-size=10 --server.maximal-queue-size=10 \
  --database.directory agent1

sudo "$ARANGO_3_6"/arangod --server.endpoint tcp://0.0.0.0:5001 \
  --agency.my-address=tcp://127.0.0.1:5001 \
  --server.authentication false \
  --agency.activate true \
  --agency.size 3 \
  --agency.endpoint tcp://127.0.0.1:5001 \
  --agency.supervision true \
  --server.scheduler-queue-size=10 --server.prio1-size=10.1 --server.maximal-queue-size=10 \
  --database.directory agent1

sudo "$ARANGO_3_6"/arangod --server.endpoint tcp://0.0.0.0:5001 \
  --agency.my-address=tcp://127.0.0.1:5001 \
  --server.authentication false \
  --agency.activate true \
  --agency.size 3 \
  --agency.endpoint tcp://127.0.0.1:5001 \
  --agency.supervision true \
  --server.scheduler-queue-size=10 --server.prio1-size=10 --server.maximal-queue-size=10.1 \
  --database.directory agent1

sudo "$ARANGO_3_6"/arangod --server.endpoint tcp://0.0.0.0:5001 \
  --agency.my-address=tcp://127.0.0.1:5001 \
  --server.authentication false \
  --agency.activate true \
  --agency.size 3 \
  --agency.endpoint tcp://127.0.0.1:5001 \
  --agency.supervision true \
  --server.scheduler-queue-size=xxx --server.prio1-size=10 --server.maximal-queue-size=10 \
  --database.directory agent1

sudo "$ARANGO_3_6"/arangod --server.endpoint tcp://0.0.0.0:5001 \
  --agency.my-address=tcp://127.0.0.1:5001 \
  --server.authentication false \
  --agency.activate true \
  --agency.size 3 \
  --agency.endpoint tcp://127.0.0.1:5001 \
  --agency.supervision true \
  --server.scheduler-queue-size=10 --server.prio1-size=xxx --server.maximal-queue-size=10 \
  --database.directory agent1

sudo "$ARANGO_3_6"/arangod --server.endpoint tcp://0.0.0.0:5001 \
  --agency.my-address=tcp://127.0.0.1:5001 \
  --server.authentication false \
  --agency.activate true \
  --agency.size 3 \
  --agency.endpoint tcp://127.0.0.1:5001 \
  --agency.supervision true \
  --server.scheduler-queue-size=10 --server.prio1-size=10 --server.maximal-queue-size=xxx \
  --database.directory agent1

echo "ALL FAILED! (as expected)"
