package org.dhis2.data.filter

import androidx.databinding.ObservableField
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.android.plugins.RxAndroidPlugins
import io.reactivex.schedulers.Schedulers
import org.dhis2.utils.filters.FilterManager
import org.dhis2.utils.filters.Filters
import org.dhis2.utils.filters.OrgUnitFilter
import org.dhis2.utils.filters.PeriodFilter
import org.dhis2.utils.filters.ProgramType
import org.dhis2.utils.filters.SyncStateFilter
import org.dhis2.utils.filters.sorting.SortingItem
import org.dhis2.utils.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.settings.FilterSetting
import org.hisp.dhis.android.core.settings.HomeFilter
import org.hisp.dhis.android.core.settings.ProgramFilter
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
        const val ASSIGN_TO_ME = "assign_to_me"
    }

    private val observableSortingInject = ObservableField<SortingItem>()
    private val observableOpenFilter = ObservableField<Filters>()
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
        whenever(resourceManager.filterResources.filterEventStatusLabel()) doReturn EVENT_STATUS
        whenever(resourceManager.filterResources.filterEnrollmentDateLabel()) doReturn
            ENROLLMENT_DATE
        whenever(resourceManager.filterResources.filterAssignedToMeLabel()) doReturn ASSIGN_TO_ME
    }

    @Test
    fun `Should get home filters without assign to me when webapp is not configured`() {
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

        assert(result[0].type == Filters.PERIOD)
        assert(result[1].type == Filters.ORG_UNIT)
        assert(result[2].type == Filters.SYNC_STATE)
        assert(result.size == 3)
    }

    @Test
    fun `Should get home filters with assign to me when webapp is not configured`() {
        whenever(d2.programModule()) doReturn mock()
        whenever(d2.programModule().programStages()) doReturn mock()
        whenever(d2.programModule().programStages().byEnableUserAssignment()) doReturn mock()
        whenever(
            d2.programModule().programStages().byEnableUserAssignment().eq(true)
        ) doReturn mock()
        whenever(
            d2.programModule().programStages().byEnableUserAssignment()
                .eq(true).blockingIsEmpty()
        ) doReturn false

        val result = filterRepository.homeFilters()
        assert(result[0].type == Filters.PERIOD)
        assert(result[1].type == Filters.ORG_UNIT)
        assert(result[2].type == Filters.SYNC_STATE)
        assert(result[3].type == Filters.ASSIGNED_TO_ME)
        assert(result.size == 4)
    }

    private fun webAppIsConfigured() {
        whenever(d2.settingModule()) doReturn mock()
        whenever(d2.settingModule().appearanceSettings()) doReturn mock()
    }

    fun getHomeWebAppFilters() = mapOf(
        HomeFilter.DATE to createFilterSetting(true),
        HomeFilter.ORG_UNIT to createFilterSetting(true),
        HomeFilter.SYNC_STATUS to createFilterSetting(false),
        HomeFilter.ASSIGNED_TO_ME to createFilterSetting(true)
    )

    fun createFilterSetting(hasToShowFilter: Boolean): FilterSetting {
        return FilterSetting.builder().filter(hasToShowFilter).sort(hasToShowFilter).build()
    }

    fun getDefaultHomeFilters() = linkedMapOf(
        ProgramFilter.EVENT_DATE to PeriodFilter(
            ProgramType.TRACKER,
            observableSortingInject,
            observableOpenFilter,
            GetFiltersApplyingWebAppConfigTest.EVENT_DATE
        ),
        ProgramFilter.ORG_UNIT to OrgUnitFilter(
            FilterManager.getInstance().observeOrgUnitFilters(),
            ProgramType.TRACKER,
            observableSortingInject,
            observableOpenFilter,
            GetFiltersApplyingWebAppConfigTest.ORG_UNIT
        ),
        ProgramFilter.SYNC_STATUS to SyncStateFilter(
            ProgramType.TRACKER,
            observableSortingInject, observableOpenFilter,
            GetFiltersApplyingWebAppConfigTest.SYNC_STATUS
        )
    )
}
