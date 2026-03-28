plugins {
    id("org.jetbrains.changelog") version "2.4.0"
}

val sdkProperties = java.util.Properties().apply {
    file("sdk/gradle.properties").inputStream().use { load(it) }
}

changelog {
    version = sdkProperties.getProperty("version")
    introduction = "Datastar Kotlin SDK updates."
    combinePreReleases = false
}

tasks.register("buildAll") {
    group = "build"
    description = "Build all included example projects"
    dependsOn(
        gradle.includedBuilds.map { it.task(":build") }
    )
}