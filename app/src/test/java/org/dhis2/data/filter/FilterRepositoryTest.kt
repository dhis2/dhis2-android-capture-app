package org.dhis2.data.filter

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import org.dhis2.utils.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.junit.Before
import org.junit.Test

class FilterRepositoryTest {

    private val d2: D2 = mock()
    private val resourceManager: ResourceManager = mock()
    private val getFiltersApplyingWebAppConfig: GetFiltersApplyingWebAppConfig = mock()
    private lateinit var filterRepository: FilterRepository

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        filterRepository = FilterRepository(d2, resourceManager, getFiltersApplyingWebAppConfig)
        whenever(resourceManager.filterResources) doReturn mock()
        whenever(resourceManager.filterResources.filterOrgUnitLabel()) doReturn "org_unit"
        whenever(resourceManager.filterResources.filterSyncLabel()) doReturn "sync"
        whenever(resourceManager.filterResources.filterEnrollmentStatusLabel()) doReturn "status"
        whenever(resourceManager.filterResources.filterDateLabel()) doReturn "date"
    }

    @Test
    fun `Should get home filters when webapp is not configured`() {
        //val result = filterRepository.homeFilters()
    }

    /*
    resources.filterResources.filterDateLabel()
            ),
            HomeFilter.ORG_UNIT to OrgUnitFilter(
                FilterManager.getInstance().observeOrgUnitFilters(),
                org.dhis2.utils.filters.ProgramType.ALL,
                observableSortingInject,
                observableOpenFilter,
                resources.filterResources.filterOrgUnitLabel()
            ),
            HomeFilter.SYNC_STATUS to SyncStateFilter(
                org.dhis2.utils.filters.ProgramType.ALL,
                observableSortingInject, observableOpenFilter,
                resources.filterResources.filterSyncLabel()
     */
}