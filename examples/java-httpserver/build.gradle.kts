plugins {
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    application
}

repositories {
    mavenCentral()
}

application {
    mainClass = "dev.datastar.kotlin.examples.httpserver.ApplicationKt"
}

dependencies {
    implementation("dev.data-star.kotlin:kotlin-sdk:1.0.0-RC1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}