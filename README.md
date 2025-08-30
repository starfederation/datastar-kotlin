# Datastar Kotlin SDK

A Kotlin SDK for Datastar!

- No dependencies, just the standard Kotlin library!
- 100% Kotlin, no Java dependencies!
- Multiplatform!
- Framework-agnostic, adapt to your own context and framework!

## Getting Started

### Minimum Requirements

The minimum JVM version compatible is **Java 21**.

### Add the dependency

#### Gradle

```kotlin
dependencies {
    implementation("dev.cloudgt.datastar.kotlin:kotlin-sdk:1.0.0-RC1")
}
```

#### Maven

```xml

<dependency>
    <groupId>dev.cloudgt.datastar.kotlin</groupId>
    <artifactId>kotlin-sdk</artifactId>
    <version>1.0.0-RC1</version>
</dependency>
```

### Usage

The SDK offers APIs to abstract the Datastar protocol while allowing you to adapt it to your own context and framework.

The following shows a simple implementation base of the Java `HttpServer`.

```kotlin
//  Depending on your context, you'll need to adapt the `Request` and `Response` interfaces, as well as implementation of the `JsonUnmarshaller` type.
val jsonUnmarshaller: JsonUnmarshaller<YourType> = "... you implementation"
val request: Request = "... you implementation"
val response: Response = "... you implementation"

// The `readSignals` method extracts the signals from the request.
// If you use a web framework, you likely don't need this since the framework probably already handles this in its own way.
// However, this method in the SDK allows you to provide your own unmarshalling strategy so you can adapt it to your preferred technology!
val signals = readSignals<YourType>(request, jsonUnmarshaller)

// Connect a Datastar SSE generator to the response.
val generator = ServerSentEventGenerator(response)

// Below are some simple examples of how to use the generator.
generator.patchElements(
    elements = "<div>Merge</div>",
)

generator.patchSignals(
    signals =
        """
        {
          "one":1,
          "two":2
        }
        """,
)

generator.executeScript(
    script = "alert('Hello World!')",
)
```

### Examples

You can find runnable examples of how to use the SDK in multiple concrete web application frameworks and contexts in the [examples](examples/README.md) folder.