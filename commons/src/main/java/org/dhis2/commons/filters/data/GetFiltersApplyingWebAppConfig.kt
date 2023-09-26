package org.dhis2.commons.filters.data

import org.dhis2.commons.filters.FilterItem
import org.hisp.dhis.android.core.settings.FilterSetting

class GetFiltersApplyingWebAppConfig {

    fun <T> execute(
        defaultFilters: LinkedHashMap<T, FilterItem>,
        webAppFilters: Map<T, FilterSetting>,
    ): List<FilterItem> {
        val filtersToShowFromWebAppKeys = webAppFilters.filterValues { it.filter()!! }.keys.toList()
        val filterToShow = defaultFilters.filter { filtersToShowFromWebAppKeys.contains(it.key) }

        return filterToShow.values.toList()
    }
}
