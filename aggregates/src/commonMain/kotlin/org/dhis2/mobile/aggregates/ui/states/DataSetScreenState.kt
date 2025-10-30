package org.dhis2.mobile.aggregates.ui.states

import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetRenderingConfig
import org.dhis2.mobile.aggregates.model.DataSetSection
import org.hisp.dhis.mobile.ui.designsystem.component.table.model.TableModel

internal sealed class DataSetScreenState {
    data class Loaded(
        val dataSetDetails: DataSetDetails,
        val dataSetSections: List<DataSetSection>,
        val initialSection: Int,
        val renderingConfig: DataSetRenderingConfig,
        val dataSetSectionTable: DataSetSectionTable,
        val selectedCellInfo: CellSelectionState,
        val modalDialog: DataSetModalDialogUIState? = null,
        val validationBar: ValidationBarUiState? = null,
    ) : DataSetScreenState() {
        override fun allowTwoPane(canUseTwoPane: Boolean) = dataSetSections.isNotEmpty() && canUseTwoPane && renderingConfig.useVerticalTabs

        override fun currentSection(): String? = dataSetSectionTable.id

        override fun currentSectionData(): DataSetSection? = dataSetSections.find { it.uid == dataSetSectionTable.id }
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

internal data class DataSetSectionTable(
    val id: String?,
    val tableModels: List<TableModel>,
    val overridingDimensions: OverwrittenDimension,
    val loading: Boolean,
)

internal data class OverwrittenDimension(
    val overwrittenTableWidth: Map<String, Float> = emptyMap(),
    val overwrittenRowHeaderWidth: Map<String, Float> = emptyMap(),
    val overwrittenColumnWidth: Map<String, Map<Int, Float>> = emptyMap(),
)
