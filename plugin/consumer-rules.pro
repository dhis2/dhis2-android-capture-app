# Plugin SDK types must be kept: the host resolves them reflectively when it loads a plugin's DEX
# via InMemoryDexClassLoader. `-keep class` already covers interfaces.
-keep class org.dhis2.mobile.plugin.sdk.** { *; }
