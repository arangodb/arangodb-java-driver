# arangodb-java-driver-resilience-tests

## run

Start the three-coordinator cluster used by the CI resilience job:
```shell
STARTER_MODE=cluster COMPRESSION=true ./docker/start_db.sh
```

In another terminal, start [toxiproxy-server](https://github.com/Shopify/toxiproxy)
at `127.0.0.1:8474`:
```shell
cd test-resilience
TOXIPROXY_VERSION=v2.9.0 ./bin/startProxy.sh
```

Run the tests from the repository root (Failsafe runs them during `verify`):
```shell
mvn verify -am -pl test-resilience -Dgpg.skip=true -Dmaven.javadoc.skip=true
```

See the [build and test guide](../.agents/skills/java-driver-development/references/testing.md)
for isolated database setup, focused selection, and shaded-artifact validation.
