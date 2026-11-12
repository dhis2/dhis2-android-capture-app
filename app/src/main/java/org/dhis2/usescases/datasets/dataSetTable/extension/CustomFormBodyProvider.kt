package org.dhis2.usescases.datasets.dataSetTable.extension

import org.dhis2.mobile.aggregates.model.DataSetInstanceParameters
import org.dhis2.mobile.aggregates.ui.body.DataSetInstanceBody
import org.dhis2.mobile.aggregates.ui.body.DataSetInstanceBodyProvider
import org.dhis2.mobile.extension.host.domain.ResolveCustomForm
import org.dhis2.mobile.extension.host.model.DataSetInstanceRef
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

/** The renderer comes from `extension-web` (`extensionWebModule`). */
val customFormBodyModule =
    module {
        factory<DataSetInstanceBodyProvider> { CustomFormBodyProvider(get(), get(), get()) }
    }
