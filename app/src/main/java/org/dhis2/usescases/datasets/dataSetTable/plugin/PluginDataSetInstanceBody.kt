package org.dhis2.usescases.datasets.dataSetTable.plugin

import org.dhis2.mobile.aggregates.model.DataSetInstanceParameters
import org.dhis2.mobile.aggregates.ui.DataSetInstanceBody
import org.dhis2.mobile.aggregates.ui.DataSetInstanceBodyProvider
import org.dhis2.mobile.plugin.registry.PluginRegistry
import org.dhis2.mobile.plugin.registry.selectReplacement
import org.dhis2.mobile.plugin.sdk.DataSetInstanceSlotArguments
import org.dhis2.mobile.plugin.ui.PluginReplacementSlot
import org.koin.dsl.module

/**
 * Lets a plugin configured for `DATA_SET_INSTANCE_CONTENT` draw the data set instance body.
 *
 * This is the only place `:app` joins `:aggregates` to the plugin modules; neither depends on the
 * other.
 */
class PluginDataSetInstanceBodyProvider(
    private val pluginRegistry: PluginRegistry,
) : DataSetInstanceBodyProvider {
    override fun bodyFor(parameters: DataSetInstanceParameters): DataSetInstanceBody? {
        val arguments = parameters.toSlotArguments()
        // Read once rather than collected: plugins load at login, long before a data set can open,
        // and the view model's matching decision is taken once too.
        val plugin = pluginRegistry.plugins.value.selectReplacement(arguments) ?: return null

        return DataSetInstanceBody { contentPadding, onHostRefresh ->
            PluginReplacementSlot(
                plugin = plugin,
                arguments = arguments,
                contentPadding = contentPadding,
                onHostRefresh = onHostRefresh,
            )
        }
    }

    private fun DataSetInstanceParameters.toSlotArguments() =
        DataSetInstanceSlotArguments(
            dataSetUid = dataSetUid,
            periodId = periodId,
            organisationUnitUid = organisationUnitUid,
            attributeOptionComboUid = attributeOptionComboUid,
        )
}

val dataSetInstanceBodyModule =
    module {
        factory<DataSetInstanceBodyProvider> { PluginDataSetInstanceBodyProvider(get()) }
    }
