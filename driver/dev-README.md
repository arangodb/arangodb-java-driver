# dev-README

## GH Actions
Check results [here](https://github.com/arangodb/arangodb-java-driver/actions).

## SonarCloud
Check results [here](https://sonarcloud.io/project/overview?id=arangodb_arangodb-java-driver).

## check dependencies updates
```shell
mvn versions:display-dependency-updates
mvn versions:display-plugin-updates
```

## JaCoCo
```shell
mvn verify
```
Report:
- [integration-tests](target/site/jacoco/index.html)


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
