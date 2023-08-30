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

    fun removeOrgUnitFilter(visualizationUid: String) {
        removeValue("${visualizationUid}_ou_type")
        removeValue("${visualizationUid}_ou")
    }

    fun addPeriodFilter(visualizationUid: String, periods: List<RelativePeriod>) {
        setValue(
            "${visualizationUid}_p",
            Gson().toJson(periods),
        )
    }

    fun removePeriodFilter(visualizationUid: String) {
        removeValue("${visualizationUid}_p")
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
