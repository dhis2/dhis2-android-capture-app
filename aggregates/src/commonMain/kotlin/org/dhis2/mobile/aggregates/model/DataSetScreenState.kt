package org.dhis2.mobile.aggregates.model

data class DataSetScreenState(
    val dataSetDetails: DataSetDetails,
    val dataSetSections: List<DataSetSection>,
    val useTwoPane: Boolean,
)

inline fun previewDataSetScreenState(
    useTwoPane: Boolean,
    numberOfTabs: Int,
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
)
