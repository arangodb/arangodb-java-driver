# arangodb-java-driver-resiliency-tests

## run

Start ArangoDB docker containers:
```shell
./docker/start_db_single.sh docker.io/arangodb/arangodb:3.10.0
./docker/start_db_cluster.sh docker.io/arangodb/arangodb:3.10.0
./docker/start_db_active-failover.sh docker.io/arangodb/arangodb:3.10.0
```

Start [toxiproxy-server](https://github.com/Shopify/toxiproxy) at `127.0.0.1:8474`.

Run the tests:
```shell
mvn test
```

## deployments endpoints

### single server
- `172.28.3.1:8529`

### cluster
- `172.28.13.1:8529`
- `172.28.13.2:8529`
- `172.28.13.3:8529`

### active failover
- `172.28.113.1:8529`
- `172.28.113.2:8529`
- `172.28.113.3:8529`
