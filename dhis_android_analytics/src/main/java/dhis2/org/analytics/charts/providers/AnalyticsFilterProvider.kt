package dhis2.org.analytics.charts.providers

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dhis2.org.analytics.charts.ui.OrgUnitFilterType
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.datastore.KeyValuePair
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

class AnalyticsFilterProvider(private val d2: D2) {

    fun addOrgUnitFilter(
        visualizationUid: String,
        lineListingColumnId: Int?,
        filterType: OrgUnitFilterType,
        orgUnits: List<OrganisationUnit>,
    ) {
        if (lineListingColumnId != null) {
            addTrackerVisualizationOrgUnitFilter(
                visualizationUid,
                lineListingColumnId,
                filterType,
                orgUnits,
            )
        } else {
            addOrgUnitFilter(visualizationUid, filterType, orgUnits)
        }
    }

    private fun addTrackerVisualizationOrgUnitFilter(
        trackerVisualizationUid: String,
        columnIndex: Int,
        filterType: OrgUnitFilterType,
        orgUnits: List<OrganisationUnit>,
    ) {
        val currentColumnOuFilter =
            trackerVisualizationOrgUnitFilters(trackerVisualizationUid)?.toMutableMap()
                ?: mutableMapOf()
        currentColumnOuFilter[columnIndex] = orgUnits.map { it.uid() }

        val currentColumnOuTypeFilter =
            trackerVisualizationOrgUnitTypeFilters(trackerVisualizationUid)?.toMutableMap()
                ?: mutableMapOf()
        currentColumnOuTypeFilter[columnIndex] = filterType

        setValue(
            "${trackerVisualizationUid}_ou_type",
            Gson().toJson(currentColumnOuTypeFilter),
        )

        setValue(
            "${trackerVisualizationUid}_ou",
            Gson().toJson(currentColumnOuFilter),
        )
    }

    private fun addOrgUnitFilter(
        visualizationUid: String,
        filterType: OrgUnitFilterType,
        orgUnits: List<OrganisationUnit>,
    ) {
        when (filterType) {
            OrgUnitFilterType.NONE -> return
            OrgUnitFilterType.ALL -> {
                setValue("${visualizationUid}_ou_type", filterType.name)
                removeValue("${visualizationUid}_ou")
            }

            OrgUnitFilterType.SELECTION -> {
                setValue("${visualizationUid}_ou_type", filterType.name)
                setValue(
                    "${visualizationUid}_ou",
                    Gson().toJson(orgUnits.map { it.uid() }),
                )
            }
        }
    }

    fun removeOrgUnitFilter(visualizationUid: String, lineListingColumnId: Int?) {
        if (lineListingColumnId != null) {
            removeTrackerVisualizationOrgUnitFilter(visualizationUid, lineListingColumnId)
        } else {
            removeValue("${visualizationUid}_ou_type")
            removeValue("${visualizationUid}_ou")
        }
    }

    private fun removeTrackerVisualizationOrgUnitFilter(
        trackerVisualizationUid: String,
        columnIndex: Int,
    ) {
        val currentOUTypeColumnFilter =
            trackerVisualizationOrgUnitTypeFilters(trackerVisualizationUid)?.toMutableMap()
                ?: mutableMapOf()
        if (currentOUTypeColumnFilter.contains(columnIndex)) {
            currentOUTypeColumnFilter.remove(columnIndex)
            setValue(
                "${trackerVisualizationUid}_ou_type",
                Gson().toJson(currentOUTypeColumnFilter),
            )
        } else if (columnIndex == -1 || currentOUTypeColumnFilter.size == 1) {
            removeValue("${trackerVisualizationUid}_ou_type")
        }

        val currentColumnFilter =
            trackerVisualizationOrgUnitFilters(trackerVisualizationUid)?.toMutableMap()
                ?: mutableMapOf()
        if (currentColumnFilter.contains(columnIndex)) {
            currentColumnFilter.remove(columnIndex)
            setValue(
                "${trackerVisualizationUid}_ou",
                Gson().toJson(currentColumnFilter),
            )
        } else if (columnIndex == -1 || currentColumnFilter.size == 1) {
            removeValue("${trackerVisualizationUid}_ou")
        }
    }

    fun addPeriodFilter(
        visualizationUid: String,
        lineListingColumnId: Int?,
        periods: List<RelativePeriod>,
    ) {
        if (lineListingColumnId != null) {
            addTrackerVisualizationPeriodFilter(visualizationUid, lineListingColumnId, periods)
        } else {
            setValue(
                "${visualizationUid}_p",
                Gson().toJson(periods),
            )
        }
    }

    private fun addTrackerVisualizationPeriodFilter(
        trackerVisualizationUid: String,
        columnIndex: Int,
        periods: List<RelativePeriod>,
    ) {
        val currentColumnFilter =
            trackerVisualizationPeriodFilters(trackerVisualizationUid)?.toMutableMap()
                ?: mutableMapOf()
        currentColumnFilter[columnIndex] = periods

        setValue(
            "${trackerVisualizationUid}_p",
            Gson().toJson(currentColumnFilter),
        )
    }

    fun removePeriodFilter(visualizationUid: String, lineListingColumnId: Int?) {
        if (lineListingColumnId != null) {
            removeTrackerVisualizationPeriodFilter(visualizationUid, lineListingColumnId)
        } else {
            removeValue("${visualizationUid}_p")
        }
    }

