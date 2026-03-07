pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "NexVault"
include(":app")
include(":core:core-ui")
include(":core:core-common")
include(":core:core-network")
include(":core:core-database")
include(":core:core-datastore")
include(":core:core-security")
include(":domain")
include(":data")
include(":feature:feature-onboarding")
include(":feature:feature-home")
include(":feature:feature-tokens")
include(":feature:feature-send")
include(":feature:feature-receive")
include(":feature:feature-history")
include(":feature:feature-dapp")
include(":feature:feature-nft")
include(":feature:feature-swap")
include(":feature:feature-settings")
