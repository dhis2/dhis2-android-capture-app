package org.dhis2.data.filter

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import java.util.Date
import org.dhis2.utils.filters.FilterManager
import org.dhis2.utils.filters.Filters
import org.dhis2.utils.filters.sorting.SortingItem
import org.dhis2.utils.filters.sorting.SortingStatus
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.EventCollectionRepository
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.DatePeriod
import org.hisp.dhis.android.core.program.Program
import org.junit.After
import org.junit.Before
import org.junit.Test

class EventProgramFilterSearchHelperTest {

    private lateinit var eventFilterSearchHelper: EventProgramFilterSearchHelper
    private val filterRepository: FilterRepository = mock()
    private val filterManager: FilterManager = FilterManager.getInstance()

    @Before
    fun setUp() {
        eventFilterSearchHelper = EventProgramFilterSearchHelper(
            filterRepository,
            filterManager
        )
        whenever(
            filterRepository.eventsByProgram(any())
        ) doReturn mock()
    }

    @After
    fun clearAll() {
        filterManager.clearAllFilters()
    }

    @Test
    fun `Should return query by program`() {
        eventFilterSearchHelper.getFilteredEventRepository(
            Program.builder().uid("programUid").build()
        )
        verify(filterRepository).eventsByProgram("programUid")
    }

    @Test
    fun `Should not apply any filters if not set`() {
        eventFilterSearchHelper.getFilteredEventRepository(
            Program.builder().uid("programUid").build()
        )
        verify(filterRepository, times(0)).applyOrgUnitFilter(
            any<EventCollectionRepository>(),
            any()
        )
        verify(filterRepository, times(0))
            .applyStateFilter(any<EventCollectionRepository>(), any())
        verify(filterRepository, times(0))
            .applyEventStatusFilter(any(), any())
        verify(filterRepository, times(0))
            .applyDateFilter(any<EventCollectionRepository>(), any())
        verify(filterRepository, times(0))
            .applyAssignToMe(any<EventCollectionRepository>())
    }

    @Test
    fun `Should apply filters if set`() {
        filterManager.apply {
            addOrgUnit(
                OrganisationUnit.builder()
                    .uid("ouUid").build()
            )
            addState(false, State.ERROR)
            addPeriod(arrayListOf(DatePeriod.create(Date(), Date())))
            setAssignedToMe(true)
        }

        whenever(
            filterRepository.applyOrgUnitFilter(any<EventCollectionRepository>(), any())
        ) doReturn mock()
        whenever(
            filterRepository.applyStateFilter(any<EventCollectionRepository>(), any())
        ) doReturn mock()
        whenever(
            filterRepository.applyDateFilter(any<EventCollectionRepository>(), any())
        ) doReturn mock()
        whenever(
            filterRepository.applyAssignToMe(any<EventCollectionRepository>())
        ) doReturn mock()

        eventFilterSearchHelper.getFilteredEventRepository(
            Program.builder().uid("programUid").build()
        )

        verify(filterRepository, times(1))
            .applyOrgUnitFilter(any<EventCollectionRepository>(), any())
        verify(filterRepository, times(1))
            .applyStateFilter(any<EventCollectionRepository>(), any())
        verify(filterRepository, times(1))
            .applyDateFilter(any<EventCollectionRepository>(), any())
        verify(filterRepository, times(1))
            .applyAssignToMe(any<EventCollectionRepository>())
    }

    @Test
    fun `Should apply sorting for supported sorting type`() {
        filterManager.sortingItem = SortingItem(Filters.PERIOD, SortingStatus.ASC)
        eventFilterSearchHelper.getFilteredEventRepository(
            Program.builder().uid("programUid").build()
        )
        verify(filterRepository, times(1)).sortByEventDate(any(), any())
    }
}
