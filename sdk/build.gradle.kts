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

val sdkProperties = Properties().apply {
    file("gradle.properties").inputStream().use { load(it) }
}

group = sdkProperties.getProperty("groupId")
version = sdkProperties.getProperty("version")

repositories {
    mavenCentral()
}

dependencies {
    api(project(":sdk-shared"))

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.kotlin.serialization.jvm)
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
    useJUnitPlatform() {
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
            sdkProperties.getProperty("datastar.test-suite.version")
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
            groupId = sdkProperties.getProperty("groupId")
            artifactId = sdkProperties.getProperty("artifactId")
            version = sdkProperties.getProperty("version")

            pom {
                name = "Datastar Kotlin SDK"
                description = "A Kotlin SDK for Datastar"
                url = "https://github.com/starfederation/datastar-kotlin"
                inceptionYear = "2025"
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
                    connection = "scm:git:https://github.com/GuillaumeTaffin/datastar-kotlin.git"
                    developerConnection = "scm:git:ssh://github.com/GuillaumeTaffin/datastar-kotlin.git"
                    url = "https://github.com/GuillaumeTaffin/datastar-kotlin"
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
