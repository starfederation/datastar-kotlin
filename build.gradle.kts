plugins {
    alias(libs.plugins.changelog)
}

version = providers.gradleProperty("version").get()

changelog {
    version = project.version.toString()
    introduction = "Datastar Kotlin SDK updates."
    combinePreReleases = false
}

tasks.register("buildExamples") {
    group = "build"
    description = "Build all included example projects"
    dependsOn(
        gradle.includedBuilds.map { it.task(":build") }
    )
}