# Driver Setup

Setup with default configuration, this automatically loads a properties file arangodb.properties if exists in the classpath:

``` Java
  // this instance is thread-safe
  ArangoDB arangoDB = new ArangoDB.Builder().build();

```


The driver is configured with some default values:

<table>
<tr><th>property-key</th><th>description</th><th>default value</th></tr>
<tr><td>arangodb.hosts</td><td>ArangoDB hosts</td><td>127.0.0.1:8529</td></tr>
<tr><td>arangodb.timeout</td><td>socket connect timeout(millisecond)</td><td>0</td></tr>
<tr><td>arangodb.user</td><td>Basic Authentication User</td><td></td></tr>
<tr><td>arangodb.password</td><td>Basic Authentication Password</td><td></td></tr>
<tr><td>arangodb.useSsl</td><td>use SSL connection</td><td>false</td></tr>
<tr><td>arangodb.chunksize</td><td>VelocyStream Chunk content-size(bytes)</td><td>30000</td></tr>
<tr><td>arangodb.connections.max</td><td>max number of connections</td><td>1</td></tr>
</table>

To customize the configuration the parameters can be changed in the code...

``` Java
  ArangoDB arangoDB = new ArangoDB.Builder().host("192.168.182.50", 8888).build();
  
```
... or with a custom properties file (my.properties)

``` Java
  InputStream in = MyClass.class.getResourceAsStream("my.properties");
  ArangoDB arangoDB = new ArangoDB.Builder().loadProperties(in).build();
  
```

Example for arangodb.properties:
``` Java
  arangodb.hosts=127.0.0.1:8529,127.0.0.1:8529
  arangodb.user=root
  arangodb.password=

```

## SSL

To use SSL, you have to set the configuration `useSsl` to `true` and set a `SSLContext`. (see [example code](../src/test/java/com/arangodb/example/ssl/SslExample.java))

``` Java
  
  ArangoDB arangoDB = new ArangoDB.Builder().useSsl(true).sslContext(sc).build();
  
```

## Connection Pooling

The driver supports connection pooling with a default of 1 maximum connections. To change this value use the method `maxConnections(Integer)` in `ArangoDB.Builder`.

``` Java

  ArangoDB arangoDB = new ArangoDB.Builder().maxConnections(8).build();

```