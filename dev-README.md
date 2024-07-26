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
mvn -Dgpg.skip=true -Dmaven.javadoc.skip=true -am -pl test-functional verify
mvn -Dgpg.skip=true -Dmaven.javadoc.skip=true -Dmaven.test.skip verify
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
