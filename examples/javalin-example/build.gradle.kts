plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.20"
    application
}

repositories {
    mavenCentral()
}

application {
    mainClass = "dev.datastar.kotlin.examples.javalin.JavalinApplicationKt"
}

dependencies {
    implementation("io.javalin:javalin:6.7.0")
    implementation(project(":sdk"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.slf4j:slf4j-simple:2.0.16")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}