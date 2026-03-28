import java.util.*

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.allopen) apply false
    alias(libs.plugins.changelog)
}

val sdkProperties = Properties().apply {
    file("sdk/gradle.properties").inputStream().use { load(it) }
}

changelog {
    version = sdkProperties.getProperty("version")
    introduction = "Datastar Kotlin SDK updates."
    combinePreReleases = false
}

