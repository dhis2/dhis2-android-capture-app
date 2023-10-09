package org.dhis2.data.filter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.Filters
import org.dhis2.commons.filters.data.FilterRepository
import org.dhis2.commons.filters.data.TrackerFilterSearchHelper
import org.dhis2.commons.filters.sorting.SortingItem
import org.dhis2.commons.filters.sorting.SortingStatus
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.DatePeriod
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchCollectionRepository
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

class TrackerFilterSearchHelperTest {

    private lateinit var trackerFilterSearchHelper: TrackerFilterSearchHelper
    private val filterRepository: FilterRepository = mock()
    private val resourceManger: ResourceManager = mock()
    private val filterManager: FilterManager = FilterManager.initWith(resourceManger)

    @Rule
    @JvmField
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }

        trackerFilterSearchHelper = TrackerFilterSearchHelper(
            filterRepository,
            filterManager,
        )
        whenever(
            filterRepository.trackedEntityInstanceQueryByProgram(any()),
        ) doReturn mock()
        whenever(
            filterRepository.trackedEntityInstanceQueryByType(any()),
        ) doReturn mock()
        whenever(
            filterRepository.applyOrgUnitFilter(any(), any(), any()),
        ) doReturn mock()
    }

    @After
    fun clearAll() {
        filterManager.clearAllFilters()
        RxAndroidPlugins.reset()
    }

    @Ignore("Null pointer exception in bitrise")
    @Test
    fun `Should return query by program`() {
        trackerFilterSearchHelper.getFilteredProgramRepository("programUid")
        verify(filterRepository).trackedEntityInstanceQueryByProgram("programUid")
    }

    @Ignore("Null pointer exception in bitrise")
    @Test
    fun `Should return query by type`() {
        trackerFilterSearchHelper.getFilteredTrackedEntityTypeRepository("teType")
        verify(filterRepository).trackedEntityInstanceQueryByType("teType")
    }

    @Ignore("Null pointer exception in bitrise")
    @Test
    fun `Should not apply any filters if not set`() {
        trackerFilterSearchHelper.getFilteredProgramRepository("programUid")
        verify(filterRepository, times(0)).applyEnrollmentStatusFilter(any(), any())
        verify(filterRepository, times(0)).applyEventStatusFilter(
            any<TrackedEntitySearchCollectionRepository>(),
            any(),
        )
        verify(filterRepository, times(1)).rootOrganisationUnitUids()
        verify(
            filterRepository,
            times(0),
        ).applyStateFilter(any<TrackedEntitySearchCollectionRepository>(), any())
        verify(
            filterRepository,
            times(0),
        ).applyDateFilter(any<TrackedEntitySearchCollectionRepository>(), any())
        verify(filterRepository, times(0)).applyEnrollmentDateFilter(any(), any())
        verify(
            filterRepository,
            times(0),
        ).applyAssignToMe(any<TrackedEntitySearchCollectionRepository>())
    }

    @Ignore
    @Test
    fun `Should apply filters if set`() {
        filterManager.apply {
            addEnrollmentStatus(false, EnrollmentStatus.ACTIVE)
            addEventStatus(false, EventStatus.SCHEDULE)
            addOrgUnit(
                OrganisationUnit.builder()
                    .uid("ouUid").build(),
            )
            addState(false, State.ERROR)
            addPeriod(arrayListOf(DatePeriod.create(Date(), Date())))
            addEnrollmentPeriod(arrayListOf(DatePeriod.create(Date(), Date())))
            setAssignedToMe(true)
        }

        whenever(filterRepository.applyEnrollmentStatusFilter(any(), any())) doReturn mock()
        whenever(
            filterRepository.applyEventStatusFilter(
                any<TrackedEntitySearchCollectionRepository>(),
                any(),
            ),
        ) doReturn mock()
        whenever(filterRepository.applyOrgUnitFilter(any(), any(), any())) doReturn mock()
        whenever(
            filterRepository.applyStateFilter(
                any<TrackedEntitySearchCollectionRepository>(),
                any(),
            ),
        ) doReturn mock()
        whenever(
            filterRepository.applyDateFilter(
                any<TrackedEntitySearchCollectionRepository>(),
                any(),
            ),
        ) doReturn mock()
        whenever(
            filterRepository.applyEnrollmentDateFilter(any(), any()),
        ) doReturn mock()
        whenever(
            filterRepository.applyAssignToMe(any<TrackedEntitySearchCollectionRepository>()),
        ) doReturn mock()
        trackerFilterSearchHelper.getFilteredProgramRepository("programUid")

        verify(filterRepository, times(1)).applyEnrollmentStatusFilter(any(), any())
        verify(
            filterRepository,
            times(1),
        ).applyEventStatusFilter(any<TrackedEntitySearchCollectionRepository>(), any())
        verify(filterRepository, times(1)).applyOrgUnitFilter(
            any(),
            any(),
            any(),
        )
        verify(
            filterRepository,
            times(1),
        ).applyStateFilter(any<TrackedEntitySearchCollectionRepository>(), any())
        verify(
            filterRepository,
            times(1),
        ).applyDateFilter(any<TrackedEntitySearchCollectionRepository>(), any())
        verify(filterRepository, times(1)).applyEnrollmentDateFilter(any(), any())
        verify(
            filterRepository,
            times(1),
        ).applyAssignToMe(any<TrackedEntitySearchCollectionRepository>())
    }

    @Ignore("Null pointer exception in bitrise")
    @Test
    fun `Should apply sorting for supported sorting type`() {
        filterManager.sortingItem = SortingItem(Filters.PERIOD, SortingStatus.ASC)
        trackerFilterSearchHelper.getFilteredProgramRepository("programUid")
        verify(filterRepository, times(1)).sortByPeriod(any(), any())
    }
}
