# arangodb-java-driver-resilience-tests

## run

Start (single server) ArangoDB:
```shell
./docker/start_db.sh
```

Start [toxiproxy-server](https://github.com/Shopify/toxiproxy) at `127.0.0.1:8474`.

Run the tests:
```shell
 mvn test -am -pl test-resilience
```
