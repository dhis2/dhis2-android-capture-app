package org.dhis2.utils.filters

import java.util.Date
import org.dhis2.utils.filters.sorting.SortingItem
import org.dhis2.utils.filters.sorting.SortingStatus
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.DatePeriod
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FilterManagerTest {

    lateinit var filterManager: FilterManager

    @Before
    fun setUp() {
        filterManager = FilterManager.getInstance()
        filterManager.reset()
    }

    @Test
    fun `Reset should clear all current values`() {
        filterManager.addPeriod(
            arrayListOf(DatePeriod.create(Date(), Date()))
        )
        assertTrue(filterManager.totalFilters == 1)
        filterManager.reset()
        assertTrue(filterManager.totalFilters == 0)
        assertTrue(filterManager.observeField(Filters.ORG_UNIT).get() == 0)
        assertTrue(filterManager.observeField(Filters.SYNC_STATE).get() == 0)
        assertTrue(filterManager.observeField(Filters.PERIOD).get() == 0)
        assertTrue(filterManager.observeField(Filters.CAT_OPT_COMB).get() == 0)
        assertTrue(filterManager.observeField(Filters.EVENT_STATUS).get() == 0)
        assertTrue(filterManager.observeField(Filters.ASSIGNED_TO_ME).get() == 0)
    }

    @Test
    fun `Should only add one sync state filter if to_post, to_update and uploading are set`() {
        filterManager.addState(
            false,
            State.TO_POST, State.TO_UPDATE, State.UPLOADING
        )
        assertTrue(filterManager.totalFilters == 1)
        assertTrue(filterManager.observeField(Filters.SYNC_STATE).get() == 1)
    }

    @Test
    fun `Should add all event statuses`() {
        filterManager.addEventStatus(
            false,
            EventStatus.ACTIVE,
            EventStatus.OVERDUE,
            EventStatus.SCHEDULE,
            EventStatus.SKIPPED,
            EventStatus.COMPLETED,
            EventStatus.VISITED
        )
        assertTrue(filterManager.totalFilters == 1)
        assertTrue(filterManager.observeField(Filters.EVENT_STATUS).get() == 5)
    }

    @Test
    fun `Should add date periods`() {
        filterManager.addPeriod(
            arrayListOf(
                DatePeriod.create(Date(), Date()),
                DatePeriod.create(Date(), Date()),
                DatePeriod.create(Date(), Date()),
                DatePeriod.create(Date(), Date())
            )
        )
        assertTrue(filterManager.totalFilters == 1)
        assertTrue(filterManager.observeField(Filters.PERIOD).get() == 1)
    }

    @Test
    fun `Should add org unit filter`() {
        val ou1 = OrganisationUnit.builder().uid("ou1").build()
        filterManager.addOrgUnit(ou1)
        filterManager.addOrgUnit(ou1)
        filterManager.addOrgUnit(
            OrganisationUnit.builder().uid("ou2").build()
        )
        filterManager.addOrgUnit(
            OrganisationUnit.builder().uid("ou3").build()
        )
        assertTrue(filterManager.totalFilters == 1)
        assertTrue(filterManager.observeField(Filters.ORG_UNIT).get() == 2)
    }

    @Test
    fun `Should add cat opt combo filter`() {
        filterManager.addCatOptCombo(
            CategoryOptionCombo.builder().uid("catOptCombo").build()
        )
        filterManager.addCatOptCombo(
            CategoryOptionCombo.builder().uid("catOptCombo").build()
        )
        filterManager.addCatOptCombo(
            CategoryOptionCombo.builder().uid("catOptCombo2").build()
        )
        assertTrue(filterManager.totalFilters == 1)
        assertTrue(filterManager.observeField(Filters.CAT_OPT_COMB).get() == 1)
    }

    @Test
    fun `Should add assigned to me filter`() {
        filterManager.setAssignedToMe(true)
        assertTrue(filterManager.totalFilters == 1)
        assertTrue(filterManager.observeField(Filters.ASSIGNED_TO_ME).get() == 1)
    }

    @Test
    fun `Should add a sortingItem to filterManager`() {
        val sortingItem = SortingItem(Filters.ORG_UNIT, SortingStatus.ASC)

        filterManager.sortingItem = sortingItem

        assertTrue(filterManager.sortingItem.filterSelectedForSorting == Filters.ORG_UNIT)
        assertTrue(filterManager.sortingItem.sortingStatus == SortingStatus.ASC)
    }
}
