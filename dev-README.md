# dev-README


## Start DB
Single:
```
./docker/start_db.sh
```
Cluster:
```
STARTER_MODE=cluster ./docker/start_db.sh
```
Active Failover:
```
STARTER_MODE=activefailover ./docker/start_db.sh
```


## GH Actions
Check results [here](https://github.com/arangodb/arangodb-java-driver/actions).


## SonarCloud
Check results [here](https://sonarcloud.io/project/overview?id=arangodb_arangodb-java-driver).


## check dependencies updates
```shell
mvn versions:display-dependency-updates
mvn versions:display-plugin-updates
```


## Code Analysis
Analyze (Spotbugs and JaCoCo):
```
mvn prepare-package -Pstatic-code-analysis
```
Report: [link](driver/target/site/jacoco/index.html)


## update native image reflection configuration

To generate reflection configuration run [NativeImageHelper](./driver/src/test/java/helper/NativeImageHelper.java) and 
copy the generated json to 
[reflect-config.json](./driver/src/main/resources/META-INF/native-image/com.arangodb/arangodb-java-driver/reflect-config.json).


## test
```shell
mvn test
```


## test native
```shell
mvn --no-transfer-progress install -DskipTests=true -Dgpg.skip=true -Dmaven.javadoc.skip=true
cd driver
mvn -Pnative test
```


## test native shaded
```shell
mvn --no-transfer-progress install -DskipTests=true -Dgpg.skip=true -Dmaven.javadoc.skip=true
cd integration-tests
mvn -Pnative test
```


## test ssl
```shell
mvn test -Dsurefire.failIfNoSpecifiedTests=false -Dtest=com.arangodb.ArangoSslTest -DSslTest=true
```


## integration tests
```shell
mvn install -DskipTests=true -Dgpg.skip=true -Dmaven.javadoc.skip=true
cd integration-tests
mvn -Pinternal-serde test
mvn -Pjackson-serde test
mvn -Pjsonb-serde test
mvn -Pplain test
```


## resilience tests
```shell
mvn install -DskipTests=true -Dgpg.skip=true -Dmaven.javadoc.skip=true
cd resilience-tests
mvn test
```
