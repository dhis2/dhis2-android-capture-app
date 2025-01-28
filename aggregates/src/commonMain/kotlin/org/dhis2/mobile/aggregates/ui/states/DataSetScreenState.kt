package org.dhis2.mobile.aggregates.ui.states

import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetSection

sealed class ScreenState {
    data class DataSetScreenState(
        val dataSetDetails: DataSetDetails,
        val dataSetSections: List<DataSetSection>,
    ) : ScreenState()

    data object Loading : ScreenState()
}

inline fun previewDataSetScreenState(
    dataSetDetails: DataSetDetails = DataSetDetails(
        titleLabel = "Data set title",
        dateLabel = "Jan. 2024",
        orgUnitLabel = "Org. Unit",
        catOptionComboLabel = "Cat. Option Combo",
    ),
    numberOfTabs: Int,
) = ScreenState.DataSetScreenState(
    dataSetDetails = dataSetDetails,
    dataSetSections = buildList {
        repeat(numberOfTabs) {
            add(
                DataSetSection("uid$it", "Section $it"),
            )
        }
    },
)
