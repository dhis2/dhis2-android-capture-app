package org.dhis2.utils.filters

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.Filters
import org.dhis2.commons.filters.data.EmptyWorkingList
import org.dhis2.commons.filters.data.EventWorkingListScope
import org.dhis2.commons.filters.data.TeiWorkingListScope
import org.dhis2.commons.filters.sorting.SortingItem
import org.dhis2.commons.filters.sorting.SortingStatus
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.AssignedUserMode
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.DatePeriod
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import java.util.Date

class FilterManagerTest {

    private val resourceManger: ResourceManager = mock()

    @Rule
    @JvmField
    var instantExecutorRule = InstantTaskExecutorRule()

    lateinit var filterManager: FilterManager

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        filterManager = FilterManager.initWith(resourceManger)
        filterManager.reset()
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()
    }

    @Test
    fun `Reset should clear all current values`() {
        filterManager.addPeriod(
            arrayListOf(DatePeriod.create(Date(), Date())),
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
    fun `Should add all event statuses`() {
        filterManager.addEventStatus(
            false,
            EventStatus.ACTIVE,
            EventStatus.OVERDUE,
            EventStatus.SCHEDULE,
            EventStatus.SKIPPED,
            EventStatus.COMPLETED,
            EventStatus.VISITED,
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
                DatePeriod.create(Date(), Date()),
            ),
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
            OrganisationUnit.builder().uid("ou2").build(),
        )
        filterManager.addOrgUnit(
            OrganisationUnit.builder().uid("ou3").build(),
        )
        assertTrue(filterManager.totalFilters == 1)
        assertTrue(filterManager.observeField(Filters.ORG_UNIT).get() == 2)
    }

    @Test
    fun `Should add cat opt combo filter`() {
        filterManager.addCatOptCombo(
            CategoryOptionCombo.builder().uid("catOptCombo").build(),
        )
        filterManager.addCatOptCombo(
            CategoryOptionCombo.builder().uid("catOptCombo").build(),
        )
        filterManager.addCatOptCombo(
            CategoryOptionCombo.builder().uid("catOptCombo2").build(),
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

    @Test
    fun `Should not count enrollment filters in total if they are in unsupported list`() {
        filterManager.setUnsupportedFilters(Filters.ENROLLMENT_DATE, Filters.ENROLLMENT_STATUS)

        filterManager.addEnrollmentPeriod(listOf(DatePeriod.create(Date(), Date())))
        filterManager.addEnrollmentStatus(false, EnrollmentStatus.ACTIVE)
        filterManager.addEventStatus(false, EventStatus.ACTIVE)

        assertTrue(filterManager.totalFilters == 1)
    }

    @Test
    fun `Should count enrollment filters in total if they are not in unsupported list`() {
        filterManager.clearUnsupportedFilters()
        filterManager.addEnrollmentPeriod(listOf(DatePeriod.create(Date(), Date())))
        filterManager.addEnrollmentStatus(false, EnrollmentStatus.ACTIVE)
        filterManager.addEventStatus(false, EventStatus.ACTIVE)

        assertTrue(filterManager.totalFilters == 3)
    }

    @Test
    fun `Should count filters in total for event working list`() {
        val workingListScope = EventWorkingListScope(
            stageUid = "uid",
            eventDate = "23/11/2023",
            eventStatusList = listOf("status"),
            assignedToMe = AssignedUserMode.CURRENT,
        )
        filterManager.setWorkingListScope(workingListScope)

        assertTrue(filterManager.totalFilters == 3)
        filterManager.setWorkingListScope(EmptyWorkingList())
    }

    @Test
    fun `Should count filters in total for tei working list`() {
        val workingListScope = TeiWorkingListScope(
            enrollmentStatusList = listOf("status", "status"),
            enrollmentDate = null,
            eventStatusList = listOf(),
            eventDateList = listOf("date"),
            assignedToMe = listOf(),
            filters = mapOf(),
            stageUid = "stageUid",
            dataValues = mapOf(),
        )
        filterManager.setWorkingListScope(workingListScope)

        assertTrue(filterManager.totalFilters == 2)
        filterManager.setWorkingListScope(EmptyWorkingList())
    }
}
