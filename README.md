
![ArangoDB-Logo](https://docs.arangodb.com/assets/arangodb_logo_2016_inverted.png)

# arangodb-java-driver

2.7: [![Build Status](https://secure.travis-ci.org/arangodb/arangodb-java-driver.svg?branch=2.7)](https://travis-ci.org/arangodb/arangodb-java-driver) 
3.0: [![Build Status](https://secure.travis-ci.org/arangodb/arangodb-java-driver.svg?branch=3.0)](https://travis-ci.org/arangodb/arangodb-java-driver) 
3.1: [![Build Status](https://secure.travis-ci.org/arangodb/arangodb-java-driver.svg?branch=3.1)](https://travis-ci.org/arangodb/arangodb-java-driver) 
4.1: [![Build Status](https://secure.travis-ci.org/arangodb/arangodb-java-driver.svg?branch=4.1)](https://travis-ci.org/arangodb/arangodb-java-driver) 
master: [![Build Status](https://secure.travis-ci.org/arangodb/arangodb-java-driver.svg?branch=master)](https://travis-ci.org/arangodb/arangodb-java-driver)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.arangodb/arangodb-java-driver/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.arangodb/arangodb-java-driver)

## Supported versions

<table>
<tr><th>arangodb-java-driver</th><th>ArangoDB</th><th>network protocol</th><th>Java version</th></tr>
<tr><td>4.1.x</td><td>3.1.x</td><td>VelocyStream</td><td>1.6+</td></tr>
<tr><td>4.0.0</td><td>3.1.0-RC1 to 3.1.0-RC3</td><td>VelocyStream</td><td>1.6+</td></tr>
<tr><td>3.1.x</td><td>3.1.x</td><td>HTTP</td><td>1.6+</td></tr>
<tr><td>3.0.x</td><td>3.0.x</td><td>HTTP</td><td>1.6+</td></tr>
<tr><td>2.7.4</td><td>2.7.x and 2.8.x</td><td>HTTP</td><td>1.6+</td></tr>
</table>

## Maven

To add the driver to your project with maven, add the following code to your pom.xml
(please use a driver with a version number compatible to your ArangoDB server's version):

ArangoDB 3.1.X
```XML
<dependencies>
  <dependency>
    <groupId>com.arangodb</groupId>
    <artifactId>arangodb-java-driver</artifactId>
    <version>4.1.10</version>
  </dependency>
	....
</dependencies>
```

If you want to test with a snapshot version (e.g. 4.0.0-SNAPSHOT), add the staging repository of oss.sonatype.org to your pom.xml:

```XML
<repositories>
  <repository>
    <id>arangodb-snapshots</id>
    <url>https://oss.sonatype.org/content/groups/staging</url>
  </repository>
</repositories>
```

## Compile java driver

```
mvn clean install -DskipTests=true -Dgpg.skip=true -Dmaven.javadoc.skip=true -B
```	

## configure VelocyPack serialization

Since version `4.1.11` you can extend the VelocyPack serialization by registering additional `VPackModule`s on `ArangoDB.Builder`.

### Java 8 types 

Added support for:
* java.time.Instant
* java.time.LocalDate
* java.time.LocalDateTime
* java.util.Optional;
* java.util.OptionalDouble;
* java.util.OptionalInt;
* java.util.OptionalLong;

```XML
<dependencies>
  <dependency>
    <groupId>com.arangodb</groupId>
    <artifactId>velocypack-module-jdk8</artifactId>
    <version>1.0.1</version>
  </dependency>
</dependencies>
```

``` Java
ArangoDB arangoDB = new ArangoDB.Builder().registerModule(new VPackJdk8Module()).build();
``` 

### Scala types

Added support for:
* scala.Option
* scala.collection.immutable.List
* scala.collection.immutable.Map

```XML
<dependencies>
  <dependency>
    <groupId>com.arangodb</groupId>
    <artifactId>velocypack-module-scala</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>
```

``` Scala
val arangoDB: ArangoDB = new ArangoDB.Builder().registerModule(new VPackScalaModule).build
``` 

### Joda-Time

Added support for:
* org.joda.time.DateTime;
* org.joda.time.Instant;
* org.joda.time.LocalDate;
* org.joda.time.LocalDateTime;

```XML
<dependencies>
  <dependency>
    <groupId>com.arangodb</groupId>
    <artifactId>velocypack-module-joda</artifactId>
    <version>1.0.0</version>
  </dependency>
</dependencies>
```

``` Java
ArangoDB arangoDB = new ArangoDB.Builder().registerModule(new VPackJodaModule()).build();
``` 

# Learn more
* [ArangoDB](https://www.arangodb.com/)
* [ChangeLog](ChangeLog)
* [Documentation](docs/documentation.md)
* [Examples](src/test/java/com/arangodb/example)
* [Tutorial](https://www.arangodb.com/tutorials/tutorial-sync-java-driver/)
* [JavaDoc](http://arangodb.github.io/arangodb-java-driver/javadoc-4_1/index.html)
* [JavaDoc VelocyPack](http://arangodb.github.io/java-velocypack/javadoc-1_0/index.html)
