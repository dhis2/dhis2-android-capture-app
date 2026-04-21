package org.dhis2.mobile.plugin.registry

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.dhis2.mobile.plugin.sdk.Dhis2Plugin
import org.dhis2.mobile.plugin.sdk.InjectionPoint
import java.io.File

/** A loaded plugin paired with the filesystem root of its extracted resources. */
data class RegisteredPlugin(
    val plugin: Dhis2Plugin,
    val resourceRoot: File,
)

/**
 * In-memory registry of successfully loaded plugin instances.
 *
 * Plugins are registered after their bundle has been downloaded, verified, extracted, and
 * instantiated. The registry exposes a [StateFlow] so that injection-point Composables can
 * observe the plugin list reactively.
 */
class PluginRegistry {

    private val _plugins = MutableStateFlow<List<RegisteredPlugin>>(emptyList())

    /** All currently registered plugins. */
    val plugins: StateFlow<List<RegisteredPlugin>> = _plugins.asStateFlow()

    /** Adds [plugin] with its associated [resourceRoot] to the registry. */
    fun register(plugin: Dhis2Plugin, resourceRoot: File) {
        _plugins.update { current -> current + RegisteredPlugin(plugin, resourceRoot) }
    }

    /** Returns all plugins targeting [injectionPoint]. */
    fun getPluginsForSlot(injectionPoint: InjectionPoint): List<RegisteredPlugin> =
        _plugins.value.filter { injectionPoint in it.plugin.metadata.injectionPoints }

    /** Removes all registered plugins (e.g. on user logout). */
    fun clear() {
        _plugins.update { emptyList() }
    }
}
