# Changelog

Datastar Kotlin SDK updates.

## Unreleased

### Added

### Changed

### Deprecated

### Removed

### Fixed

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