    private fun removeTrackerVisualizationPeriodFilter(
        trackerVisualizationUid: String,
        columnIndex: Int,
    ) {
        val currentOUTypeColumnFilter =
            trackerVisualizationPeriodFilters(trackerVisualizationUid)?.toMutableMap()
                ?: mutableMapOf()
        if (currentOUTypeColumnFilter.contains(columnIndex)) {
            currentOUTypeColumnFilter.remove(columnIndex)
            setValue(
                "${trackerVisualizationUid}_p",
                Gson().toJson(currentOUTypeColumnFilter),
            )
        } else if (columnIndex == -1 || currentOUTypeColumnFilter.size == 1) {
            removeValue("${trackerVisualizationUid}_p")
        }
    }

    fun addColumnFilter(trackerVisualizationUid: String, columnIndex: Int, filterValue: String) {
        val currentColumnFilter =
            trackerVisualizationFilters(trackerVisualizationUid)?.toMutableMap() ?: mutableMapOf()
        currentColumnFilter[columnIndex] = filterValue
        setValue(
            "${trackerVisualizationUid}_c",
            Gson().toJson(currentColumnFilter),
        )
    }

    fun removeColumnFilter(trackerVisualizationUid: String, columnIndex: Int) {
        val currentColumnFilter =
            trackerVisualizationFilters(trackerVisualizationUid)?.toMutableMap() ?: mutableMapOf()
        if (currentColumnFilter.contains(columnIndex)) {
            currentColumnFilter.remove(columnIndex)
            setValue(
                "${trackerVisualizationUid}_c",
                Gson().toJson(currentColumnFilter),
            )
        } else if (columnIndex == -1 || currentColumnFilter.size == 1) {
            removeValue("${trackerVisualizationUid}_c")
            removeTrackerVisualizationOrgUnitFilter(trackerVisualizationUid, columnIndex)
            removeTrackerVisualizationPeriodFilter(trackerVisualizationUid, columnIndex)
        }
    }

    fun visualizationPeriod(visualizationUid: String): List<RelativePeriod>? {
        return if (isValueSaved("${visualizationUid}_p")) {
            val entry = valueFromKey("${visualizationUid}_p")
            val type = object : TypeToken<List<RelativePeriod>?>() {}.type
            return entry?.value()?.let { Gson().fromJson(entry.value(), type) }
        } else {
            null
        }
    }

    fun visualizationOrgUnitsType(visualizationUid: String): OrgUnitFilterType? {
        return if (isValueSaved("${visualizationUid}_ou_type")) {
            val entry = valueFromKey("${visualizationUid}_ou_type")
            val type = object : TypeToken<OrgUnitFilterType>() {}.type
            return entry?.value()?.let { Gson().fromJson(entry.value(), type) }
        } else {
            null
        }
    }

    fun visualizationOrgUnits(visualizationUid: String): List<String>? {
        return if (isValueSaved("${visualizationUid}_ou")) {
            val entry = valueFromKey("${visualizationUid}_ou")
            val type = object : TypeToken<List<String>?>() {}.type
            return entry?.value()?.let { Gson().fromJson(entry.value(), type) }
        } else {
            null
        }
    }

    fun trackerVisualizationFilters(trackerVisualizationUid: String): Map<Int, String>? {
        return if (isValueSaved("${trackerVisualizationUid}_c")) {
            val entry = valueFromKey("${trackerVisualizationUid}_c")
            val type = object : TypeToken<Map<Int, String>>() {}.type
            return entry?.value()?.let { Gson().fromJson(it, type) }
        } else {
            null
        }
    }

    fun trackerVisualizationPeriodFilters(trackerVisualizationUid: String): Map<Int, List<RelativePeriod>>? {
        return if (isValueSaved("${trackerVisualizationUid}_p")) {
            val entry = valueFromKey("${trackerVisualizationUid}_p")
            val type = object : TypeToken<Map<Int, List<RelativePeriod>>>() {}.type
            return entry?.value()?.let { Gson().fromJson(it, type) }
        } else {
            null
        }
    }

    fun trackerVisualizationOrgUnitTypeFilters(trackerVisualizationUid: String): Map<Int, OrgUnitFilterType>? {
        return try {
            if (isValueSaved("${trackerVisualizationUid}_ou_type")) {
                val entry = valueFromKey("${trackerVisualizationUid}_ou_type")
                val type = object : TypeToken<Map<Int, OrgUnitFilterType>>() {}.type
                return entry?.value()?.let { Gson().fromJson(it, type) }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun trackerVisualizationOrgUnitFilters(trackerVisualizationUid: String): Map<Int, List<String>>? {
        try {
            return if (isValueSaved("${trackerVisualizationUid}_ou")) {
                val entry = valueFromKey("${trackerVisualizationUid}_ou")
                val type = object : TypeToken<Map<Int, List<String>>>() {}.type
                return entry?.value()?.let { Gson().fromJson(it, type) }
            } else {
                null
            }
        } catch (e: Exception) {
            return null
        }
    }

    private fun setValue(key: String, value: String) {
        d2.dataStoreModule().localDataStore()
            .value(key)
            .blockingSet(value)
    }

    private fun isValueSaved(key: String): Boolean {
        return d2.dataStoreModule().localDataStore()
            .value(key)
            .blockingExists()
    }

    private fun valueFromKey(key: String): KeyValuePair? {
        return d2.dataStoreModule().localDataStore()
            .value(key)
            .blockingGet()
    }

    private fun removeValue(key: String) {
        d2.dataStoreModule().localDataStore()
            .value(key)
            .blockingDeleteIfExist()
    }
}
