package org.dhis2.usescases.datasets.dataSetTable

import org.dhis2.usescases.datasets.CellData
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.internal.semantics.headerTestTag
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.internal.semantics.rowHeaderTestTag

internal data class PivotTestingData(
    val sectionIndex: Int,
    val sectionName: String,
    val rowTestTags: List<CellData>,
    val headerTestTags: List<CellData>,
)

internal val pivotTestingData = listOf(
    PivotTestingData(
        sectionIndex = 4,
        sectionName = "5",
        rowTestTags = listOf(
            CellData(
                testTag = rowHeaderTestTag("gbvX3pogf7p", "AdkHQiiJBB4"),
                label = "Consumption"
            ),
            CellData(
                testTag = rowHeaderTestTag("gbvX3pogf7p", "xp1wtEkhBmV"),
                label = "End Balance"
            ),
            CellData(
                testTag = rowHeaderTestTag("gbvX3pogf7p", "gpK9r0M1RFZ"),
                label = "Quantity to be ordered"
            ),
            CellData(
                testTag = rowHeaderTestTag("aN8uN5b15YG", "Z8aX3AkrDMS"),
                label = "On salary"
            ),
            CellData(
                testTag = rowHeaderTestTag("aN8uN5b15YG", "sNr1y5Qq1YQ"),
                label = "Not on salary"
            ),
        ),
        headerTestTags = listOf(
            CellData(
                testTag = headerTestTag("aN8uN5b15YG", 0, 1),
                label = "Community Health Assistant (CHA)"
            ),
            CellData(
                testTag = headerTestTag("aN8uN5b15YG", 0, 0),
                label = "CHO"
            ),
            CellData(
                testTag = headerTestTag("gbvX3pogf7p", 0, 1),
                label = "Commodities - Injectable Antibiotics"
            ),
            CellData(
                testTag = headerTestTag("gbvX3pogf7p", 0, 0),
                label = "Commodities - Magnesium Sulfate"
            ),
        ),
    ),
    PivotTestingData(
        sectionIndex = 12,
        sectionName = "13",
        rowTestTags = listOf(
            CellData(
                testTag = rowHeaderTestTag("t3aNCvHsoSn", "FbLZS3ueWbQ"),
                label = "0-11m"
            ),
            CellData(
                testTag = rowHeaderTestTag("t3aNCvHsoSn", "rEq3Hkd3XXH"),
                label = "12-59m"
            ),
            CellData(
                testTag = rowHeaderTestTag("t3aNCvHsoSn", "dUm5jaCTPBb"),
                label = "5-14y"
            ),
            CellData(
                testTag = rowHeaderTestTag("t3aNCvHsoSn", "ZZxYuoTCcDd"),
                label = "15y+"
            ),
            CellData(
                testTag = rowHeaderTestTag("t3aNCvHsoSn", "t3aNCvHsoSn_totals"),
                label = "Totals"
            ),
            CellData(
                testTag = rowHeaderTestTag("UnNIOt1uB0J", "D3E6Qfzjs6I"),
                label = "Under 5 years"
            ),
            CellData(
                testTag = rowHeaderTestTag("UnNIOt1uB0J", "PYddIHxZRKK"),
                label = "5 years and above"
            ),
            CellData(
                testTag = rowHeaderTestTag("UnNIOt1uB0J", "UnNIOt1uB0J_totals"),
                label = "Totals"
            ),
        ),
        headerTestTags = listOf(
            CellData(
                testTag = headerTestTag("UnNIOt1uB0J", 0, 1),
                label = "Inpatient malaria cases"
            ),
            CellData(
                testTag = headerTestTag("UnNIOt1uB0J", 0, 0),
                label = "Inpatient cases"
            ),
            CellData(
                testTag = headerTestTag("t3aNCvHsoSn", 0, 1),
                label = "ARI treated with antibiotics (pneumonia) follow-up"
            ),
            CellData(
                testTag = headerTestTag("t3aNCvHsoSn", 0, 0),
                label = "ARI treated with antibiotics (pneumonia) new"
            ),
        ),
    ),
    PivotTestingData(
        sectionIndex = 20,
        sectionName = "23",
        rowTestTags = listOf(
            CellData(
                testTag = rowHeaderTestTag("t3aNCvHsoSn_0", "FbLZS3ueWbQ"),
                label = "0-11m"
            ),
            CellData(
                testTag = rowHeaderTestTag("t3aNCvHsoSn_0", "rEq3Hkd3XXH"),
                label = "12-59m"
            ),
            CellData(
                testTag = rowHeaderTestTag("t3aNCvHsoSn_0", "dUm5jaCTPBb"),
                label = "5-14y"
            ),
            CellData(
                testTag = rowHeaderTestTag("t3aNCvHsoSn_0", "ZZxYuoTCcDd"),
                label = "15y+"
            ),
            CellData(
                testTag = rowHeaderTestTag("t3aNCvHsoSn_0", "t3aNCvHsoSn_0_totals"),
                label = "Totals"
            ),
            CellData(
                testTag = rowHeaderTestTag("aN8uN5b15YG_1", "Z8aX3AkrDMS"),
                label = "On salary"
            ),
            CellData(
                testTag = rowHeaderTestTag("aN8uN5b15YG_1", "sNr1y5Qq1YQ"),
                label = "Not on salary"
            ),
            CellData(
                testTag = rowHeaderTestTag("aN8uN5b15YG_1", "aN8uN5b15YG_1_totals"),
                label = "Totals"
            ),
        ),
        headerTestTags = listOf(
            CellData(
                testTag = headerTestTag("aN8uN5b15YG_3", 0, 0),
                label = "MCH Aide"
            ),
            CellData(
                testTag = headerTestTag("aN8uN5b15YG_1", 0, 0),
                label = "PH Aide"
            ),
            CellData(
                testTag = headerTestTag("t3aNCvHsoSn_2", 0, 0),
                label = "Onchocerciasis follow-up"
            ),
            CellData(
                testTag = headerTestTag("t3aNCvHsoSn_0", 0, 0),
                label = "Onchocerciasis new"
            ),
        ),
    ),
)