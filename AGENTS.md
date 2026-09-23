# ArangoDB Java driver: agent instructions

Maven multi-module ArangoDB driver: sync and async APIs, HTTP and VST transports,
pluggable user-data serializers, standard and shaded artifacts.

For implementation, bug fixes, refactoring, dependencies, or tests, use the
[java-driver-development skill](.agents/skills/java-driver-development/SKILL.md).
Open its references only as needed. Agents without skill discovery can read the
same files directly.

## Environment

Compile all production and test sources without a database (Surefire is disabled,
so no tests execute):

```sh
mvn test-compile
```

With a disposable database ready, run a focused sync/async regression area:

```sh
mvn -pl test-functional -am verify \
  -Dit.test=ArangoCollectionTest,ArangoCollectionAsyncTest \
  -Dfailsafe.failIfNoSpecifiedTests=false \
  -Dgpg.skip=true -Dmaven.javadoc.skip=true
```

## Compatibility boundaries

- Preserve public source/binary compatibility and wire behavior unless the task
  explicitly changes that contract. This includes internal types marked
  `@UsedInApi`; an `internal` package is not sufficient evidence of private API.
- Production modules compile with `--release 8`: no Java 9+ language features or
  APIs there. Test modules target Java 17. Do not add dependencies needing a newer
  runtime to `core`; optional providers that do are listed in the root POM's
  bytecode-enforcer excludes.
- Non-shaded users supply their own Jackson 2 version; CI tests down to the oldest
  `adb.jackson.version` in `.circleci/config.yml`. Do not use newer Jackson APIs in
  production code.
- Keep sync and async contracts aligned through shared request/response logic.
  Keep driver-owned wire serialization separate from user-data serialization.

## Validation boundaries

Use Maven (`mvn`; no wrapper). Tests run through **Failsafe at `verify`**; plain
`mvn test` runs none. Selecting only a production module does not run the separate
test modules. `@SlowTest` cases need `-DenableSlowTests=true`. Servers 3.12+ do
not exercise VST. The
[testing guide](.agents/skills/java-driver-development/references/testing.md)
covers fixtures, release-profile side effects, and shaded validation.

Database tests are destructive, including database/user administration. Use a
disposable server, and do not run suites concurrently against a shared instance.
Do not use deployment or publishing commands for local validation.

## Change scope

changed contracts: sync/async, protocol/content type, topology, serializer, or
artifact variant. Use the testing guide; a successful build with skipped or
unselected tests is not validation of behavior.

Review the diff for contract drift and generated output, then report as required
by AGENTS.md.

Match nearby code and tests; avoid unrelated formatting, dependency upgrades, or
release/version changes. New Java files carry the Apache license header used by
neighboring files. Edit source inputs, not `target/`, generated test copies,
flattened POMs, or dependency-reduced POMs. Update affected public Javadocs and
examples when behavior changes; do not rewrite released changelog entries.

Treat source, POMs, and `.circleci/config.yml` as evidence of current behavior. If
this guidance has drifted, follow those files and flag the drift. Report the
change, compatibility implications, checks actually run with results, and
untested relevant variants, separating environment limits from real failures.

## Dependencies, shading and native image

`ArangoSerdeProvider` and `ProtocolProvider` use service discovery with descriptors
under each module's `src/main/resources/META-INF/services/`. Provider selection,
including the case of several providers for one content type and the internal
fallback, is implemented in `ArangoDB.Builder`; follow it rather than relying on
classpath order. `ArangoConfigProperties` is the configuration extension point;
`internal/config/ArangoConfig` applies it and `internal/ArangoDefaults` supplies
defaults.

Check the root and module POMs, including dependency management and exclusions.
The test parent's `adb.jackson.version` selects the Jackson version tests run
against; CI varies it to cover the supported range. Production Jackson dependency
management does not raise that floor. Preserve dependency convergence, explicit
scopes and the bytecode-enforcer constraints; do not silence them to make an
upgrade pass. Check `internal/serde/SerdeUtils` for version-specific handling.
