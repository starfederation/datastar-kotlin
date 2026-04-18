val kotlin_version: String by project
val logback_version: String by project

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("io.ktor.plugin") version "3.2.3"
}

group = "dev.datastar.kotlin"
version = "0.0.1"

repositories {
    mavenCentral()
}

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencies {
    implementation(project(":sdk-coroutines"))
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml")
    testImplementation("io.ktor:ktor-server-test-host")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
