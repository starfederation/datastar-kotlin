plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.allopen) apply false
    alias(libs.plugins.changelog)
}

val sdkVersion = providers.gradleProperty("version")

changelog {
    version = sdkVersion.get()
    introduction = "Datastar Kotlin SDK updates."
    combinePreReleases = false
}

tasks.register("verifyChangelog") {
    group = "verification"
    description = "Fails if CHANGELOG.md has no entry for the SDK version."
    val changelogFile = file("CHANGELOG.md")
    val version = sdkVersion.get()
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

tasks.register("verifyReadmeVersion") {
    group = "verification"
    description = "Fails if README.md references a different SDK version than gradle.properties."
    val readmeFile = file("README.md")
    val version = sdkVersion.get()
    inputs.file(readmeFile)
    inputs.property("version", version)
    doLast {
        val coordRef = Regex("""kotlin-sdk(?:-coroutines)?:([^"\s)]+)""")
        val xmlVersion = Regex("""<version>([^<]+)</version>""")
        val text = readmeFile.readText()
        val mismatches = buildList {
            coordRef.findAll(text).forEach { if (it.groupValues[1] != version) add(it.value) }
            xmlVersion.findAll(text).forEach { if (it.groupValues[1] != version) add(it.value) }
        }
        if (mismatches.isNotEmpty()) {
            throw GradleException(
                "README.md version references do not match SDK version $version: " +
                    mismatches.joinToString(", "),
            )
        }
    }
}
