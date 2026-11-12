package org.dhis2.mobile.plugin.registry

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.dhis2.mobile.plugin.domain.model.FormFieldPluginPlacement
import org.dhis2.mobile.plugin.domain.model.FormLayout
import org.dhis2.mobile.plugin.domain.model.FormLayoutElement
import org.dhis2.mobile.plugin.domain.model.placements
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

/**
 * A form's configured layout ready to render: only plugins whose bundle is installed are kept, so a
 * plugin that could not be downloaded leaves no gap.
 */
data class ResolvedFormLayout(
    val layout: FormLayout,
    val plugins: Map<String, FormFieldPlugin>,
)

data class PluginRegistryState(
    val plugins: Map<String, RegisteredPlugin> = emptyMap(),
    val layouts: Map<String, FormLayout> = emptyMap(),
) {
    /** The plugins configured for the form [formUid] whose bundle is installed, in configured order. */
    fun formFieldPlugins(formUid: String): List<FormFieldPlugin> =
        listOfNotNull(layouts[formUid])
            .placements()
            .mapNotNull { placement -> placement.appKey?.let(plugins::get)?.let { FormFieldPlugin(placement, it) } }

    /** The layout configured for the form [formUid], or null if the form keeps its native layout. */
    fun resolvedLayout(formUid: String): ResolvedFormLayout? {
        val layout = layouts[formUid] ?: return null
        val installed = formFieldPlugins(formUid).associateBy { it.placement.elementId }
        return ResolvedFormLayout(
            layout =
                layout.copy(
                    sections =
                        layout.sections.map { section ->
                            section.copy(
                                elements =
                                    section.elements.filter {
                                        it !is FormLayoutElement.Plugin || it.placement.elementId in installed
                                    },
                            )
                        },
                ),
            plugins = installed,
        )
    }
}

/** In-memory registry of installed plugins and where they are placed, observed by the forms. */
class PluginRegistry {
    private val _state = MutableStateFlow(PluginRegistryState())

    val state: StateFlow<PluginRegistryState> = _state.asStateFlow()

    fun register(plugin: RegisteredPlugin) {
        _state.update { it.copy(plugins = it.plugins + (plugin.key to plugin)) }
    }

    fun setLayouts(layouts: List<FormLayout>) {
        _state.update { it.copy(layouts = layouts.associateBy(FormLayout::formUid)) }
    }

    fun clear() {
        _state.value = PluginRegistryState()
    }
}
