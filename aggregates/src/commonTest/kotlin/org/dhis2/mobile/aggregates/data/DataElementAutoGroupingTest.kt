package org.dhis2.mobile.aggregates.data

import org.dhis2.mobile.aggregates.model.CellElement
import org.junit.Test
import kotlin.test.assertTrue

internal class DataElementAutoGroupingTest {
    private val dataElements: List<CellElement> =
        listOf(
            CellElement(
                uid = "dataElement0",
                categoryComboUid = "catCombo1",
                label = "opv0",
                description = null,
                isMultiText = false,
            ),
            CellElement(
                uid = "dataElement1",
                categoryComboUid = "catCombo1",
                label = "opv1",
                description = null,
                isMultiText = false,
            ),
            CellElement(
                uid = "dataElement2",
                categoryComboUid = "catCombo2",
                label = "opv2_alt",
                description = null,
                isMultiText = false,
            ),
            CellElement(
                uid = "dataElement3",
                categoryComboUid = "catCombo1",
                label = "opv2",
                description = null,
                isMultiText = false,
            ),
            CellElement(
                uid = "dataElement4",
                categoryComboUid = "catCombo1",
                label = "opv3",
                description = null,
                isMultiText = false,
            ),
        )

    @Test
    fun testAutoGrouping() {
        val result = DisableDataElementGrouping(dataElements)
        assertTrue {
            result.size == 3 &&
                result[0].size == 2 &&
                result[1].size == 1 &&
                result[2].size == 2
        }
    }
}
