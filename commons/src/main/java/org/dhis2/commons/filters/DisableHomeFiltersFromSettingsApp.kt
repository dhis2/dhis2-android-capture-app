package org.dhis2.commons.filters

class DisableHomeFiltersFromSettingsApp {

    fun execute(filters: List<FilterItem>) {
        val isOrgUnit = filters.filterIsInstance<OrgUnitFilter>().isNotEmpty()
        val isSync = filters.filterIsInstance<SyncStateFilter>().isNotEmpty()
        val isPeriod = filters.filterIsInstance<PeriodFilter>().isNotEmpty()

        if (!isOrgUnit) FilterManager.getInstance().clearOuFilter()
        if (!isSync) FilterManager.getInstance().clearSyncFilter()
        if (!isPeriod) FilterManager.getInstance().clearPeriodFilter()
    }
}
