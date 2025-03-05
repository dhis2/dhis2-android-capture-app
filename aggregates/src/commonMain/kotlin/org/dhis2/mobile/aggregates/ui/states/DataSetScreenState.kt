package org.dhis2.mobile.aggregates.ui.states

import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetRenderingConfig
import org.dhis2.mobile.aggregates.model.DataSetSection
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableModel

internal sealed class DataSetScreenState {

    data class Loaded(
        val dataSetDetails: DataSetDetails,
        val dataSetSections: List<DataSetSection>,
        val renderingConfig: DataSetRenderingConfig,
        val dataSetSectionTable: DataSetSectionTable,
        val selectedCellInfo: InputData? = null,
        val modalDialog: DataSetModalDialogUIState? = null,
        val validationBar: ValidationBarUiState? = null,
    ) : DataSetScreenState() {
        override fun allowTwoPane(canUseTwoPane: Boolean) =
            dataSetSections.isNotEmpty() && canUseTwoPane && renderingConfig.useVerticalTabs

        override fun currentSection(): String? = when (dataSetSectionTable) {
            is DataSetSectionTable.Loaded -> dataSetSectionTable.id
            is DataSetSectionTable.Loading -> null
        }

        override fun currentInput(): InputData? = selectedCellInfo
    }

    data object Loading : DataSetScreenState() {
        override fun allowTwoPane(canUseTwoPane: Boolean) = false
        override fun currentSection() = null
        override fun currentInput(): InputData? = null
    }

    abstract fun allowTwoPane(canUseTwoPane: Boolean): Boolean

    abstract fun currentSection(): String?
    abstract fun currentInput(): InputData?
}

internal sealed class DataSetSectionTable {
    data class Loaded(
        val id: String,
        val tableModels: List<TableModel>,
    ) : DataSetSectionTable()

    data object Loading : DataSetSectionTable()

    fun tables() = when (this) {
        Loading -> emptyList()
        is Loaded -> this.tableModels
    }
}
