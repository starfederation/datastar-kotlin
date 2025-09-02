plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "datastar-kotlin"

include("sdk")

includeBuild("examples/quarkus-example")
includeBuild("examples/quarkus-qute-example")
