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

tasks.register("verifyChangelog") {
    group = "verification"
    description = "Fails if CHANGELOG.md has no entry for the SDK version."
    val version = sdkProperties.getProperty("version")
    val changelogFile = file("CHANGELOG.md")
    inputs.file(changelogFile)
    inputs.property("version", version)
    doLast {
        val heading = Regex("^##\\s+\\Q$version\\E(\\s|$)", RegexOption.MULTILINE)
        if (!heading.containsMatchIn(changelogFile.readText())) {
            throw GradleException(
                "CHANGELOG.md has no entry for version $version. " +
                    "Add a '## $version - <YYYY-MM-DD>' section (promote the Unreleased section).",
            )
        }
    }
}

