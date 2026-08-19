package org.dhis2.mobile.plugin.security

/**
 * A pass-through class loader that refuses the classes [PluginClassLoaderPolicy] denies.
 *
 * Sits between a plugin's `InMemoryDexClassLoader` and the host APK's loader:
 *
 * ```
 * PathClassLoader (host APK)
 *   └── FilteringClassLoader          ← refuses D2Manager, GlobalContext, org.dhis2.*, …
 *         └── InMemoryDexClassLoader  ← the plugin's own classes
 * ```
 *
 * Refusing with [ClassNotFoundException] rather than a bespoke exception is deliberate: it is what
 * delegation already expects, so a plugin class whose name happens to collide with a denied host
 * prefix still resolves from the plugin's own DEX instead of failing outright.
 */
internal class FilteringClassLoader(
    parent: ClassLoader,
) : ClassLoader(parent) {
    override fun loadClass(
        name: String,
        resolve: Boolean,
    ): Class<*> {
        if (!PluginClassLoaderPolicy.isAllowed(name)) {
            throw ClassNotFoundException(
                "Class '$name' is not available to plugins. Plugin data access goes through " +
                    "Dhis2PluginContext.sdk, which is scoped to what the server granted.",
            )
        }
        return super.loadClass(name, resolve)
    }
}
