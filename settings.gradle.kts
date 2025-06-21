rootProject.name = "MyImageApp"
include(":app")

pluginManagement {
    repositories {
        google() // Don't restrict content here — plugin resolution needs full access
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


