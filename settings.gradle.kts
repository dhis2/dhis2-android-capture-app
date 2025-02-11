include(
    ":app",
    ":dhis_android_analytics", ":form", ":commons",
    ":dhis2_android_maps", ":compose-table", ":ui-components",
    ":stock-usecase"
)
include(":dhis2-mobile-program-rules")
include(":tracker")
include(":aggregates")
include(":commonskmm")
dependencyResolutionManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}