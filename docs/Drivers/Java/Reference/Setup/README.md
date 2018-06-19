# Driver setup

Setup with default configuration, this automatically loads a properties file arangodb.properties if exists in the classpath:

```Java
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
<tr><td>arangodb.connections.max</td><td>max number of connections</td><td>1 VST, 20 HTTP</td></tr>
<tr><td>arangodb.protocol</td><td>used network protocol</td><td>VST</td></tr>
</table>

To customize the configuration the parameters can be changed in the code...

```Java
  ArangoDB arangoDB = new ArangoDB.Builder().host("192.168.182.50", 8888).build();
```

... or with a custom properties file (my.properties)

```Java
  InputStream in = MyClass.class.getResourceAsStream("my.properties");
  ArangoDB arangoDB = new ArangoDB.Builder().loadProperties(in).build();
```

Example for arangodb.properties:

```Java
  arangodb.hosts=127.0.0.1:8529,127.0.0.1:8529
  arangodb.user=root
  arangodb.password=
```
