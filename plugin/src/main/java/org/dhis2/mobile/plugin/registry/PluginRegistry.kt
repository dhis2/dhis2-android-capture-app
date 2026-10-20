package org.dhis2.mobile.plugin.registry

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.dhis2.mobile.plugin.domain.model.FormFieldPluginPlacement
import java.io.File

/** An installed plugin bundle, ready to render: the app it came from and where it was extracted. */
data class RegisteredPlugin(
    val key: String,
    val version: String,
    val entry: String,
    val bundleDir: File,
)

/** A placement whose plugin is installed. */
data class FormFieldPlugin(
    val placement: FormFieldPluginPlacement,
    val plugin: RegisteredPlugin,
)

data class PluginRegistryState(
    val plugins: Map<String, RegisteredPlugin> = emptyMap(),
    val placements: List<FormFieldPluginPlacement> = emptyList(),
) {
    /** The plugins configured for the form [formUid] whose bundle is installed, in configured order. */
    fun formFieldPlugins(formUid: String): List<FormFieldPlugin> =
        placements
            .filter { it.formUid == formUid }
            .mapNotNull { placement -> placement.appKey?.let(plugins::get)?.let { FormFieldPlugin(placement, it) } }
}

/** In-memory registry of installed plugins and where they are placed, observed by the forms. */
class PluginRegistry {
    private val _state = MutableStateFlow(PluginRegistryState())

    val state: StateFlow<PluginRegistryState> = _state.asStateFlow()

    fun register(plugin: RegisteredPlugin) {
        _state.update { it.copy(plugins = it.plugins + (plugin.key to plugin)) }
    }

    fun setPlacements(placements: List<FormFieldPluginPlacement>) {
        _state.update { it.copy(placements = placements) }
    }

    fun clear() {
        _state.value = PluginRegistryState()
    }
}
