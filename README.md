# Datastar Kotlin SDK

A Kotlin SDK for Datastar, shipped in two flavors:

- `dev.data-star.kotlin:kotlin-sdk`: blocking. For frameworks with synchronous write primitives (Java `HttpServer`, Javalin, Spring Web MVC, Quarkus REST, Quarkus Qute…).
- `dev.data-star.kotlin:kotlin-sdk-coroutines`: `suspend`. For frameworks with suspending emit primitives (Ktor, Spring WebFlux, Micronaut Reactor).

Both produce identical Datastar wire output.

## Getting Started

### Add the dependency

#### Blocking SDK

##### Gradle

```kotlin
dependencies {
    implementation("dev.data-star.kotlin:kotlin-sdk:1.0.0-RC5")
}
```

##### Maven

```xml
<dependency>
    <groupId>dev.data-star.kotlin</groupId>
    <artifactId>kotlin-sdk</artifactId>
    <version>1.0.0-RC5</version>
</dependency>
```

#### Coroutines SDK

##### Gradle

```kotlin
dependencies {
    implementation("dev.data-star.kotlin:kotlin-sdk-coroutines:1.0.0-RC5")
}
```

##### Maven

```xml
<dependency>
    <groupId>dev.data-star.kotlin</groupId>
    <artifactId>kotlin-sdk-coroutines</artifactId>
    <version>1.0.0-RC5</version>
</dependency>
```

### Compatibility

- Java `21`+
- Kotlin `2.3.20`
- `kotlin-sdk-coroutines` tested against `kotlinx-coroutines-core` `1.10.2`.

### Usage

The API is identical in both flavors; the coroutines flavor exposes the same methods as `suspend`. Import from `dev.datastar.kotlin.sdk.blocking.*` or `dev.datastar.kotlin.sdk.coroutines.*`.

```kotlin
import dev.datastar.kotlin.sdk.*
import dev.datastar.kotlin.sdk.blocking.* // or .coroutines.*

val jsonUnmarshaller: JsonUnmarshaller<YourType> = /* your impl */
val request: Request = /* your adapter impl */
val response: Response = /* your adapter impl */

val signals = readSignals<YourType>(request, jsonUnmarshaller)

val generator = ServerSentEventGenerator(response)

generator.patchElements(elements = "<div>Merge</div>")

generator.patchSignals(
    signals = """{"one":1,"two":2}""",
)

generator.executeScript(script = "alert('Hello World!')")
```

### Examples

Runnable examples for each supported framework are listed in [`examples/README.md`](examples/README.md).
