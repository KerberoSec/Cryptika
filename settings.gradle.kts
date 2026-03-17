// Cryptika Messenger — Gradle Settings
// ----------------------------------------
// Defines the root project name and the module structure.
// All dependency repositories are declared here under dependencyResolutionManagement
// so individual modules cannot override them (FAIL_ON_PROJECT_REPOS enforces this).

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "CryptikaMessenger"
include(":app")
