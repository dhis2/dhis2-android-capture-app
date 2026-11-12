package org.dhis2.mobile.plugin.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import org.dhis2.mobile.plugin.data.FormPluginContext
import org.dhis2.mobile.plugin.data.FormPluginContextRepository
import org.dhis2.mobile.plugin.data.PluginFieldMetadataRepository
import org.dhis2.mobile.plugin.data.PluginFieldValueRepository
import org.dhis2.mobile.plugin.domain.model.FormPluginTarget
import org.dhis2.mobile.plugin.domain.model.PluginFieldMetadata
import org.dhis2.mobile.plugin.domain.model.PluginFieldState
import org.dhis2.mobile.plugin.registry.FormFieldPlugin
import org.dhis2.mobile.plugin.registry.PluginRegistry
import org.dhis2.mobile.plugin.registry.ResolvedFormLayout
import org.koin.compose.koinInject

/** A form's configured layout and where the form is being filled, for drawing its plugins. */
data class FormPluginLayout(
    val target: FormPluginTarget,
    val resolved: ResolvedFormLayout,
    val orgUnitUid: String?,
)

/**
 * The layout configured in `capture/dataEntryForms` for the form editing [target], or null while it
 * is resolved or when the form has none and keeps its native layout.
 */
@Composable
fun rememberFormPluginLayout(target: FormPluginTarget): FormPluginLayout? {
    val contextRepository = koinInject<FormPluginContextRepository>()
    val context by produceState<FormPluginContext?>(null, target) { value = contextRepository.context(target) }
    val registryState by koinInject<PluginRegistry>().state.collectAsState()
    return remember(context, registryState) {
        context?.let { ctx -> registryState.resolvedLayout(ctx.formUid)?.let { FormPluginLayout(target, it, ctx.orgUnitUid) } }
    }
}

/**
 * One Capture form field plugin, drawn where the layout places it.
 *
 * @param fieldStates The form's current field states by uid, including fields the layout does not
 *   draw; the plugin sees only its mapped fields, with their stored values.
 * @param onFieldValue Called with `uid` and value when the plugin writes a field it was given.
 */
@Composable
fun FormFieldPluginItem(
    formFieldPlugin: FormFieldPlugin,
    layout: FormPluginLayout,
    fieldStates: Map<String, PluginFieldState>,
    onFieldValue: (uid: String, value: String?) -> Unit,
) {
    val placement = formFieldPlugin.placement
    val uids = placement.fields.map { it.uid }
    val metadataRepository = koinInject<PluginFieldMetadataRepository>()
    val metadata by produceState<Map<String, PluginFieldMetadata>?>(null, placement) {
        value = metadataRepository.metadata(uids)
    }
    // Re-read whenever the form's state changes, which is what follows every save.
    val valueRepository = koinInject<PluginFieldValueRepository>()
    val stored by produceState<Map<String, String?>?>(null, layout.target, fieldStates) {
        value = valueRepository.storedValues(layout.target, uids)
    }
    val fieldMetadata = metadata ?: return
    val storedValues = stored ?: return

    WebPlugin(
        plugin = formFieldPlugin.plugin,
        props =
            formFieldPluginProps(
                placement = placement,
                states = fieldStates.withStoredValues(storedValues),
                metadata = fieldMetadata,
                orgUnitId = layout.orgUnitUid,
                viewMode = false,
            ),
        onCallback = { callback ->
            formFieldPluginWrite(placement, callback)?.let { (uid, value) -> onFieldValue(uid, value) }
        },
    )
}
