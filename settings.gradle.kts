plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "datastar-kotlin"

include("sdk")

includeBuild("examples/spring/spring-web-example")
includeBuild("examples/spring/spring-webflux-example")
includeBuild("examples/quarkus/quarkus-rest-example")
includeBuild("examples/quarkus/quarkus-qute-example")
includeBuild("examples/ktor/ktor-example")
includeBuild("examples/java-httpserver")
includeBuild("examples/micronaut/micronaut-reactor-example")
