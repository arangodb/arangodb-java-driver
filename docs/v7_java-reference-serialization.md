# Serialization

The driver functionalities related to serialization and deserialization (serde) are implemented by 2 different components:
- `internal serde`
- `user-data serde`

The **internal serde** is based on [Jackson](https://github.com/FasterXML/jackson) API and is responsible for
serializing/deserializing data definition classes (in packages `com.arangodb.model` and `com.arangodb.entity`). 
This includes all the classes that are not user data, such as metadata properties for databases, collections, graphs, ...

Furthermore, it is used to serialize and deserialize user data of the following managed types:
- `com.fasterxml.jackson.databind.JsonNode` and its subclasses (`ArrayNode`, `ObjectNode`, ...)
- `com.arangodb.util.RawJson`
- `com.arangodb.util.RawBytes`
- `com.arangodb.entity.BaseDocument`
- `com.arangodb.entity.BaseEdgeDocument`

Note that in `arangodb-java-driver-shaded`, due to dependencies relocation, the fully-qualified class name of first
entry above (`JsonNode`) is: `com.arangodb.shaded.fasterxml.jackson.databind.JsonNode`. The same relocation rule is also
applied to its subclasses (`ArrayNode`, `ObjectNode`, ...).

The **user-data serde** is used to serialize and deserialize the user data, namely the data representing:
- documents
- vertexes
- edges
- AQL bind variables
- transactions parameters
- bodies of custom requests and custom responses (`com.arangodb.Request<T>` and `com.arangodb.Response<T>`).

A `user-data serde` implements the `com.arangodb.serde.ArangoSerde` interface, which is an abstract API with no 
dependencies on specific libraries.
The `user-data serde` can be explicitly registered via `ArangoDB.Builder#serde(ArangoSerde)`.
If no serde is registered, then the driver will use SPI (Service Provider Interface) to automatically
discover and load serde service providers (instances of `com.arangodb.serde.ArangoSerdeProvider`).
In case no `user-data serde` is explicitly registered and no serde service provider can be discovered via SPI, then the
**internal serde** will be used to serialize and deserialize user data. In this case, please note that the 
**internal serde** is not part of the public API and could change in future releases without notice, thus breaking 
client applications relying on it to serialize or deserialize user data.

Examples of custom `user-data serde` implementations can be found here: 
- [JSON_B serde](../jsonb-serde/src/main/java/com/arangodb/serde/jsonb): a custom serde implementation based on `JSON-B`
  (supporting `JSON` format only)
- [Micronaut Serialization serde](https://github.com/arangodb-helper/arango-micronaut-native-example/blob/main/src/main/kotlin/com/example/ArangoService.kt): 
  a custom serde implementation based on [Micronaut Serialization](https://micronaut-projects.github.io/micronaut-serialization/latest/guide/index.html). 


## JacksonSerde (default user-data serde)

The default `user-data serde` is `com.arangodb.serde.jackson.JacksonSerde`.
It is implemented delegating [Jackson](https://github.com/FasterXML/jackson) `ObjectMapper`, thus compatible 
with Jackson Streaming, Data Binding and Tree Model API.

It supports both `JSON` and `VPACK` data formats, while offering the same API to the user.

The `JSON` implementation is provided by the module `com.arangodb:jackson-serde-json` and transitively imported by 
`arangodb-java-driver`. This implementation will be used by default.

The `VPACK` implementation is provided by the optional module `com.arangodb:jackson-serde-vpack`.


### Mapping API

The library is fully compatible with [Jackson Databind](https://github.com/FasterXML/jackson-databind)
API. To customize the serialization and deserialization behavior using the
Jackson Data Binding API, entities can be annotated with
[Jackson Annotations](https://github.com/FasterXML/jackson-annotations).
For more advanced customizations refer to [Custom serializer](#custom-serializers) section.


### Renaming Properties

To use a different serialized name for a field, use the annotation `@JsonProperty`.

```java
public class MyObject {
    @JsonProperty("title")
    private String name;
}
```


### Ignoring properties

To ignore fields use the annotation `@JsonIgnore`.

```java
public class Value {
    @JsonIgnore
    public int internalValue;
}
```


### Custom Configuration

Create an instance of `JacksonSerde`, configure the underlying `ObjectMapper` and register it in the driver:

```java
JacksonSerde serde = JacksonSerde.of(ContentType.JSON);
serde.configure((ObjectMapper mapper) -> {
    // ...
});

ArangoDB adb = new ArangoDB.Builder()
    .serde(serde)
    // ...
    .build();
```

See also [Jackson Databind](https://github.com/FasterXML/jackson-databind/wiki/JacksonFeatures) configurable features.


### Custom serializers

The serialization and deserialization can be customized using the lower level
Streaming API or the Tree Model API, creating and registering respectively
`JsonSerializer<T>` and `JsonDeserializer<T>`, as specified by the Jackson API
for [CustomSerializers](https://github.com/FasterXML/jackson-docs/wiki/JacksonHowToCustomSerializers).

```java
static class PersonSerializer extends JsonSerializer<Person> {
    @Override
    public void serialize(Person value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        // example using the Streaming API
        gen.writeStartObject();
        gen.writeFieldName("name");
        gen.writeString(value.name);
        gen.writeEndObject();
    }
}

static class PersonDeserializer extends JsonDeserializer<Person> {
    @Override
    public Person deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
        // example using the Tree Model API
        Person person = new Person();
        JsonNode rootNode = parser.getCodec().readTree(parser);
        JsonNode nameNode = rootNode.get("name");
        if (nameNode != null && nameNode.isTextual()) {
            person.name = nameNode.asText();
        }
        return person;
    }
}

// registering using annotation
@JsonSerialize(using = PersonSerializer.class)
public static class Person {
    public String name;
}

// registering programmatically
JacksonSerde serde = JacksonSerde.of(ContentType.JSON);
serde.configure(mapper -> {
    SimpleModule module = new SimpleModule("PersonModule");
    module.addDeserializer(Person.class, new PersonDeserializer());
    mapper.registerModule(module);
});

ArangoDB adb = new ArangoDB.Builder()
    .serde(serde)
    // ...
    .build();
```

### Jackson datatype and language modules

The `JacksonSerde` can be configured
with [Jackson datatype modules](https://github.com/FasterXML/jackson#third-party-datatype-modules)
as well as [Jackson JVM Language modules](https://github.com/FasterXML/jackson#jvm-language-modules).

### Kotlin

[Kotlin language module](https://github.com/FasterXML/jackson-module-kotlin)
enables support for Kotlin classes and types and can be registered in the following way:

```kotlin
val arangoDB = ArangoDB.Builder()
    .serde(JacksonSerde.of(ContentType.JSON).apply {
        configure { it.registerModule(KotlinModule()) }
    })
    .build()
```

### Scala

[Scala language module](https://github.com/FasterXML/jackson-module-scala)
enables support for Scala classes and types and can be registered in the following way:

```scala
val serde = JacksonSerde.of(ContentType.JSON)
serde.configure(mapper => mapper.registerModule(DefaultScalaModule))

val arangoDB = new ArangoDB.Builder()
  .serde(arangoJack)
  .build()
```

### Java 8 types

Support for Java 8 features is offered by
[jackson-modules-java8](https://github.com/FasterXML/jackson-modules-java8).

### Joda types

Support for Joda data types, such as DateTime, is offered by
[jackson-datatype-joda](https://github.com/FasterXML/jackson-datatype-joda).

### Metadata fields

Metadata fields `_id`, `_key`, `_rev`, `_from`, `_to` can be mapped using respectively the annotations (from package 
`com.arangodb.serde.jackson`): 
`@Id`, `@Key`, `@Rev`, `@From`, `@To`.

```java
public class MyObject {

  @Key
  private String key;
  
  // ...
}
```
