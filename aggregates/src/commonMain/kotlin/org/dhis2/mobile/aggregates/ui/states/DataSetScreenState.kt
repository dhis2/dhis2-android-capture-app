package org.dhis2.mobile.aggregates.ui.states

import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetSection

data class DataSetScreenState(
    val dataSetDetails: DataSetDetails,
    val dataSetSections: List<DataSetSection>,
    val useTwoPane: Boolean,
    val test: String,
) {
    fun titleLabel(): String {
        return test.ifEmpty {
            dataSetDetails.titleLabel
        }
    }
}

inline fun previewDataSetScreenState(
    useTwoPane: Boolean,
    numberOfTabs: Int,
    test: String,
) = DataSetScreenState(
    dataSetDetails = DataSetDetails(
        titleLabel = "Data set title",
        dateLabel = "Jan. 2024",
        orgUnitLabel = "Org. Unit",
        catOptionComboLabel = "Cat. Option Combo",
    ),
    dataSetSections = buildList {
        repeat(numberOfTabs) {
            add(
                DataSetSection("uid$it", "Section $it"),
            )
        }
    },
    useTwoPane = useTwoPane,
    test = test,
)
