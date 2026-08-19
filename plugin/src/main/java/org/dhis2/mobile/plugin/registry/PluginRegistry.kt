package org.dhis2.mobile.plugin.registry

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.dhis2.mobile.plugin.sdk.Dhis2Plugin
import org.dhis2.mobile.plugin.sdk.Dhis2PluginContext
import org.dhis2.mobile.plugin.sdk.InjectionPoint
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import org.koin.core.KoinApplication
import java.io.File

/**
 * A loaded plugin paired with its server-side [metadata], the filesystem root of its extracted
 * resources, and the scoped [context] it will be rendered with.
 *
 * [metadata] is the server dataStore configuration, not anything the plugin declared about
 * itself — it is the authority for the plugin's identity, injection points and data scope.
 *
 * [context] is built once here, at load time, from that metadata. It is deliberately *not*
 * something a Composable asks a factory for at render time: a factory that mints a context from
 * caller-supplied metadata is a factory a plugin can ask for its own unrestricted context.
 */
data class RegisteredPlugin(
    val plugin: Dhis2Plugin,
    val metadata: PluginMetadata,
    val resourceRoot: File,
    val context: Dhis2PluginContext,
    val classLoader: ClassLoader,
    /** The plugin's private Koin container, or null if it declared no module. */
    val koinApplication: KoinApplication?,
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
        context: Dhis2PluginContext,
        classLoader: ClassLoader,
        koinApplication: KoinApplication? = null,
    ) {
        _plugins.update { current ->
            // Closing the outgoing container matters: re-registration happens on logout/re-login,
            // and a leaked container keeps every singleton the previous session built.
            current.firstOrNull { it.metadata.id == metadata.id }?.koinApplication?.close()

            current.filterNot { it.metadata.id == metadata.id } +
                RegisteredPlugin(plugin, metadata, resourceRoot, context, classLoader, koinApplication)
        }
    }

    /** Returns all plugins the server configured for [injectionPoint]. */
    fun getPluginsForSlot(injectionPoint: InjectionPoint): List<RegisteredPlugin> =
        _plugins.value.filter { injectionPoint in it.metadata.injectionPoints }

    /** Removes all registered plugins and closes their private containers (e.g. on user logout). */
    fun clear() {
        _plugins.update { current ->
            current.forEach { it.koinApplication?.close() }
            emptyList()
        }
    }
}
