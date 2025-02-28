package org.dhis2.usescases.datasets

internal data class DisableAutomaticGroupingTestingData(
    val sectionIndex: Int,
    val sectionName: String,
    val dataElementsRowTestTags: List<CellData>,
    val tableIdTestTags: List<String>,
)

internal val disableAutomaticGroupingList: List<DisableAutomaticGroupingTestingData> = listOf(
    DisableAutomaticGroupingTestingData(
        sectionIndex = 17,
        sectionName = "19",
        dataElementsRowTestTags = emptyList(),
        tableIdTestTags = listOf(
            "t3aNCvHsoSn_0",
            "aN8uN5b15YG_1",
            "t3aNCvHsoSn_2",
            "aN8uN5b15YG_3"
        )
    ),
    DisableAutomaticGroupingTestingData(
        sectionIndex = 18,
        sectionName = "20",
        dataElementsRowTestTags = emptyList(),
        tableIdTestTags = listOf(
            "aN8uN5b15YG_0",
            "ck7mRNwGDjP_1",
            "aN8uN5b15YG_2",
            "ck7mRNwGDjP_3"
        ),
    ),
    DisableAutomaticGroupingTestingData(
        sectionIndex = 19,
        sectionName = "22",
        dataElementsRowTestTags = emptyList(),
        tableIdTestTags = listOf(
            "t3aNCvHsoSn_0",
            "aN8uN5b15YG_1",
            "t3aNCvHsoSn_2",
            "aN8uN5b15YG_3",
        ),
    ),
)