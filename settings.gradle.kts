include(
    ":app", ":viewpagerdotsindicator", ":core",
    ":dhis_android_analytics", ":form", ":commons",
    ":dhis2_android_maps", ":compose-table", ":ui-components",
    ":stock-usecase"
)
project(":core").projectDir = File("dhis2-android-sdk/core")
