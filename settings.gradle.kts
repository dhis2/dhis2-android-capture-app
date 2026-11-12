rootProject.name = "dhis2-android-capture-app"

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    @Suppress("UnstableApiUsage")
    repositories {
        mavenLocal()
        maven("https://central.sonatype.com/repository/maven-snapshots") {
            // Only DHIS2 snapshots (SDK, design system) live here. Without this filter every
            // dependency is looked up here first, and a 429 from Sonatype fails the build.
            content { includeGroupAndSubgroups("org.hisp.dhis") }
        }
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

include(
    ":app",
    ":dhis_android_analytics", ":form", ":commons",
    ":dhis2_android_maps", ":compose-table",
    ":stock-usecase"
)
include(":dhis2-mobile-program-rules")
include(":tracker")
include(":aggregates")
include(":commonskmm")
include(":login")
include(":sync")