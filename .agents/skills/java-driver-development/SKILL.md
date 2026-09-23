---
name: java-driver-development
description: >-
  Maintain and extend the arangodb-java-driver repository. Use for implementing
  driver features or bug fixes, refactoring Java APIs and internals, changing
  transports or serialization, updating configuration or dependencies, and
  selecting tests for Maven, shaded artifacts, or native-image support. Not a
  guide to using the driver in an application.
---

# Java driver development

Apply the repository's [AGENTS.md](../../../AGENTS.md). Use the following workflow
at the scale of the change; do not load every reference for every task.

## Locate the change

Trace one neighboring operation from public API to request construction,
execution, and response mapping before choosing the implementation layer. For a
bug, identify the violated contract and a reproducer. For a feature, verify the
server API and supported server versions rather than extrapolating from another
endpoint. For a refactor, identify the observable behavior that must remain fixed.

| Work | Read when needed |
| --- | --- |
| Locate ownership or trace a request | [Architecture](references/architecture.md) |
| Change API, options, configuration, or wire mapping | [Change guide: API and configuration](references/changes.md#api-options-and-configuration) |
| Change serialization or provider discovery | [Change guide: serialization](references/changes.md#serialization-and-providers) |
| Change concurrency, cursors, retries, or transport | [Change guide: execution](references/changes.md#execution-cursors-and-transport) |
| Change dependencies, shading, or native support | [Change guide: packaging](references/changes.md#dependencies-shading-and-native-image) |
| Add tests or choose validation commands | [Testing](references/testing.md) |

## Implement and validate

Keep endpoint construction in the shared `InternalArango*` layer when both API
facades need it. Check the counterpart facade and related overloads; API symmetry
does not imply identical cursor iteration or exception-delivery mechanics.

Add regression coverage at the affected boundary, following the existing
parameterized fixtures. Where practical, demonstrate that a bug's reproducer fails
before the fix. Select the smallest meaningful test run first, then expand for
changed contracts: sync/async, protocol/content type, topology, serializer, or
artifact variant. Use the testing guide; a successful build with skipped or
unselected tests is not validation of behavior.

Review the diff for contract drift and generated output, then report as required
by AGENTS.md.