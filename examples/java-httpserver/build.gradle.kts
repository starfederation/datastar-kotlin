plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.20"
    application
}

repositories {
    mavenCentral()
}

application {
    mainClass = "dev.datastar.kotlin.examples.httpserver.ApplicationKt"
}

dependencies {
    implementation(project(":sdk"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}