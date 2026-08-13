package org.dhis2.mobile.plugin.registry

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.dhis2.mobile.plugin.sdk.Dhis2Plugin
import org.dhis2.mobile.plugin.sdk.InjectionPoint
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import java.io.File

/**
 * A loaded plugin paired with its server-side [metadata] and the filesystem root of its
 * extracted resources.
 *
 * [metadata] is the server dataStore configuration, not anything the plugin declared about
 * itself — it is the authority for the plugin's identity, injection points and data scope.
 */
data class RegisteredPlugin(
    val plugin: Dhis2Plugin,
    val metadata: PluginMetadata,
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

    /**
     * Adds [plugin], configured by [metadata], with its associated [resourceRoot].
     *
     * Registration is idempotent per plugin id: re-registering an already known plugin replaces
     * the previous entry instead of appending a duplicate. This keeps the registry correct when
     * the load pipeline runs more than once in a process (e.g. logout followed by re-login).
     */
    fun register(
        plugin: Dhis2Plugin,
        metadata: PluginMetadata,
        resourceRoot: File,
    ) {
        _plugins.update { current ->
            current.filterNot { it.metadata.id == metadata.id } +
                RegisteredPlugin(plugin, metadata, resourceRoot)
        }
    }

    /** Returns all plugins the server configured for [injectionPoint]. */
    fun getPluginsForSlot(injectionPoint: InjectionPoint): List<RegisteredPlugin> =
        _plugins.value.filter { injectionPoint in it.metadata.injectionPoints }

    /** Removes all registered plugins (e.g. on user logout). */
    fun clear() {
        _plugins.update { emptyList() }
    }
}
