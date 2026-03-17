# Plugin SDK interfaces must be kept so host app can load plugin classes via InMemoryDexClassLoader
-keep interface org.dhis2.mobile.plugin.sdk.** { *; }
-keep class org.dhis2.mobile.plugin.sdk.** { *; }
