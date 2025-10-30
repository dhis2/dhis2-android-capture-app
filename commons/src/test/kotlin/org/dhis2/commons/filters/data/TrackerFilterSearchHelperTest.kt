package org.dhis2.commons.filters.data

import org.dhis2.commons.filters.FilterManager
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TrackerFilterSearchHelperTest {
    private val filterRepository: FilterRepository = mock()

    private val filterManager: FilterManager = mock()

    private val trackerFilterSearchHelper =
        TrackerFilterSearchHelper(filterRepository, filterManager)

    @Test
    fun `should set org unit mode to ACCESIBLE`() {
        defaultSetup()
        val repository = trackerFilterSearchHelper.getFilteredProgramRepository("programUid")
        trackerFilterSearchHelper.applyFiltersTo(repository)
        verify(
            filterRepository,
        ).applyOrgUnitFilter(repository, OrganisationUnitMode.ACCESSIBLE, emptyList())
    }

    @Test
    fun `should set org unit mode to SELECTED`() {
        val selectedOrgUnits = listOf("orgUnitUid1", "orgUnitUid2")
        defaultSetup(orgUnitFilterList = selectedOrgUnits)
        val repository = trackerFilterSearchHelper.getFilteredProgramRepository("programUid")
        trackerFilterSearchHelper.applyFiltersTo(repository)
        verify(
            filterRepository,
        ).applyOrgUnitFilter(repository, OrganisationUnitMode.SELECTED, selectedOrgUnits)
    }

    private fun defaultSetup(orgUnitFilterList: List<String> = emptyList()) {
        whenever(filterRepository.trackedEntityInstanceQueryByProgram(any())) doReturn mock()
        whenever(filterRepository.trackedEntityInstanceQueryByType(any())) doReturn mock()
        whenever(filterManager.workingListActive()) doReturn false
        whenever(filterManager.enrollmentStatusFilters) doReturn emptyList()
        whenever(filterManager.eventStatusFilters) doReturn emptyList()
        whenever(filterManager.orgUnitUidsFilters) doReturn orgUnitFilterList
        whenever(filterRepository.applyOrgUnitFilter(any(), any(), any())) doReturn mock()
        whenever(filterManager.stateFilters) doReturn emptyList()
        whenever(filterManager.periodFilters) doReturn emptyList()
        whenever(filterManager.enrollmentPeriodFilters) doReturn emptyList()
        whenever(filterManager.assignedFilter) doReturn false
        whenever(filterManager.followUpFilter) doReturn false
        whenever(filterManager.sortingItem) doReturn null
    }
}
