package org.dhis2.mobile.plugin.registry

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.dhis2.mobile.plugin.sdk.Dhis2Plugin
import org.dhis2.mobile.plugin.sdk.InjectionPoint

/**
 * In-memory registry of successfully loaded [Dhis2Plugin] instances.
 *
 * Plugins are registered after their DEX has been downloaded, verified, and instantiated.
 * The registry exposes a [StateFlow] so that injection-point Composables can observe
 * the plugin list reactively (e.g., when a plugin is loaded after the screen is already showing).
 */
class PluginRegistry {

    private val _plugins = MutableStateFlow<List<Dhis2Plugin>>(emptyList())

    /** All currently loaded plugins. */
    val plugins: StateFlow<List<Dhis2Plugin>> = _plugins.asStateFlow()

    /** Adds [plugin] to the registry. */
    fun register(plugin: Dhis2Plugin) {
        _plugins.update { current -> current + plugin }
    }

    /** Returns all plugins that target [injectionPoint]. */
    fun getPluginsForSlot(injectionPoint: InjectionPoint): List<Dhis2Plugin> =
        _plugins.value.filter { injectionPoint in it.metadata.injectionPoints }

    /** Removes all registered plugins (e.g. on user logout). */
    fun clear() {
        _plugins.update { emptyList() }
    }
}
