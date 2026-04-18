import kotlinx.kover.gradle.plugin.dsl.CoverageUnit
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.spotless)
    alias(libs.plugins.kover)
    alias(libs.plugins.jreleaser)
    `java-library`
    `maven-publish`
}

val sdkCoroutinesProperties = Properties().apply {
    file("gradle.properties").inputStream().use { load(it) }
}

group = sdkCoroutinesProperties.getProperty("groupId")
version = sdkCoroutinesProperties.getProperty("version")

repositories {
    mavenCentral()
}

dependencies {
    api(project(":sdk-shared"))
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.kotlin.serialization.jvm)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.server.core)
    testImplementation(libs.ktor.server.netty)
    testImplementation(testFixtures(project(":sdk-shared")))

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    withSourcesJar()
    withJavadocJar()
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform {
        excludeTags("integration")
    }
}

tasks.register<Test>("integrationTest") {
    group = "verification"
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath

    useJUnitPlatform {
        includeTags("integration")
        systemProperty(
            "datastar.test-suite.version",
            sdkCoroutinesProperties.getProperty("datastar.test-suite.version"),
        )
    }
}

spotless {
    kotlin {
        ktlint(libs.versions.ktlint.get())
    }
}

kover {
    reports {
        total {
            html {
                onCheck = true
            }

            verify {
                this.onCheck = true
                rule {
                    this.minBound(100, coverageUnits = CoverageUnit.LINE)
                    this.minBound(100, coverageUnits = CoverageUnit.BRANCH)
                }
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = sdkCoroutinesProperties.getProperty("groupId")
            artifactId = sdkCoroutinesProperties.getProperty("artifactId")
            version = sdkCoroutinesProperties.getProperty("version")

            pom {
                name = "Datastar Kotlin SDK — Coroutines"
                description = "Fully-suspend Kotlin SDK for Datastar, for frameworks with suspending emit primitives."
                url = "https://github.com/starfederation/datastar-kotlin"
                inceptionYear = "2026"
                licenses {
                    license {
                        name = "MIT"
                        url = "https://github.com/starfederation/datastar/blob/develop/LICENSE.md"
                    }
                }
                developers {
                    developer {
                        id = "guillaumetaffin"
                        name = "Guillaume Taffin"
                    }
                }
                scm {
                    connection = "scm:git:https://github.com/starfederation/datastar-kotlin.git"
                    developerConnection = "scm:git:ssh://github.com/starfederation/datastar-kotlin.git"
                    url = "https://github.com/starfederation/datastar-kotlin"
                }
            }
        }
    }

    repositories {
        maven {
            url = file("build/staging-deploy").toURI()
        }
    }
}

jreleaser {
    gitRootSearch = false
    signing {
        setActive("ALWAYS")
        armored = true
    }
    deploy {
        maven {
            mavenCentral {
                register("maven-central") {
                    setActive("ALWAYS")
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository("build/staging-deploy")
                }
            }
        }
    }
}
