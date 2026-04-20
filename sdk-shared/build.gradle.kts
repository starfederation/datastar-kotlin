import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.spotless)
    alias(libs.plugins.kover)
    alias(libs.plugins.jreleaser)
    `java-library`
    `java-test-fixtures`
    `maven-publish`
}

group = providers.gradleProperty("groupId").get()
version = providers.gradleProperty("version").get()

repositories {
    mavenCentral()
}

dependencies {
    testFixturesApi(libs.junit.jupiter)
    testFixturesApi(libs.kotlin.serialization.jvm)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.bundles.kotest)

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
    useJUnitPlatform()
}

spotless {
    kotlin {
        ktlint(libs.versions.ktlint.get())
    }
}

kover {
    reports {
        filters {
            excludes {
                packages("dev.datastar.kotlin.sdk.testfixtures")
            }
        }
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
            artifactId = "kotlin-sdk-shared"

            pom {
                name = "Datastar Kotlin SDK — Shared Types"
                description = "Pure types shared by the Datastar Kotlin SDK flavors (blocking and coroutines)."
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
