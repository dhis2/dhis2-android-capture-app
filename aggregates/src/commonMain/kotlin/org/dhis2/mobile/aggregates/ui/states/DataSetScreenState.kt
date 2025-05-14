package org.dhis2.mobile.aggregates.ui.states

import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetRenderingConfig
import org.dhis2.mobile.aggregates.model.DataSetSection
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableModel
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.TableSelection

internal sealed class DataSetScreenState {

    data class Loaded(
        val dataSetDetails: DataSetDetails,
        val dataSetSections: List<DataSetSection>,
        val initialSection: Int,
        val renderingConfig: DataSetRenderingConfig,
        val dataSetSectionTable: DataSetSectionTable,
        val selectedCellInfo: InputDataUiState? = null,
        val modalDialog: DataSetModalDialogUIState? = null,
        val validationBar: ValidationBarUiState? = null,
        val nextCellSelection: Pair<TableSelection.CellSelection?, Boolean> = Pair(null, false),
    ) : DataSetScreenState() {
        override fun allowTwoPane(canUseTwoPane: Boolean) =
            dataSetSections.isNotEmpty() && canUseTwoPane && renderingConfig.useVerticalTabs

        override fun currentSection(): String? = when (dataSetSectionTable) {
            is DataSetSectionTable.Loaded -> dataSetSectionTable.id
            is DataSetSectionTable.Loading -> null
        }

        override fun currentSectionData(): DataSetSection? = when (dataSetSectionTable) {
            is DataSetSectionTable.Loaded -> dataSetSections.find { it.uid == dataSetSectionTable.id }
            is DataSetSectionTable.Loading -> null
        }
    }

    data object Loading : DataSetScreenState() {
        override fun allowTwoPane(canUseTwoPane: Boolean) = false
        override fun currentSection() = null
        override fun currentSectionData() = null
    }

    abstract fun allowTwoPane(canUseTwoPane: Boolean): Boolean

    abstract fun currentSection(): String?

    abstract fun currentSectionData(): DataSetSection?
}

internal sealed class DataSetSectionTable {
    data class Loaded(
        val id: String,
        val tableModels: List<TableModel>,
        val overridingDimensions: OverwrittenDimension,
    ) : DataSetSectionTable()

    data object Loading : DataSetSectionTable()

    fun tables() = when (this) {
        Loading -> emptyList()
        is Loaded -> this.tableModels
    }

    fun sectionId() = when (this) {
        Loading -> null
        is Loaded -> this.id
    }

    fun overridingDimensions() = when (this) {
        is Loaded -> overridingDimensions
        Loading -> null
    }
}

internal data class OverwrittenDimension(
    val overwrittenTableWidth: Map<String, Float> = emptyMap(),
    val overwrittenRowHeaderWidth: Map<String, Float> = emptyMap(),
    val overwrittenColumnWidth: Map<String, Map<Int, Float>> = emptyMap(),
)
