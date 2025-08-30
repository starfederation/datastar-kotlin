plugins {
    alias(libs.plugins.changelog)
}

version = providers.gradleProperty("version").get()

changelog {
    version = project.version.toString()
    introduction = "Datastar Kotlin SDK updates."
}