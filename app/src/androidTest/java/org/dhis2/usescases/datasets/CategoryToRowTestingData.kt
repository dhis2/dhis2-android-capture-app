package org.dhis2.usescases.datasets

import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.internal.semantics.headerTestTag
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.internal.semantics.rowHeaderTestTag

internal data class CategoryToRowTestingData(
    val sectionIndex: Int,
    val sectionName: String,
    val dataElementsRowTestTags: List<CellData>,
    val rowTestTags: List<CellData>,
    val numberOfDataElements: Int,
    val headerTestTags: List<CellData>,
    val pivotedHeaderTestTags: List<CellData>
)

data class CellData(
    val testTag: String,
    val label: String,
)

internal val categoryToRowList = listOf(
    CategoryToRowTestingData(
        sectionIndex = 7,
        sectionName = "8",
        numberOfDataElements = 2,
        dataElementsRowTestTags = listOf(
            CellData(
                rowHeaderTestTag("t3aNCvHsoSn", "P3jJH5Tu5VC"),
                "Acute Flaccid Paralysis (AFP) follow-up"
            )
        ),
        rowTestTags = listOf(
            CellData(
                rowHeaderTestTag("t3aNCvHsoSn", "FbLZS3ueWbQ"),
                "0-11m"
            ),
            CellData(
                rowHeaderTestTag("t3aNCvHsoSn", "rEq3Hkd3XXH"),
                "12-59m"
            ),
            CellData(
                rowHeaderTestTag("t3aNCvHsoSn", "dUm5jaCTPBb"),
                "5-14y"
            ),
            CellData(
                rowHeaderTestTag("t3aNCvHsoSn", "ZZxYuoTCcDd"),
                "15y+"
            ),
        ),
        headerTestTags = emptyList(),
        pivotedHeaderTestTags = listOf(
            CellData(
                headerTestTag("t3aNCvHsoSn", 0, 0),
                "0-11m"
            ),
            CellData(
                headerTestTag("t3aNCvHsoSn", 0, 1),
                "12-59m"
            ),
            CellData(
                headerTestTag("t3aNCvHsoSn", 0, 2),
                "5-14y"
            ),
            CellData(
                headerTestTag("t3aNCvHsoSn", 0, 3),
                "15y+"
            ),
        )
    ),
    CategoryToRowTestingData(
        sectionIndex = 15,
        sectionName = "16",
        numberOfDataElements = 4,
        dataElementsRowTestTags = listOf(
            CellData(
                rowHeaderTestTag("t3aNCvHsoSn", "DWLCM68Q7Zl"),
                "Otitis media new"
            ),
        ),
        rowTestTags = listOf(
            CellData(
                rowHeaderTestTag("t3aNCvHsoSn", "FbLZS3ueWbQ"),
                "0-11m"
            ),
            CellData(
                rowHeaderTestTag("t3aNCvHsoSn", "rEq3Hkd3XXH"),
                "12-59m"
            ),
            CellData(
                rowHeaderTestTag("t3aNCvHsoSn", "dUm5jaCTPBb"),
                "5-14y"
            ),
            CellData(
                rowHeaderTestTag("t3aNCvHsoSn", "ZZxYuoTCcDd"),
                "15y+"
            ),
        ),
        headerTestTags = emptyList(),
        pivotedHeaderTestTags = listOf(
            CellData(
                headerTestTag("t3aNCvHsoSn", 0, 0),
                "0-11m"
            ),
            CellData(
                headerTestTag("t3aNCvHsoSn", 0, 1),
                "12-59m"
            ),
            CellData(
                headerTestTag("t3aNCvHsoSn", 0, 2),
                "5-14y"
            ),
            CellData(
                headerTestTag("t3aNCvHsoSn", 0, 3),
                "15y+"
            ),
        )
    ),
    CategoryToRowTestingData(
        sectionIndex = 21,
        sectionName = "24",
        numberOfDataElements = 4,
        dataElementsRowTestTags = listOf(
            CellData(
                rowHeaderTestTag("aN8uN5b15YG_1", "zgeAdnpSY5K"),
                "Porter"
            ),
        ),
        rowTestTags = listOf(
            CellData(
                rowHeaderTestTag("aN8uN5b15YG_1", "Z8aX3AkrDMS"),
                "On salary"
            ),
            CellData(
                rowHeaderTestTag("aN8uN5b15YG_1", "sNr1y5Qq1YQ"),
                "Not on salary"
            ),
        ),
        headerTestTags = listOf(),
        pivotedHeaderTestTags = listOf(
            CellData(
                headerTestTag("aN8uN5b15YG_1", 0,0),
                "On salary"
            ),
            CellData(
                headerTestTag("t3aNCvHsoSn_1", 0,1),
                "Not on salary"
            ),
        )
    )
)