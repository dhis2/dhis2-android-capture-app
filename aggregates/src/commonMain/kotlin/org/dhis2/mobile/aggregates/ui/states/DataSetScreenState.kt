package org.dhis2.mobile.aggregates.ui.states

import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetRenderingConfig
import org.dhis2.mobile.aggregates.model.DataSetSection

sealed class DataSetScreenState {
    data class Loaded(
        val dataSetDetails: DataSetDetails,
        val dataSetSections: List<DataSetSection>,
        val renderingConfig: DataSetRenderingConfig,
    ) : DataSetScreenState() {
        override fun allowTwoPane(canUseTwoPane: Boolean) =
            canUseTwoPane && renderingConfig.useVerticalTabs
    }

    data object Loading : DataSetScreenState() {
        override fun allowTwoPane(canUseTwoPane: Boolean) = false
    }

    abstract fun allowTwoPane(canUseTwoPane: Boolean): Boolean
}
