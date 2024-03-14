package org.dhis2.data.filter

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.data.DataSetFilterSearchHelper
import org.dhis2.commons.filters.data.FilterRepository
import org.dhis2.commons.resources.ResourceManager
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataset.DataSetInstanceSummaryCollectionRepository
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.DatePeriod
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

class DataSetFilterSearchHelperTest {

    @Rule
    @JvmField
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var dataSetFilterSearchHelper: DataSetFilterSearchHelper
    private val filterRepository: FilterRepository = mock()
    private val resourceManager: ResourceManager = mock()
    private val filterManager: FilterManager = FilterManager.initWith(resourceManager)

    @Before
    fun setUp() {
        whenever(resourceManager.getString(any())) doReturn "text"
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        dataSetFilterSearchHelper = DataSetFilterSearchHelper(
            filterRepository,
            filterManager,
        )
        whenever(
            filterRepository.dataSetInstanceSummaries(),
        ) doReturn mock()
    }

    @After
    fun tearDown() {
        RxAndroidPlugins.reset()
    }

    @After
    fun clearAll() {
        filterManager.clearAllFilters()
    }

    @Ignore("Null pointer exception in bitrise")
    @Test
    fun `Should return dataset instance summaries`() {
        dataSetFilterSearchHelper.getFilteredDataSetSearchRepository()
        verify(filterRepository).dataSetInstanceSummaries()
    }

    @Ignore("Null pointer exception in bitrise")
    @Test
    fun `Should not apply any filters if not set`() {
        dataSetFilterSearchHelper.getFilteredDataSetSearchRepository()
        verify(filterRepository, times(0))
            .applyOrgUnitFilter(any<DataSetInstanceSummaryCollectionRepository>(), any())
        verify(filterRepository, times(0))
            .applyStateFilter(any<DataSetInstanceSummaryCollectionRepository>(), any())
        verify(filterRepository, times(0))
            .applyPeriodFilter(any(), any())
    }

    @Ignore
    @Test
    fun `Should apply filters if set`() {
        filterManager.apply {
            addOrgUnit(
                OrganisationUnit.builder()
                    .uid("ouUid").build(),
            )
            addState(false, State.ERROR)
            addPeriod(arrayListOf(DatePeriod.create(Date(), Date())))
        }

        whenever(
            filterRepository.applyOrgUnitFilter(
                any<DataSetInstanceSummaryCollectionRepository>(),
                any(),
            ),
        ) doReturn mock()
        whenever(
            filterRepository.applyStateFilter(
                any<DataSetInstanceSummaryCollectionRepository>(),
                any(),
            ),
        ) doReturn mock()
        whenever(
            filterRepository.applyPeriodFilter(any(), any()),
        ) doReturn mock()

        dataSetFilterSearchHelper.getFilteredDataSetSearchRepository()

        verify(filterRepository, times(1))
            .applyOrgUnitFilter(any<DataSetInstanceSummaryCollectionRepository>(), any())
        verify(filterRepository, times(1))
            .applyStateFilter(any<DataSetInstanceSummaryCollectionRepository>(), any())
        verify(filterRepository, times(1))
            .applyPeriodFilter(any(), any())
    }
}
