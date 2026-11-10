package org.dhis2.usescases.datasets.dataSetTable.extension

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.dhis2.mobile.aggregates.model.DataSetInstanceParameters
import org.dhis2.mobile.aggregates.ui.body.DataSetInstanceBody
import org.dhis2.mobile.aggregates.ui.body.DataSetInstanceBodyProvider
import org.dhis2.mobile.extension.host.domain.ResolveCustomForm
import org.dhis2.mobile.extension.host.model.DataSetInstanceRef
import org.dhis2.mobile.extension.host.model.FailureReason
import org.dhis2.mobile.extension.host.renderer.CustomFormRenderer
import org.dhis2.mobile.extension.host.reporting.ExtensionFailureReporter
import org.koin.dsl.module

/**
 * The only place `:app` joins `:aggregates` (the seam) to `:extension-host`; neither depends on
 * the other.
 */
class CustomFormBodyProvider(
    private val resolveCustomForm: ResolveCustomForm,
    private val renderer: CustomFormRenderer,
    private val reporter: ExtensionFailureReporter,
) : DataSetInstanceBodyProvider {
    override suspend fun bodyFor(parameters: DataSetInstanceParameters): DataSetInstanceBody? {
        val form = resolveCustomForm(parameters.dataSetUid) ?: return null
        val instance =
            DataSetInstanceRef(
                dataSetUid = parameters.dataSetUid,
                periodId = parameters.periodId,
                organisationUnitUid = parameters.organisationUnitUid,
                attributeOptionComboUid = parameters.attributeOptionComboUid,
            )
        return DataSetInstanceBody { modifier, contentPadding, editable, reloadKey, saveValue, onFailed ->
            renderer.Render(
                html = form.html,
                instance = instance,
                modifier = modifier,
                contentPadding = contentPadding,
                editable = editable,
                reloadKey = reloadKey,
                saveValue = saveValue,
                onFailed = { reason ->
                    reporter.report(form.extension, reason)
                    onFailed()
                },
            )
        }
    }
}

/**
 * SPIKE: stands in for the WebView renderer (`extension-web`) so the seam can be wired and
 * seen end to end. Never shipped.
 */
class PlaceholderCustomFormRenderer : CustomFormRenderer {
    override fun deviceSupport(): FailureReason? = null

    @Composable
    override fun Render(
        html: String,
        instance: DataSetInstanceRef,
        modifier: Modifier,
        contentPadding: PaddingValues,
        editable: Boolean,
        reloadKey: Int,
        saveValue: suspend (dataElementUid: String, categoryOptionComboUid: String, value: String?) -> String?,
        onFailed: (FailureReason) -> Unit,
    ) {
        Text(
            modifier = modifier.padding(contentPadding),
            text = "Custom form of ${instance.dataSetUid}: ${html.length} chars, editable=$editable, reload=$reloadKey",
        )
    }
}

val customFormBodyModule =
    module {
        factory<CustomFormRenderer> { PlaceholderCustomFormRenderer() }
        factory<DataSetInstanceBodyProvider> { CustomFormBodyProvider(get(), get(), get()) }
    }
