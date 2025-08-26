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
    implementation("dev.datastar.kotlin:kotlin-sdk:...")
}
```

#### Maven

```xml

<dependency>
    <groupId>dev.datastar.kotlin</groupId>
    <artifactId>kotlin-sdk</artifactId>
    <version>...</version>
</dependency>
```

### Usage

The SDK offers APIs to abstract the Datastar protocol while allowing you to adapt it to your own context and framework.

The following shows a simple implementation base of the Java `HttpServer`.

```kotlin

val server = HttpServer.create(
    InetSocketAddress(8080), // Port used
    0,                       // Backlog, 0 means default
    "/",                     // Path
    { exchange ->            // Exchange handler

        // The `readSignals` method extracts the signals from the request.
        // If you use a web framework, you likely don't need this since the framework probably already handles this in its own way.
        // However, this method in the SDK allows you to provide your own unmarshalling strategy so you can adapt it to your preferred technology!
        val request: Request = adaptRequest(exchange)
        val signals = readSignals<EventsWrapper>(request, jsonUnmarshaller)

        // Connect a Datastar SSE generator to the response.
        val response: Response = adaptResponse(exchange)
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
                """.trimIndent(),
        )

        generator.executeScript(
            script = "alert('Hello World!')",
        )

        exchange.close()
    }
)

fun adaptRequest(exchange: HttpExchange): Request = object : Request {
    override fun bodyString() = exchange.requestBody.use { it.readAllBytes().decodeToString() }

    override fun isGet() = exchange.requestMethod == "GET"

    override fun readParam(string: String) =
        exchange.requestURI.query
            ?.let { URLDecoder.decode(it, Charsets.UTF_8) }
            ?.split("&")
            ?.find { it.startsWith("$string=") }
            ?.substringAfter("=")!!
}

fun adaptResponse(exchange: HttpExchange): Response = object : Response {

    override fun sendConnectionHeaders(
        status: Int,
        headers: Map<String, List<String>>,
    ) {
        exchange.responseHeaders.putAll(headers)
        exchange.sendResponseHeaders(status, 0)
    }

    override fun write(text: String) {
        exchange.responseBody.write(text.toByteArray())
    }

    override fun flush() {
        exchange.responseBody.flush()
    }

}
```