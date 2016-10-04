# Driver Setup

Setup with default configuration, this automatically loads a properties file arangodb.properties if exists in the classpath:

``` Java
  // this instance is thread-safe
  ArangoDB arangoDB = new ArangoDB.Builder().build();

```


The driver is configured with some default values:

<table>
<tr><th>property-key<  h><th>description<  h><th>default value<  h><  r>
<tr><td>arangodb.host<  d><td>ArangoDB host<  d><td>127.0.0.1<  d><  r>
<tr><td>arangodb.port<  d><td>ArangoDB port<  d><td>8529<  d><  r>
<tr><td>arangodb.timeout<  d><td>socket connect timeout(millisecond)<  d><td>0<  d><  r>
<tr><td>arangodb.user<  d><td>Basic Authentication User<  d><td><  d><  r>
<tr><td>arangodb.password<  d><td>Basic Authentication Password<  d><td><  d><  r>
<tr><td>arangodb.useSsl<  d><td>use SSL connection<  d><td>false<  d><  r>
<tr><td>harangodb.chunksize<  d><td>VelocyStream Chunk content-size(bytes)<  d><td>30000<  d><  r>
<  able>

To customize the configuration the parameters can be changed in the code...

``` Java
  ArangoDB arangoDB = new ArangoDB.Builder().host("192.168.182.50").port(8888).build();
  
```
... or with a custom properties file (my.properties)

``` Java
  InputStream in = MyClass.class.getResourceAsStream("my.properties");
  ArangoDB arangoDB = new ArangoDB.Builder().loadProperties(in).build();
  
```

Example for arangodb.properties:
``` Java
  arangodb.host=127.0.0.1
  arangodb.port=8529
  arangodb.user=root
  arangodb.password=

```