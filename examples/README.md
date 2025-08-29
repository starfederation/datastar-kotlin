# Datastar Kotlin Examples

* [About the examples](#about-the-examples)
* [Running the examples](#running-the-examples)
    * [Prerequisites](#prerequisites)
    * [Java HTTP Server](#java-http-server)

## About the examples

This directory contains examples of using Datastar in Kotlin.  
All are a simple counter implemented using Datastar server-sent events.

- the front end consists in a single HTML page located in the `front` directory
- each back implementation is in a separate directory, consisting in a single Kotlin file

## Running the examples

### Prerequisites

To make the examples work as simply as possible, each back implementation is a JBang script.

JBang is a tool that allows to run Kotlin scripts taking care of all the dependencies without the need to use more heavy weight tools like Maven or Gradle.  
You can find the installation instructions on the [official documentation](https://www.jbang.dev/documentation/jbang/latest/installation.html).

### Java HTTP Server

This example uses the plain Java `HttpServer` to serve the front end. ([code](java-httpserver/server.kt))

```shell
cd ./java-httpserver ; jbang server.kt ; cd ..
```

### Ktor

This example uses the `Ktor` to serve the front end. ([code](ktor/server.kt))

```shell
cd ./ktor ; jbang server.kt ; cd ..
```
