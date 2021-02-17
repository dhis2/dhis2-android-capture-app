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

    companion object {
        const val EVENT_DATE = "date"
        const val ENROLLMENT_DATE = "enrollment_date"
        const val ORG_UNIT = "org_unit"
        const val SYNC_STATUS = "sync_status"
        const val ENROLLMENT_STATUS = "enrollment_status"
        const val EVENT_STATUS = "event_status"
    }

    private val d2: D2 = mock()
    private val resourceManager: ResourceManager = mock()
    private val getFiltersApplyingWebAppConfig: GetFiltersApplyingWebAppConfig = mock()
    private lateinit var filterRepository: FilterRepository

    @Before
    fun setUp() {
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
        filterRepository = FilterRepository(d2, resourceManager, getFiltersApplyingWebAppConfig)
        whenever(resourceManager.filterResources) doReturn mock()
        whenever(resourceManager.filterResources.filterOrgUnitLabel()) doReturn ORG_UNIT
        whenever(resourceManager.filterResources.filterSyncLabel()) doReturn SYNC_STATUS
        whenever(resourceManager.filterResources.filterEnrollmentStatusLabel()) doReturn
            ENROLLMENT_STATUS
        whenever(resourceManager.filterResources.filterDateLabel()) doReturn EVENT_DATE
    }

    @Test
    fun `Should get home filters when webapp is not configured`() {
        whenever(d2.programModule()) doReturn mock()
        whenever(d2.programModule().programStages()) doReturn mock()
        whenever(d2.programModule().programStages().byEnableUserAssignment()) doReturn mock()
        whenever(
            d2.programModule().programStages().byEnableUserAssignment().eq(true)
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byEnableUserAssignment()
                .eq(true).blockingIsEmpty()
        ) doReturn true

        val result = filterRepository.homeFilters()

        // assert(result)
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
