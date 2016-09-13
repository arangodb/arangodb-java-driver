
![ArangoDB-Logo](https://docs.arangodb.com/assets/arangodb_logo_2016_inverted.png)

# arangodb-java-driver

2.7: [![Build Status](https://secure.travis-ci.org/arangodb/arangodb-java-driver.svg?branch=2.7)](https://travis-ci.org/arangodb/arangodb-java-driver) 
3.0: [![Build Status](https://secure.travis-ci.org/arangodb/arangodb-java-driver.svg?branch=3.0)](https://travis-ci.org/arangodb/arangodb-java-driver) 
3.1: [![Build Status](https://secure.travis-ci.org/arangodb/arangodb-java-driver.svg?branch=3.1)](https://travis-ci.org/arangodb/arangodb-java-driver) 
4.0: [![Build Status](https://secure.travis-ci.org/arangodb/arangodb-java-driver.svg?branch=4.0)](https://travis-ci.org/arangodb/arangodb-java-driver) 

master: [![Build Status](https://secure.travis-ci.org/arangodb/arangodb-java-driver.svg?branch=master)](https://travis-ci.org/arangodb/arangodb-java-driver)

# Supported versions

* ```arangodb-java-driver 4.0.0``` for ArangoDB 3.1.x with VelocyStream
* ```arangodb-java-driver 3.1.0``` for ArangoDB 3.1.x with HTTP
* ```arangodb-java-driver 3.0.0``` for ArangoDB 3.0.x
* ```arangodb-java-driver 2.7.4``` for ArangoDB 2.7.x and ArangoDB 2.8.x

# Required

* [ArangoDB](https://github.com/arangodb/arangodb) version 3.1.X
* since ```arangodb-java-driver 4.0.0``` Java 1.8, otherwise Java 1.6

# Basics

## Maven

To add the driver to your project with maven, add the following code to your pom.xml
(please use a driver with a version number compatible to your ArangoDB server's version):

ArangoDB 3.1.X
```XML
<dependencies>
  <dependency>
    <groupId>com.arangodb</groupId>
    <artifactId>arangodb-java-driver</artifactId>
    <version>4.0.0</version>
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
# API Overview

Overview of all important API calls are available [here](docs/api_overview.md).

# Compile java driver
```
	mvn clean install -DskipTests=true -Dgpg.skip=true -Dmaven.javadoc.skip=true -B
```	

# Learn more
* [ArangoDB](https://www.arangodb.com/)
* [ChangeLog](https://github.com/arangodb/arangodb-java-driver/tree/master/ChangeLog)
* [Examples](https://github.com/arangodb/arangodb-java-driver/tree/master/src/test/java/com/arangodb/example)
* [Document examples](https://github.com/arangodb/arangodb-java-driver/tree/master/src/test/java/com/arangodb/example/document)
* [Graph examples](https://github.com/arangodb/arangodb-java-driver/tree/master/src/test/java/com/arangodb/example/graph)
* [HTTPS examples](https://github.com/arangodb/arangodb-java-driver/tree/master/src/test/java/com/arangodb/example/ssl)
* [Tutorial](https://www.arangodb.com/tutorial-java/)

