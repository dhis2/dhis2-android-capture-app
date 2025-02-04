package org.dhis2.mobile.aggregates.ui.states

import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetRenderingConfig
import org.dhis2.mobile.aggregates.model.DataSetSection
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableModel

sealed class DataSetScreenState {
    data class Loaded(
        val dataSetDetails: DataSetDetails,
        val dataSetSections: List<DataSetSection>,
        val renderingConfig: DataSetRenderingConfig,
        val dataSetSectionTable: DataSetSectionTable,
    ) : DataSetScreenState() {
        override fun allowTwoPane(canUseTwoPane: Boolean) =
            dataSetSections.isNotEmpty() && canUseTwoPane && renderingConfig.useVerticalTabs
    }

    data object Loading : DataSetScreenState() {
        override fun allowTwoPane(canUseTwoPane: Boolean) = false
    }

    abstract fun allowTwoPane(canUseTwoPane: Boolean): Boolean
}

sealed class DataSetSectionTable {
    data class Loaded(
        val tableModels: List<TableModel>,
    ) : DataSetSectionTable()

    data object Loading : DataSetSectionTable()

    fun tables() = when (this) {
        Loading -> emptyList()
        is Loaded -> this.tableModels
    }
}
