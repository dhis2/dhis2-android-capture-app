package org.dhis2.data.filter

import androidx.databinding.ObservableField
import org.dhis2.commons.filters.EnrollmentDateFilter
import org.dhis2.commons.filters.EnrollmentStatusFilter
import org.dhis2.commons.filters.EventStatusFilter
import org.dhis2.commons.filters.FilterManager
import org.dhis2.commons.filters.Filters
import org.dhis2.commons.filters.OrgUnitFilter
import org.dhis2.commons.filters.PeriodFilter
import org.dhis2.commons.filters.ProgramType
import org.dhis2.commons.filters.SyncStateFilter
import org.dhis2.commons.filters.data.GetFiltersApplyingWebAppConfig
import org.dhis2.commons.filters.sorting.SortingItem
import org.hisp.dhis.android.core.settings.FilterSetting
import org.hisp.dhis.android.core.settings.ProgramFilter
import org.junit.Test

class GetFiltersApplyingWebAppConfigTest {

    private val getFiltersApplyingWebAppConfig: GetFiltersApplyingWebAppConfig =
        GetFiltersApplyingWebAppConfig()
    private val observableSortingInject = ObservableField<SortingItem>()
    private val observableOpenFilter = ObservableField<Filters>()

    @Test
    fun `Should get filters applying webapp config deleting last item`() {
        val result = getFiltersApplyingWebAppConfig.execute(
            getDefaultTrackerFilters(),
            getWebAppFiltersFirstConfig()
        )
        assert(result[0].filterLabel == EVENT_DATE)
        assert(result[1].filterLabel == ENROLLMENT_DATE)
        assert(result[2].filterLabel == ORG_UNIT)
        assert(result[3].filterLabel == SYNC_STATUS)
        assert(result[4].filterLabel == ENROLLMENT_STATUS)
    }

    @Test
    fun `Should get filters applying webapp config deleting random items`() {
        val result = getFiltersApplyingWebAppConfig.execute(
            getDefaultTrackerFilters(),
            getWebAppFiltersSecondConfig()
        )
        assert(result[0].filterLabel == EVENT_DATE)
        assert(result[1].filterLabel == ENROLLMENT_DATE)
        assert(result[2].filterLabel == SYNC_STATUS)
        assert(result[3].filterLabel == EVENT_STATUS)
    }

    @Test
    fun `Should get empty filters applying webapp config with all of them false`() {
        val result = getFiltersApplyingWebAppConfig.execute(
            getDefaultTrackerFilters(),
            getWebAppFitersNoFiltersToShow()
        )
        assert(result.isEmpty())
    }

    fun getDefaultTrackerFilters() = linkedMapOf(
        ProgramFilter.EVENT_DATE to PeriodFilter(
            ProgramType.TRACKER,
            observableSortingInject,
            observableOpenFilter,
            EVENT_DATE
        ),
        ProgramFilter.ENROLLMENT_DATE to EnrollmentDateFilter(
            ProgramType.TRACKER,
            observableSortingInject,
            observableOpenFilter,
            ENROLLMENT_DATE
        ),
        ProgramFilter.ORG_UNIT to OrgUnitFilter(
            FilterManager.getInstance().observeOrgUnitFilters(),
            ProgramType.TRACKER,
            observableSortingInject,
            observableOpenFilter,
            ORG_UNIT
        ),
        ProgramFilter.SYNC_STATUS to SyncStateFilter(
            ProgramType.TRACKER,
            observableSortingInject,
            observableOpenFilter,
            SYNC_STATUS
        ),
        ProgramFilter.ENROLLMENT_STATUS to EnrollmentStatusFilter(
            ProgramType.TRACKER,
            observableSortingInject,
            observableOpenFilter,
            ENROLLMENT_STATUS
        ),
        ProgramFilter.EVENT_STATUS to EventStatusFilter(
            ProgramType.TRACKER,
            observableSortingInject,
            observableOpenFilter,
            EVENT_STATUS
        )
    )

    fun getWebAppFiltersFirstConfig() = mapOf(
        ProgramFilter.EVENT_DATE to createFilterSetting(true),
        ProgramFilter.SYNC_STATUS to createFilterSetting(true),
        ProgramFilter.EVENT_STATUS to createFilterSetting(false),
        ProgramFilter.ASSIGNED_TO_ME to createFilterSetting(true),
        ProgramFilter.ENROLLMENT_DATE to createFilterSetting(true),
        ProgramFilter.ENROLLMENT_STATUS to createFilterSetting(true),
        ProgramFilter.ORG_UNIT to createFilterSetting(true),
        ProgramFilter.CAT_COMBO to createFilterSetting(false)
    )

    fun getWebAppFiltersSecondConfig() = mapOf(
        ProgramFilter.EVENT_DATE to createFilterSetting(true),
        ProgramFilter.SYNC_STATUS to createFilterSetting(true),
        ProgramFilter.EVENT_STATUS to createFilterSetting(true),
        ProgramFilter.ASSIGNED_TO_ME to createFilterSetting(true),
        ProgramFilter.ENROLLMENT_DATE to createFilterSetting(true),
        ProgramFilter.ENROLLMENT_STATUS to createFilterSetting(false),
        ProgramFilter.ORG_UNIT to createFilterSetting(false),
        ProgramFilter.CAT_COMBO to createFilterSetting(false)
    )

    fun getWebAppFitersNoFiltersToShow() = mapOf(
        ProgramFilter.EVENT_DATE to createFilterSetting(false),
        ProgramFilter.SYNC_STATUS to createFilterSetting(false),
        ProgramFilter.EVENT_STATUS to createFilterSetting(false),
        ProgramFilter.ASSIGNED_TO_ME to createFilterSetting(false),
        ProgramFilter.ENROLLMENT_DATE to createFilterSetting(false),
        ProgramFilter.ENROLLMENT_STATUS to createFilterSetting(false),
        ProgramFilter.ORG_UNIT to createFilterSetting(false),
        ProgramFilter.CAT_COMBO to createFilterSetting(false)
    )

    fun createFilterSetting(hasToShowFilter: Boolean): FilterSetting {
        return FilterSetting.builder().filter(hasToShowFilter).sort(hasToShowFilter).build()
    }

    companion object {
        const val EVENT_DATE = "date"
        const val ENROLLMENT_DATE = "enrollment_date"
        const val ORG_UNIT = "org_unit"
        const val SYNC_STATUS = "sync_status"
        const val ENROLLMENT_STATUS = "enrollment_status"
        const val EVENT_STATUS = "event_status"
    }
}
