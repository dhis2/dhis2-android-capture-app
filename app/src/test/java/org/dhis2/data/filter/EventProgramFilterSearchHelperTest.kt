package org.dhis2.data.filter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import java.util.Date
import org.dhis2.utils.filters.FilterManager
import org.dhis2.utils.filters.Filters
import org.dhis2.utils.filters.sorting.SortingItem
import org.dhis2.utils.filters.sorting.SortingStatus
import org.dhis2.utils.resources.ResourceManager
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.search.EventQueryCollectionRepository
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.DatePeriod
import org.hisp.dhis.android.core.program.Program
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class EventProgramFilterSearchHelperTest {

    @Rule
    @JvmField
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var eventFilterSearchHelper: EventProgramFilterSearchHelper
    private val filterRepository: FilterRepository = mock()
    private val resourceManger: ResourceManager = mock()
    private val filterManager: FilterManager = FilterManager.initWith(resourceManger)

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

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
        RxAndroidPlugins.reset()
    }

    @Test
    fun `Should return query by program`() {
        eventFilterSearchHelper.getFilteredEventRepository(
            Program.builder().uid("programUid").build(), null
        )
        verify(filterRepository).eventsByProgram("programUid")
    }

    @Test
    fun `Should not apply any filters if not set`() {
        eventFilterSearchHelper.getFilteredEventRepository(
            Program.builder().uid("programUid").build(), null
        )
        verify(filterRepository, times(0)).applyOrgUnitFilter(
            any<EventQueryCollectionRepository>(),
            any()
        )
        verify(filterRepository, times(0))
            .applyStateFilter(any<EventQueryCollectionRepository>(), any())
        verify(filterRepository, times(0))
            .applyEventStatusFilter(any<EventQueryCollectionRepository>(), any())
        verify(filterRepository, times(0))
            .applyDateFilter(any<EventQueryCollectionRepository>(), any())
        verify(filterRepository, times(0))
            .applyAssignToMe(any<EventQueryCollectionRepository>())
    }

    @Test
    @Ignore
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
            filterRepository.applyOrgUnitFilter(any<EventQueryCollectionRepository>(), any())
        ) doReturn mock()
        whenever(
            filterRepository.applyStateFilter(any<EventQueryCollectionRepository>(), any())
        ) doReturn mock()
        whenever(
            filterRepository.applyDateFilter(any<EventQueryCollectionRepository>(), any())
        ) doReturn mock()
        whenever(
            filterRepository.applyAssignToMe(any<EventQueryCollectionRepository>())
        ) doReturn mock()

        eventFilterSearchHelper.getFilteredEventRepository(
            Program.builder().uid("programUid").build(), null
        )

        verify(filterRepository, times(1))
            .applyOrgUnitFilter(any<EventQueryCollectionRepository>(), any())
        verify(filterRepository, times(1))
            .applyOrgUnitFilter(any<EventQueryCollectionRepository>(), any())
        verify(filterRepository, times(1))
            .applyStateFilter(any<EventQueryCollectionRepository>(), any())
        verify(filterRepository, times(1))
            .applyDateFilter(any<EventQueryCollectionRepository>(), any())
        verify(filterRepository, times(1))
            .applyAssignToMe(any<EventQueryCollectionRepository>())
    }

    @Test
    fun `Should apply sorting for supported sorting type`() {
        filterManager.sortingItem = SortingItem(Filters.PERIOD, SortingStatus.ASC)
        eventFilterSearchHelper.getFilteredEventRepository(
            Program.builder().uid("programUid").build(), null
        )
        verify(filterRepository, times(1)).sortByEventDate(any(), any())
    }
}
