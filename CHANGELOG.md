# Changelog

Datastar Kotlin SDK updates.

## Unreleased

### Added

- New `dev.data-star.kotlin:kotlin-sdk-shared` artifact hosting pure types (data classes, enums, constants, JSON typealiases) shared between the blocking and coroutines SDKs. Pulled transitively via `api` dependency from either flavor.
- New `dev.data-star.kotlin:kotlin-sdk-coroutines` artifact: fully `suspend` public SPI for frameworks whose emit primitives are suspending (Ktor, Spring WebFlux with `kotlinx.coroutines`, Micronaut Reactor with coroutine interop). Depends on `kotlinx-coroutines-core`.
- Shared wire-format test fixtures exposed via Gradle `java-test-fixtures` on `sdk-shared`, consumed by both the blocking and coroutines integration tests.
- Datastar Go golden-suite integration test runs against `sdk-coroutines` using an embedded Ktor server.

### Changed

- **Breaking**: Kotlin packages restructured. Shared types remain at `dev.datastar.kotlin.sdk`. The existing blocking `Request`, `Response`, `ServerSentEventGenerator`, `readSignals` (and related extensions) moved to `dev.datastar.kotlin.sdk.blocking`. Suspend counterparts live in `dev.datastar.kotlin.sdk.coroutines`. Users upgrading must update imports.
- Centralized shared Gradle properties (`groupId`, `version`, `datastar.test-suite.version`) in the root `gradle.properties`; per-module `gradle.properties` files dropped in favor of hardcoded `artifactId` inside each module's `MavenPublication`.
- Migrated examples to `kotlin-sdk-coroutines`:
  - `examples/ktor/ktor-example` now uses `respondBytesWriter` + `ByteWriteChannel` with the suspend SDK.
  - `examples/spring/spring-webflux-example` no longer wraps writes in `runBlocking`; the suspend `Response.write` calls the reactive sink directly.
  - `examples/micronaut/micronaut-reactor-example` no longer wraps writes in `runBlocking`.
- Examples using the blocking SDK (`java-httpserver`, `javalin-example`, `spring-web-example`, `quarkus-rest-example`, `quarkus-qute-example`) updated to the new `dev.datastar.kotlin.sdk.blocking.*` imports.

### Deprecated

### Removed

### Fixed

- POM `scm` URLs corrected from the defunct `GuillaumeTaffin/datastar-kotlin` (pre-org-move leftover) to `starfederation/datastar-kotlin` across all published modules.

### Security

## 1.0.0-RC4 - 2026-04-18

### Added

- Added `buildExamples` Gradle task to build all included example projects
- Added `Request.Method` enum nested inside `Request` interface
- Added `Request.method()` function returning `Request.Method`
- Added `ReadSignalsTests` unit tests for `readSignals`
- Added `namespace` option to `PatchElementsOptions` (`html`, `svg`, `mathml`) per ADR spec
- Added `ElementNamespace` enum

### Changed

- Upgraded Kotlin from 2.2.21 to 2.3.20
- Upgraded Gradle from 9.0.0 to 9.4.1
- Configured Gradle daemon JVM to use GraalVM 25
- `readSignals` now uses `Request.method()` instead of `Request.isGet()`
- Tested against [Datastar](https://github.com/starfederation/datastar/releases/tag/v1.0.0) `v1.0.0` (ecb1d4c4043524c1c5c58681c8337ded544f7a3a)

### Removed

- Removed `Request.isGet()` in favor of `Request.method()`

## 1.0.0-RC3 - 2025-11-01

### Changed

- Tested against [Datastar](https://github.com/starfederation/datastar/releases/tag/v1.0.0-RC.6) `v1.0.0-RC.6` (746ddadbd8ad46b0c87ea5f9f628ce906e844627)

## 1.0.0-RC2 - 2025-09-04

### Added

- Added `ServerSentEventGenerator.redirect` specialized helper method to send a script that will redirect the browser to a new URL.
- Added integration examples for
    - Spring Boot
        - Web MVC
        - WebFlux
    - Quarkus
        - Rest
        - Rest + Qute
    - Ktor
    - Java HTTP Server

## 1.0.0-RC1 - 2025-08-30

### Added

- Initial project setup.
- [Datastar ADR](https://github.com/starfederation/datastar/blob/develop/sdk/ADR.md) compliant implementation.
    - `readSignals`
    - `ServerSentEventGenerator.send`
    - `ServerSentEventGenerator.patchElements`
    - `ServerSentEventGenerator.patchSignals`
- Tested against [Datastar](https://github.com/starfederation/datastar/releases/tag/v1.0.0-RC.5) `v1.0.0-RC.5` (a7adbacbc9c1a3b27707d7cceba3c02a3f81a86c).
