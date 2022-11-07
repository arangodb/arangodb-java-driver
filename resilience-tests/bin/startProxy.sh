#!/bin/bash

wget -O bin/toxiproxy-server-linux-amd64 https://github.com/Shopify/toxiproxy/releases/download/${TOXIPROXY_VERSION}/toxiproxy-server-linux-amd64
chmod a+x bin/toxiproxy-server-linux-amd64
bin/toxiproxy-server-linux-amd64 &
