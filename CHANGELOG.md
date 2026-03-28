# Changelog

Datastar Kotlin SDK updates.

## Unreleased

### Added

- Added `buildExamples` Gradle task to build all included example projects
- Added `Request.Method` enum nested inside `Request` interface
- Added `Request.method()` function returning `Request.Method`
- Added `ReadSignalsTests` unit tests for `readSignals`

### Changed

- Upgraded Kotlin from 2.2.21 to 2.3.20
- Upgraded Gradle from 9.0.0 to 9.4.1
- Configured Gradle daemon JVM to use GraalVM 25
- `readSignals` now uses `Request.method()` instead of `Request.isGet()`
- Tested against [Datastar](https://github.com/starfederation/datastar/releases/tag/v1.0.0-RC.8) `v1.0.0-RC.8` (f46ece21f7446f755d17bec79ebadf2262bd9099)

### Deprecated

### Removed

- Removed `Request.isGet()` in favor of `Request.method()`

### Fixed

- Fixed indentation in `executeScriptWithoutDefaults` test case output

### Security

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
