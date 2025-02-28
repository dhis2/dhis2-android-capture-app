package org.dhis2.usescases.datasets

internal data class DisableAutomaticGroupingTestingData(
    val sectionIndex: Int,
    val sectionName: String,
    val dataElementsRowTestTags: List<CellData>,
)

internal val disableAutomaticGroupingList: List<DisableAutomaticGroupingTestingData> = listOf(
    DisableAutomaticGroupingTestingData(
        sectionIndex = 17,
        sectionName = "19",
        dataElementsRowTestTags = emptyList()
    ),
    DisableAutomaticGroupingTestingData(
        sectionIndex = 18,
        sectionName = "20",
        dataElementsRowTestTags = emptyList()
    ),
    DisableAutomaticGroupingTestingData(
        sectionIndex = 19,
        sectionName = "22",
        dataElementsRowTestTags = emptyList()
    ),
)