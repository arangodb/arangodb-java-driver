## run

```
mvn clean package -am -pl test-perf
java -cp test-perf/target/benchmarks.jar com.arangodb.SerdeBench
```