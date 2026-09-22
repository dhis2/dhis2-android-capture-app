package org.dhis2.mobile.plugin.registry

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.dhis2.mobile.plugin.sdk.Dhis2Plugin
import org.dhis2.mobile.plugin.sdk.Dhis2PluginContext
import org.dhis2.mobile.plugin.sdk.InjectionPoint
import org.dhis2.mobile.plugin.sdk.PluginMetadata
import org.dhis2.mobile.plugin.sdk.SlotArguments
import org.koin.core.KoinApplication
import timber.log.Timber
import java.io.File

/**
 * A loaded plugin paired with its server-side [metadata] and the filesystem root of its
 * extracted resources.
 *
 * [metadata] is the server dataStore configuration, not anything the plugin declared about
 * itself — it is the authority for the plugin's identity, injection points and slot
 * configuration.
 */
data class RegisteredPlugin(
    val plugin: Dhis2Plugin,
    val metadata: PluginMetadata,
    val resourceRoot: File,
    /**
     * The context this plugin renders with, built once at load time from [metadata].
     *
     * Deliberately not something the render path asks a factory for: a factory that mints a context
     * from caller-supplied metadata is a factory a plugin can call itself.
     */
    val context: Dhis2PluginContext,
    /**
     * The loader that defined this plugin's classes.
     *
     * Carried because the load pipeline can run more than once per process, and each run builds a
     * fresh loader — so the same plugin id can be backed by two generations of its own classes. The
     * render path keys on this to avoid handing one generation's state to the other.
     */
    val classLoader: ClassLoader,
    /** The plugin's private Koin container. Always present; see `PluginContainer`. */
    val koinApplication: KoinApplication,
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
        koinApplication: KoinApplication,
    ) {
        _plugins.update { current ->
            // Closing the outgoing container matters: re-registration happens on logout/re-login,
            // and a leaked container keeps every singleton the previous session built.
            current.firstOrNull { it.metadata.id == metadata.id }?.koinApplication?.close()

            current.filterNot { it.metadata.id == metadata.id } +
                RegisteredPlugin(plugin, metadata, resourceRoot, context, classLoader, koinApplication)
        }
    }

    /** Removes all registered plugins (e.g. on user logout). */
    fun clear() {
        _plugins.update { current ->
            current.forEach { it.koinApplication.close() }
            emptyList()
        }
    }
}

/**
 * The plugins in this list that target [injectionPoint].
 *
 * An extension on the list rather than a method on the registry, because the render path filters
 * collected state rather than taking a snapshot of it. One definition, so the slot rule cannot
 * differ between a snapshot read and a collected one.
 */
fun List<RegisteredPlugin>.forSlot(injectionPoint: InjectionPoint): List<RegisteredPlugin> =
    filter { injectionPoint in it.metadata.injectionPoints }

/**
 * The plugins in this list that render for [arguments].
 *
 * A slot that declares `requiresConfiguration` renders nowhere until an administrator configures it;
 * an unconfigured additive slot keeps applying everywhere.
 */
internal fun List<RegisteredPlugin>.forSlotArguments(arguments: SlotArguments): List<RegisteredPlugin> =
    forSlot(arguments.injectionPoint).filter { registered ->
        val config = registered.metadata.slotConfig[arguments.injectionPoint]
        when {
            config != null -> arguments.appliesTo(config)
            else -> !arguments.injectionPoint.requiresConfiguration
        }
    }

/**
 * The single plugin that replaces the host's own UI for [arguments], or null to keep it.
 *
 * Exclusive: when several claim the same occurrence the first in configuration order wins and the
 * rest are logged. Stacking full-screen layouts is not an option, and falling back to the host would
 * let one admin's typo disable another team's plugin.
 */
fun List<RegisteredPlugin>.selectReplacement(arguments: SlotArguments): RegisteredPlugin? {
    val candidates = forSlotArguments(arguments)
    if (candidates.size > 1) {
        Timber.w(
            "%d plugins claim %s; rendering '%s' and ignoring %s",
            candidates.size,
            arguments.injectionPoint.name,
            candidates.first().metadata.id,
            candidates.drop(1).joinToString { it.metadata.id },
        )
    }
    return candidates.firstOrNull()
}
