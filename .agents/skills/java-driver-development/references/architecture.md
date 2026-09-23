# Architecture and ownership

Paths are repository-relative. This is a navigation map, not a replacement for
reading the implementation being changed.

## Modules

| Module | Responsibility |
| --- | --- |
| `core/` | Public APIs, models/entities, configuration and SPI contracts; shared request construction, execution, host management, cursors, and internal serialization. |
| `http-protocol/` | Vert.x HTTP/1.1 and HTTP/2 transport, JSON/VelocyPack wire formats, HTTP configuration and compression. |
| `vst-protocol/` | Optional VelocyStream transport, framing, authentication and message correlation. |
| `jackson-serde-json/` | Jackson 2 user-data serializer, mapping annotations and JSON provider. |
| `jackson3-serde-json/` | Separate Jackson 3 user-data serializer and JSON provider; not a replacement for core's Jackson 2 wire mapper. |
| `jackson-serde-vpack/` | VelocyPack provider built on the Jackson 2 serializer. |
| `jsonb-serde/` | JSON-B user-data serializer, annotations and provider. |
| `driver/` | Standard `arangodb-java-driver` assembly: core, HTTP transport, Jackson 2 JSON serializer, and native-image metadata. Most implementation code is **not** here. |
| `shaded/` | Separate `arangodb-java-driver-shaded` artifact: bundled/relocated implementation dependencies, service descriptors, native metadata and substitutions. |
| `test-parent/`, `test-*/` | Shared test build configuration and separate functional, non-functional, resilience and JMH benchmark modules. |
| `tutorial/` | Standalone Maven/Gradle consumer examples, outside the root reactor. |

Core owns the abstractions; transport and serializer modules implement them.
Do not make core depend on a concrete provider to reach a new feature. Inspect
[SerdeArchTest](../../../../test-non-functional/src/test/java/arch/SerdeArchTest.java)
and [ShadedArchTest](../../../../test-non-functional/src/test/java/arch/ShadedArchTest.java)
when changing these boundaries. Core intentionally uses Jackson internally;
provider independence does **not** mean removing all Jackson dependencies.

## Request lifecycle

Under `core/src/main/java/com/arangodb/`:

```text
ArangoDB.Builder -> ArangoConfig + protocol provider + host handling
  -> ArangoDBImpl / ArangoDBAsyncImpl -> database / collection / graph handles
  -> InternalArango* request builder -> InternalRequest
  -> ArangoExecutorSync / ArangoExecutorAsync
  -> CommunicationProtocol -> Communication -> host / connection / transport
  -> InternalResponse -> response deserializer -> result or ArangoDBException
```

`ArangoDB.Builder` discovers `ProtocolProvider` implementations and wires the
connection factory, host resolver/handler and protocol. `ArangoDB.async()` and
child handles reuse the executors and serializer via `internal/ArangoExecuteable`;
they are not independent clients. Shutting down the async facade shuts down the
underlying client.

`internal/InternalArangoCollection` is a representative endpoint implementation:
request methods choose paths, query/header parameters and serialization; response
deserializers recover envelopes, user data and metadata. `ArangoCollectionImpl`
and `ArangoCollectionAsyncImpl` expose the two execution styles over this shared
logic. `model/` holds options and `entity/` holds results.

The synchronous facade does not simply call the asynchronous facade. Each has an
executor; blocking occurs in `internal/net/CommunicationProtocol.execute()`.
`internal/net/Communication` coordinates host selection, connection release,
error translation and retry decisions. HTTP and VST supply protocol-specific I/O.
`internal/util/ResponseUtils` and `ArangoDBException` define error conversion.

`internal/cursor/`, `internal/InternalArangoCursor` and
`internal/ArangoCursorExecute` manage batches and cleanup. Follow `HostHandle`
through initial query, continuation and close when changing cursor routing.

## Serialization and extension points

[InternalSerdeImpl](../../../../core/src/main/java/com/arangodb/internal/serde/InternalSerdeImpl.java)
owns the wire mapper for driver envelopes, options, entities and managed/raw
values. `ArangoSerde` is the pluggable user-data boundary. `@UserData` and
`@UserDataInside` route nested user values through that boundary. The executors
establish `RequestContext` during response deserialization; custom serializers
can depend on it.

`ArangoSerdeProvider` and `ProtocolProvider` use service discovery with descriptors
under each module's `src/main/resources/META-INF/services/`. Provider selection,
including the case of several providers for one content type and the internal
fallback, is implemented in `ArangoDB.Builder`; follow it rather than relying on
classpath order. `ArangoConfigProperties` is the configuration extension point;
`internal/config/ArangoConfig` applies it and `internal/ArangoDefaults` supplies
defaults.

## Artifact and generated-code boundaries

`shaded/pom.xml` specifies relocations and resource filtering. User serializer
modules can be used outside the shaded artifact; `internal/ShadedProxy` and
`ArangoSerdeAccessor` bridge relevant relocated types. Treat that reflection-based
boundary as a contract, not redundant indirection.

Native metadata lives under participating modules' `META-INF/native-image/`;
`driver/` and `shaded/` have distinct driver configurations. The helper in
`driver/src/test/java/helper/NativeImageHelper.java` generates `reflect-config.json`
entries for entities/options and internal serializers, not all native metadata.

`core/src/main/java/com/arangodb/PackageVersion.java.in` generates `PackageVersion`
during the Maven build. Shaded test profiles generate rewritten test sources under
`target/generated-test-sources/replacer`; the original test sources remain the
editing point.
