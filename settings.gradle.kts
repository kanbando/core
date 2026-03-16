pluginManagement {
    includeBuild("../build/build-logic")
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../build/gradle/libs.versions.toml"))
        }
    }
}

includeBuild("../build")

rootProject.name = "core"
