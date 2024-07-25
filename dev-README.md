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
mvn -Pstatic-code-analysis -am -pl test-functional test
mvn -Dgpg.skip=true -Dmaven.javadoc.skip=true -am -pl core verify
```
Reports:
- [core](core/target/site/jacoco/index.html)
- [jackson-serde-json](jackson-serde-json/target/site/jacoco/index.html)
- [jackson-serde-vpack](jackson-serde-vpack/target/site/jacoco/index.html)
- [http-protocol](http-protocol/target/site/jacoco/index.html)
- [vst-protocol](vst-protocol/target/site/jacoco/index.html)


## update native image reflection configuration

To generate reflection configuration run [NativeImageHelper](./driver/src/test/java/helper/NativeImageHelper.java) and 
copy the generated json to 
[reflect-config.json](./driver/src/main/resources/META-INF/native-image/com.arangodb/arangodb-java-driver/reflect-config.json).

````
## test
```shell
mvn test
```
````

## test native
```shell
mvn install -DskipTests=true -Dgpg.skip=true -Dmaven.javadoc.skip=true -pl shaded,test-functional
cd test-native
mvn test
```


## test native shaded
```shell
mvn install -DskipTests=true -Dgpg.skip=true -Dmaven.javadoc.skip=true -pl shaded,test-functional
cd test-native
mvn test -Dshaded
```


## test ssl
```shell
mvn test -am -pl test-ssl
```

````
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
cd test-resilience
mvn test
```
````