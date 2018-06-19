## VelocyPack serialization

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
    <version>1.0.2</version>
  </dependency>
</dependencies>
```

```Java
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
    <version>1.0.1</version>
  </dependency>
</dependencies>
```

```Scala
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

```Java
ArangoDB arangoDB = new ArangoDB.Builder().registerModule(new VPackJodaModule()).build();
```

## custom serialization

```Java
  ArangoDB arangoDB = new ArangoDB.Builder().registerModule(new VPackModule() {
    @Override
    public <C extends VPackSetupContext<C>> void setup(final C context) {
      context.registerDeserializer(MyObject.class, new VPackDeserializer<MyObject>() {
        @Override
        public MyObject deserialize(VPackSlice parent,VPackSlice vpack,
            VPackDeserializationContext context) throws VPackException {
          MyObject obj = new MyObject();
          obj.setName(vpack.get("name").getAsString());
          return obj;
        }
      });
      context.registerSerializer(MyObject.class, new VPackSerializer<MyObject>() {
        @Override
        public void serialize(VPackBuilder builder,String attribute,MyObject value,
            VPackSerializationContext context) throws VPackException {
          builder.add(attribute, ValueType.OBJECT);
          builder.add("name", value.getName());
          builder.close();
        }
      });
    }
  }).build();
```
