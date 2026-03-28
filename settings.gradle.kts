pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "datastar-kotlin"

include("sdk")
include("examples:spring:spring-web-example")
include("examples:spring:spring-webflux-example")
include("examples:quarkus:quarkus-rest-example")
include("examples:quarkus:quarkus-qute-example")
include("examples:ktor:ktor-example")
include("examples:java-httpserver")
include("examples:micronaut:micronaut-reactor-example")
include("examples:javalin-example")
