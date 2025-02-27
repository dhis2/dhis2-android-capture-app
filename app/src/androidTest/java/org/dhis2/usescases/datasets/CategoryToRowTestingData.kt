package org.dhis2.usescases.datasets

import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.internal.semantics.headerTestTag
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.internal.semantics.rowHeaderTestTag

internal data class CategoryToRowTestingData(
    val sectionIndex: Int,
    val sectionName: String,
    val dataElementsRowTestTags:List<String>,
    val rowTestTags: List<String>,
    val numberOfDataElements:Int,
    val headerTestTags: List<String>,
    val pivotedHeaderTestTags: List<String>
)

internal val categoryToRowList = listOf(
    CategoryToRowTestingData(
        sectionIndex = 7,
        sectionName = "8",
        numberOfDataElements = 2,
        dataElementsRowTestTags = listOf(
            rowHeaderTestTag("t3aNCvHsoSn", "P3jJH5Tu5VC"), //Data element AFP follow-up
        ),
        rowTestTags = listOf(
            rowHeaderTestTag("t3aNCvHsoSn", "FbLZS3ueWbQ"), //cat option 0-11m
            rowHeaderTestTag("t3aNCvHsoSn", "rEq3Hkd3XXH"), //cat option 12-59m
            rowHeaderTestTag("t3aNCvHsoSn", "dUm5jaCTPBb"), //cat option 5-14y
            rowHeaderTestTag("t3aNCvHsoSn", "ZZxYuoTCcDd"), //cat option 15+
        ),
        headerTestTags = emptyList(),
        pivotedHeaderTestTags = listOf(
            headerTestTag("t3aNCvHsoSn", 0, 0), //cat option 0-11m
            headerTestTag("t3aNCvHsoSn", 0, 1), //cat option 12-59m
            headerTestTag("t3aNCvHsoSn", 0, 2), //cat option 5-14y
            headerTestTag("t3aNCvHsoSn", 0, 3), //cat option 15+
        )
    ),
    CategoryToRowTestingData(
        sectionIndex = 16,
        sectionName = "18",
        numberOfDataElements = 2,
        dataElementsRowTestTags = listOf(),
        rowTestTags = emptyList(),
        headerTestTags = emptyList(),
        pivotedHeaderTestTags = emptyList()
    ),
    CategoryToRowTestingData(
        sectionIndex = 20,
        sectionName = "23",
        numberOfDataElements = 2,
        dataElementsRowTestTags = listOf(),
        rowTestTags = emptyList(),
        headerTestTags = emptyList(),
        pivotedHeaderTestTags = emptyList()
    )
)