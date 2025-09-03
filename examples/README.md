# Datastar Kotlin Examples

[//]: # (@formatter:off)
<!-- TOC -->
* [Datastar Kotlin Examples](#datastar-kotlin-examples)
  * [Spring](#spring)
  * [Quarkus](#quarkus)
  * [Ktor](#ktor)
  * [Java HTTP Server](#java-http-server)
<!-- TOC -->
[//]: # (@formatter:on)

## Spring

These examples use `Spring` to serve the front end.

- [Spring Web](spring/spring-web-example/src/main/kotlin/dev/datastar/kotlin/examples/spring/web/SpringWebExampleApplication.kt): Demonstrates integration with Spring Web.
- [Spring Webflux](spring/spring-webflux-example/src/main/kotlin/dev/datastar/kotlin/examples/spring/webflux/SpringWebfluxExampleApplication.kt): Demonstrates reactive integration with Spring Webflux.

## Quarkus

These examples use `Quarkus` to serve the front end.

- [Quarkus Rest](quarkus/quarkus-rest-example/src/main/kotlin/dev/datastar/kotlin/CounterApp.kt) : Demonstrates integration with Quarkus Rest.
- [Quarkus Qute](quarkus/quarkus-qute-example/src/main/kotlin/dev/datastar/kotlin/QuteCounterApp.kt): Demonstrates integration with Qute templates as templating engine.

## Ktor

This example uses `Ktor` to serve the front end. ([code](ktor/server.kt))

```shell
cd ./ktor ; jbang server.kt ; cd ..
```

## Java HTTP Server

This example uses the plain Java `HttpServer` to serve the front end. ([code](java-httpserver/server.kt))

```shell
cd ./java-httpserver ; jbang server.kt ; cd ..
```

