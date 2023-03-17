# ArangoDB Java Driver

The official ArangoDB [Java Driver](https://github.com/arangodb/arangodb-java-driver).

- [Java Driver Tutorial](../tutorial)
- [Reference](./v7_java-reference.md)


## Supported versions

Only the latest version of this driver is maintained to support the most recent ArangoDB server features. 
It is compatible with all supported stable versions of ArangoDB server, see 
[Product Support End-of-life Announcements](https://www.arangodb.com/eol-notice).

It is compatible with JDK 8 and higher versions, currently tested up to JDK 19.


## Maven

To add the driver to your project with Maven, add the following code to your
`pom.xml` (substitute `x.x.x` with the latest driver version):

```xml
<dependencies>
  <dependency>
    <groupId>com.arangodb</groupId>
    <artifactId>arangodb-java-driver</artifactId>
    <version>x.x.x</version>
  </dependency>
</dependencies>
```


## Gradle

To add the driver to your project with Gradle, add the following code to your
`build.gradle` (substitute `x.x.x` with the latest driver version):

```
repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.arangodb:arangodb-java-driver:x.x.x'
}
```


## GraalVM Native Image

The driver supports GraalVM Native Image compilation. 
To compile with `--link-at-build-time` when `http-protocol` module is present in the classpath, additional substitutions
are be required for its transitive dependencies (`Netty` and `Vert.x`). An example of this can be found 
[here](../driver/src/test/java/graal). Such substitutions are not required when compiling the shaded driver. 


### Quarkus and Micronaut examples

The driver can be used from Quarkus and Micronaut applications and does not
require any additional configuration for GraalVM native image generation.
Examples can be found here:

- [arango-quarkus-native-example](https://github.com/arangodb-helper/arango-quarkus-native-example)
- [arango-micronaut-native-example](https://github.com/arangodb-helper/arango-micronaut-native-example)


## See Also

- [JavaDoc](https://www.javadoc.io/doc/com.arangodb/arangodb-java-driver/latest/index.html)
- [ChangeLog](../ChangeLog.md)
- [Code examples](../driver/src/test/java/com/arangodb/example)
- [Tutorial](../tutorial)
