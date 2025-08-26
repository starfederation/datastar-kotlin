import kotlinx.kover.gradle.plugin.dsl.CoverageUnit

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.spotless)
    alias(libs.plugins.kover)
    alias(libs.plugins.jreleaser)
    `java-library`
    `maven-publish`
}

group = providers.gradleProperty("groupId").get()
version = providers.gradleProperty("version").get()

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.kotlin.serialization.jvm)

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
            providers.gradleProperty("datastar.test-suite.version").get()
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
            groupId = providers.gradleProperty("groupId").get()
            artifactId = providers.gradleProperty("artifactId").get()
            version = providers.gradleProperty("version").get()

            pom {
                name = "Datastar Kotlin SDK"
                description = "A Kotlin SDK for Datastar"
                url = "https://github.com/GuillaumeTaffin/datastar-kotlin"
                inceptionYear = "2025"
                licenses {
                    license {
                        name = "MIT"
                        url = "https://mit-license.org/"
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
