# Change guide

Read only the sections touching the change. Paths are repository-relative;
Java class names without a module prefix refer to `core/src/main/java/com/arangodb/`.

## API, options and configuration

For an endpoint change, trace the public interface and its async counterpart,
`internal/*Impl`, shared `internal/InternalArango*`, and relevant `model/` or
`entity/` types. Update applicable overloads and public Javadocs together. Use a
neighboring operation with the same request/response shape as the implementation
pattern, not just one with a similar name.

Preserve the distinction between absent options and explicit `false`, zero or
empty values. Nullable option fields and omission on the wire let the server
choose defaults. Keep parameters in their specified location (path, query,
headers, body); reuse `ArangoExecuteable.request()`, `InternalRequest`,
`ArangoRequestParam`, and the existing path/encoding helpers. Test non-ASCII or
reserved characters when changing names, handles or path construction.

Check success and error contracts, not just successful deserialization: missing
documents, conditional requests, silent writes, bulk per-document errors, and
transaction/dirty-read headers where relevant. Preserve response codes, Arango
error numbers and causes through the existing error-conversion path.

For a driver configuration option, follow the applicable chain:
`ArangoDB.Builder` -> `ArangoConfig`/`ArangoDefaults` -> consumer. If it belongs in
property-based configuration, also extend `config/ArangoConfigProperties` and
`internal/config/ArangoConfigPropertiesImpl`; use a compatible default method
where needed so custom implementations do not acquire a new required method.
Inspect [HttpProtocolConfig](../../../../http-protocol/src/main/java/com/arangodb/http/HttpProtocolConfig.java)
for HTTP-specific configuration.
Test builder/property parity and defaults without broadening core's dependencies.

## Serialization and providers

Use `InternalSerde` for driver-owned wire structures and the existing user-data
methods/annotations for documents and nested user values. Do not serialize driver
options through an application's mapper, or bypass a custom `ArangoSerde` by
serializing arbitrary user POJOs with the internal mapper. Preserve `RawJson`,
`RawBytes`, managed types, generic result types, and null handling where affected.

For a change that crosses this boundary, inspect the functional
`com/arangodb/serde/CustomSerdeTest`, `CustomSerdeAsyncTest`,
`JacksonInterferenceTest`, and `com/arangodb/RequestContextTest`, plus the relevant
`test-non-functional/src/test/java/serde/` tests. Check JSON and VelocyPack and the
affected provider(s); a Jackson 2-only test does not establish Jackson 3 or JSON-B
behavior. Preserve `RequestContext` delivery and cleanup on exceptional paths.

When adding or moving a provider, update its service descriptor and relevant
native-image metadata. Exercise discovery with the intended consumer classpath,
including shaded use when applicable. Do not assume adding a dependency selects
that provider when another provider supports the same content type.

## Execution, cursors and transport

Keep async execution non-blocking; pass request suppliers through
`ArangoExecutorAsync` so request-construction failures follow its future-based
error path. Preserve the configured downstream executor behavior, exception
conversion, and interruption handling in the synchronous boundary.

Before changing `internal/net/Communication`, distinguish failures before a
request is sent from ambiguous outcomes after sending it. Existing retry safety
is deliberately not equivalent to retrying every method that looks idempotent.
Do not broaden retries in a refactor: a replay can duplicate writes. Cover timeout,
failover and connection release paths in resilience tests.

For cursor changes, preserve batch state, retry identifiers, host affinity and
server-side cleanup through exhaustion, explicit close and failure as applicable.
Check sync iteration and async `nextBatch()` separately. For pool/client changes,
check pending futures, shutdown and externally supplied resource ownership; follow
the actual transport's ownership rules rather than closing every resource.

## Dependencies, shading and native image

Check the root and module POMs, including dependency management and exclusions.
The test parent's `adb.jackson.version` selects the Jackson version tests run
against; CI varies it to cover the supported range. Production Jackson dependency
management does not raise that floor. Preserve dependency convergence, explicit
scopes and the bytecode-enforcer constraints; do not silence them to make an
upgrade pass. Check `internal/serde/SerdeUtils` for version-specific handling.

Dependency or package changes can affect service resources, shading relocations,
reflection, proxies and GraalVM substitutions even when ordinary tests pass.
Inspect `shaded/pom.xml`, affected `META-INF/services/` and `META-INF/native-image/`
resources, and `shaded/src/main/java/graal/` as appropriate. Use the two-stage
shaded validation in the testing guide; reactor class directories are not proof
that the packaged relocated artifact works.

For entity reflection changes, use
`driver/src/test/java/helper/NativeImageHelper.java` as described in `dev-README.md`,
review its output, and check both driver variants' corresponding metadata. Do not
blindly overwrite service/proxy or other native configurations with that output.
Update originals, not generated shaded test copies. Run `SerdeArchTest` and,
against the shaded artifact, `ShadedArchTest` when module boundaries change; assess
native-image tests separately from JVM tests.
