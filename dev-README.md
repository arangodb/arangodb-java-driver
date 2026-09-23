# dev-README

## Working on the driver

- [Agent instructions and development skill](AGENTS.md)
- [Architecture and ownership](.agents/skills/java-driver-development/references/architecture.md)
- [Build and test guide](.agents/skills/java-driver-development/references/testing.md)

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

See [CI parameters](.agents/skills/java-driver-development/references/testing.md#ci-parameters)
for the local SpotBugs/JaCoCo commands and report locations.

## update native image reflection configuration
To generate reflection configuration run [NativeImageHelper](./driver/src/test/java/helper/NativeImageHelper.java) and 
copy the generated json to 
[reflect-config.json](./driver/src/main/resources/META-INF/native-image/com.arangodb/arangodb-java-driver/reflect-config.json).
