import java.util.*

plugins {
    id("org.jetbrains.changelog") version "2.4.0"
}

val sdkProperties = Properties().apply {
    file("sdk/gradle.properties").inputStream().use { load(it) }
}

changelog {
    version = sdkProperties.getProperty("version")
    introduction = "Datastar Kotlin SDK updates."
    combinePreReleases = false
}

