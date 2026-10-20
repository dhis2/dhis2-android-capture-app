package org.dhis2.mobile.plugin.web

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import org.dhis2.mobile.plugin.data.FormPluginContext
import org.dhis2.mobile.plugin.data.FormPluginContextRepository
import org.dhis2.mobile.plugin.data.PluginFieldMetadataRepository
import org.dhis2.mobile.plugin.domain.model.FormPluginTarget
import org.dhis2.mobile.plugin.domain.model.PluginFieldMetadata
import org.dhis2.mobile.plugin.domain.model.PluginFieldState
import org.dhis2.mobile.plugin.registry.FormFieldPlugin
import org.dhis2.mobile.plugin.registry.PluginRegistry
import org.koin.compose.koinInject

/**
 * Renders the Capture form field plugins configured for the form editing [target], one under another.
 * Renders nothing when none is configured, so a form can always include it.
 *
 * @param fieldStates The form's current field states by uid; a plugin sees only its mapped fields.
 * @param onFieldValue Called with `uid` and value when a plugin writes a field it was given.
 */
@Composable
fun FormFieldPluginSlot(
    target: FormPluginTarget,
    fieldStates: Map<String, PluginFieldState>,
    onFieldValue: (uid: String, value: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val contextRepository = koinInject<FormPluginContextRepository>()
    val context by produceState<FormPluginContext?>(null, target) { value = contextRepository.context(target) }
    val formUid = context?.formUid ?: return

    val registryState by koinInject<PluginRegistry>().state.collectAsState()
    val plugins = remember(registryState, formUid) { registryState.formFieldPlugins(formUid) }
    if (plugins.isEmpty()) return

    Column(modifier = modifier) {
        plugins.forEach { formFieldPlugin ->
            key(formFieldPlugin.placement.elementId, formFieldPlugin.plugin.version) {
                FormFieldPluginView(formFieldPlugin, fieldStates, context?.orgUnitUid, onFieldValue)
            }
        }
    }
}

@Composable
private fun FormFieldPluginView(
    formFieldPlugin: FormFieldPlugin,
    fieldStates: Map<String, PluginFieldState>,
    orgUnitUid: String?,
    onFieldValue: (uid: String, value: String?) -> Unit,
) {
    val placement = formFieldPlugin.placement
    val metadataRepository = koinInject<PluginFieldMetadataRepository>()
    val metadata by produceState<Map<String, PluginFieldMetadata>?>(null, placement) {
        value = metadataRepository.metadata(placement.fields.map { it.uid })
    }
    val fieldMetadata = metadata ?: return

    WebPlugin(
        plugin = formFieldPlugin.plugin,
        props = formFieldPluginProps(placement, fieldStates, fieldMetadata, orgUnitUid, viewMode = false),
        onCallback = { callback ->
            formFieldPluginWrite(placement, callback)?.let { (uid, value) -> onFieldValue(uid, value) }
        },
    )
}
