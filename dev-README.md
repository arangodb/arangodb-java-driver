# dev-README

## native image reflection configuration

To generate reflection configuration run `helper.NativeImageHelper` and copy the generated json to `src/main/resources/META-INF/native-image/com.arangodb/arangodb-java-driver/reflect-config.json`.


## test native

```shell
mvn -Pnative test
```

## test ssl

```shell
mvn test -Dtest=com.arangodb.ArangoSslTest -DSslTest=true
```
