package org.dhis2.mobile.commons.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.data.OrgUnitTreeRepository
import org.dhis2.mobile.commons.model.internal.DomainOrgUnit
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FetchOrgUnitsTest {
    private val repository: OrgUnitTreeRepository = mock()
    private val fetchOrgUnits: FetchOrgUnits =
        FetchOrgUnits(
            orgUnitTreeRepository = repository,
        )

    @Test
    fun shouldSetParentTag() =
        runTest {
            whenever(repository.orgUnits("Hospital")) doReturn
                listOf(
                    DomainOrgUnit(
                        uid = "uid_parent_4",
                        label = "Hospital",
                        level = 3,
                        path = "uid_parent_1/uid_parent_2/uid_parent_21",
                        namePath = listOf("Spain", "Extremadura", "Salud"),
                    ),
                    DomainOrgUnit(
                        uid = "uid_parent_4",
                        label = "Hospital",
                        level = 3,
                        path = "uid_parent_1/uid_parent_3/uid_parent_31",
                        namePath = listOf("Spain", "Madrid", "Salud"),
                    ),
                )

            whenever(repository.orgUnitHasChildren(any())) doReturn false
            whenever(repository.countSelectedChildren(any(), any())) doReturn 0
            whenever(repository.canBeSelected(any())) doReturn true

            val result =
                fetchOrgUnits(
                    FetchOrgUnitsInput(
                        query = "Hospital",
                        selectedOrgUnits = emptyList(),
                    ),
                )
            with(result) {
                assertTrue(isSuccess)
                val list = getOrDefault(emptyList())
                assertEquals("Extremadura", list[0].tag)
                assertEquals("Madrid", list[1].tag)
            }
        }
}
