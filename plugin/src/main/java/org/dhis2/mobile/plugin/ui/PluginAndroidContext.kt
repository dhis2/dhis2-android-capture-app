package org.dhis2.mobile.plugin.ui

import android.content.Context
import android.content.ContextWrapper

/**
 * The host [Context], but handing out the plugin's own class loader.
 *
 * Compose publishes the Android [Context] to every Composable through `LocalContext`, and
 * `Context.getClassLoader()` on the real one returns the app's `PathClassLoader` — which is exactly
 * the loader the plugin's
 * [FilteringClassLoader][org.dhis2.mobile.plugin.security.FilteringClassLoader] exists to keep it
 * away from. One property override closes that.
 *
 * Everything else delegates, so plugins keep working resources, themes, and system services.
 */
internal class PluginAndroidContext(
    base: Context,
    private val pluginClassLoader: ClassLoader,
) : ContextWrapper(base) {
    override fun getClassLoader(): ClassLoader = pluginClassLoader
}
