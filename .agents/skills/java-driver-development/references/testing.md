# Build and test guide

Contents: [environment](#environment), [focused checks](#focused-checks),
[database fixtures](#database-fixtures), [test placement](#test-placement-and-coverage),
[shaded artifacts](#shaded-artifacts), [CI parameters](#ci-parameters),

## Environment

Run commands from the repository root unless noted. Use Maven 3.6.3 or newer
(no wrapper) and JDK 17 or newer: test modules target Java 17, production modules
compile with `--release 8`. Take server image, topology, JDK and GraalVM choices
from the matching job in `.circleci/config.yml`.

Read the root `pom.xml` and `test-parent/pom.xml` when changing build behavior.
## Focused checks

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

Replace the class selection with the relevant existing/new test. The no-match
flag accommodates upstream reactor modules; it can also hide a misspelled selector.
Check the selected module's **fresh** `target/failsafe-reports/` and verify that the
intended tests ran, including parameterized cases and skips. In a standalone test
module run, omit that flag so an unmatched selection fails. Tests annotated
`@SlowTest` are skipped unless `-DenableSlowTests=true` is also passed.

For a broader functional check (also start the HTTP proxy described below):

```sh
mvn -pl test-functional -am verify -Dgpg.skip=true -Dmaven.javadoc.skip=true
```

Use `test-non-functional` or `test-resilience` in place of `test-functional` for
those suites, with their own prerequisites. Avoid an undifferentiated root
`verify`: it includes suites with different infrastructure requirements.

## Database fixtures

The functional and non-functional classpath `arangodb.properties` files point to
`172.28.0.1:8529` with test credentials. Their file-backed configuration loader does
not merge arbitrary JVM system properties; do not assume `-Darangodb.hosts=...`
changes the endpoint. For a different isolated setup, adjust the applicable local
fixture configuration and keep that environment-only change out of the patch.
Some tests, especially resilience tests, have their own endpoint setup.

The repository harness is `docker/start_db.sh` (see `dev-README.md`). Inspect it
before running: it creates fixed-name Docker resources on a fixed subnet, mounts
the Docker socket, and defaults to an Enterprise image. Select the server image,
license and topology appropriate to the target CI case. Its Linux-style gateway
assumptions may not match another Docker environment.

```sh
./docker/start_db.sh
# Required by HttpProxyTest / the full default functional suite:
./docker/start_proxy.sh
```

`STARTER_MODE=cluster` and `SSL=true` select different server setups. Only use test
infrastructure: fixtures drop `java_driver_test_db` and can change server users
and other state. `BaseJunit5` contacts the server during class initialization, so
inheriting it does not produce a database-free unit test.

Resilience tests additionally use Toxiproxy at `127.0.0.1:8474`; see
`test-resilience/README.md`, its `bin/startProxy.sh`, test-specific setup and the CI
resilience job. Do not run suites against the same mutable server concurrently.

## Test placement and coverage

| Change | Start with |
| --- | --- |
| Public API, requests, options, response mapping | `test-functional/src/test/java/com/arangodb/`, including paired sync/async tests. |
| Custom serializers, provider boundaries, concurrency, consumer examples | Relevant tests under `test-non-functional/src/test/java/`; this suite is not generally database-free. |
| Retry, timeout, failover, pooling, shutdown, transport behavior | Relevant tests under `test-resilience/src/test/java/resilience/`. |
| Performance | Targeted JMH benchmarks in `test-perf/`; follow its README, and compare equivalent environments. |

Follow JUnit Jupiter, AssertJ and the nearby fixture style. Functional
`BaseJunit5` provides protocol-parameterized sync/async sources and version,
license and topology predicates. Use these for genuine capability differences,
not to hide regressions. It filters out VST on servers 3.12 and newer; a green
modern-server run does not cover a VST change. Use a compatible server for that
path. For pure tests, avoid inheriting fixtures that initialize a live client.

## Shaded artifacts

Follow CI's **two-stage** build: install production artifacts first, then run the
test module separately against installed JARs, without `-am`. A single shaded
reactor build can exercise unrelocated class directories instead of the artifact.

```sh
mvn install -Dmaven.test.skip=true -Dgpg.skip=true -Dmaven.javadoc.skip=true
mvn -f test-functional/pom.xml clean verify -Dshaded=true \
  -Dgpg.skip=true -Dmaven.javadoc.skip=true
```

Substitute the non-functional or resilience module when appropriate. Add
`-Dit.test=...` for a focused run, without suppressing unmatched tests. Inspect the
module's shaded profile first: some tests are explicitly excluded. `clean` removes
stale generated sources when switching shaded or SSL profiles; never edit those
generated copies.

## CI parameters

Choose relevant CI dimensions, not the whole matrix for every change. Read
`.circleci/config.yml` for current server/topology, Jackson-version, SSL, shaded
and native-image combinations.

`-Dssl=true` selects `test-functional/src/test-ssl/java`, not a blanket rerun of
all ordinary tests with TLS. Pair it with SSL server setup. `-Dnative=true` invokes
the native test profile and requires the matching GraalVM environment; do not
report JVM-only results as native coverage. `-DenableSlowTests=true` enables the
opt-in slow cases used by CI.

For static analysis and functional coverage, follow the CI sequence (database and
proxy required for the first command):

```sh
mvn -pl test-functional -am verify -Pstatic-code-analysis \
  -Dgpg.skip=true -Dmaven.javadoc.skip=true
mvn verify -Pstatic-code-analysis -Dmaven.test.skip=true \
  -Dgpg.skip=true -Dmaven.javadoc.skip=true
```

The explicit profile enables SpotBugs and JaCoCo. Preserve the first run's
coverage data between commands; the second generates module reports from it.
JaCoCo output is XML at the participating modules' `target/site/jacoco/jacoco.xml`.
Remote Sonar publishing is not needed for local checks.

For documentation-only changes, check referenced paths, commands against the
POMs, and `git diff --check`; no database run is necessary. For code changes with
missing infrastructure, still perform available checks and state which behavior
and configurations remain unverified. A zero-test, skipped, or stale report is not
a passing regression test.
