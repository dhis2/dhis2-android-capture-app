package dhis2.org.analytics.charts.providers

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dhis2.org.analytics.charts.ui.OrgUnitFilterType
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

class AnalyticsFilterProvider(private val d2: D2) {

    fun addOrgUnitFilter(
        visualizationUid: String,
        filterType: OrgUnitFilterType,
        orgUnits: List<OrganisationUnit>
    ) {
        when (filterType) {
            OrgUnitFilterType.NONE -> return
            OrgUnitFilterType.ALL -> {
                d2.dataStoreModule().localDataStore()
                    .value("${visualizationUid}_ou_type")
                    .blockingSet(filterType.name)
                d2.dataStoreModule().localDataStore()
                    .value("${visualizationUid}_ou")
                    .blockingDeleteIfExist()
            }

            OrgUnitFilterType.SELECTION -> {
                d2.dataStoreModule().localDataStore()
                    .value("${visualizationUid}_ou_type")
                    .blockingSet(filterType.name)
                d2.dataStoreModule().localDataStore()
                    .value("${visualizationUid}_ou")
                    .blockingSet(
                        Gson().toJson(orgUnits.map { it.uid() })
                    )
            }
        }
    }

    fun removeOrgUnitFilter(visualizationUid: String) {
        d2.dataStoreModule().localDataStore()
            .value("${visualizationUid}_ou_type")
            .blockingDeleteIfExist()
        d2.dataStoreModule().localDataStore()
            .value("${visualizationUid}_ou")
            .blockingDeleteIfExist()
    }

    fun addPeriodFilter(visualizationUid: String, periods: List<RelativePeriod>){
        d2.dataStoreModule().localDataStore()
            .value("${visualizationUid}_p")
            .blockingSet(
                Gson().toJson(periods)
            )
    }

    fun removePeriodFilter(visualizationUid: String){
        d2.dataStoreModule().localDataStore()
            .value("${visualizationUid}_p")
            .blockingDeleteIfExist()
    }

    fun visualizationPeriod(visualizationUid: String): List<RelativePeriod>? {
        return if (d2.dataStoreModule().localDataStore().value("${visualizationUid}_p")
                .blockingExists()
        ) {
            val entry =
                d2.dataStoreModule().localDataStore()
                    .value("${visualizationUid}_p")
                    .blockingGet()
            val type = object : TypeToken<List<RelativePeriod>?>() {}.type
            return entry.value()?.let { Gson().fromJson(entry.value(), type) }
        } else {
            null
        }
    }

    fun visualizationOrgUnitsType(visualizationUid: String): OrgUnitFilterType? {
        return if (d2.dataStoreModule().localDataStore().value("${visualizationUid}_ou_type")
                .blockingExists()
        ) {
            val entry =
                d2.dataStoreModule().localDataStore()
                    .value("${visualizationUid}_ou_type")
                    .blockingGet()
            val type = object : TypeToken<OrgUnitFilterType>() {}.type
            return entry.value()?.let { Gson().fromJson(entry.value(), type) }
        } else {
            null
        }
    }

    fun visualizationOrgUnits(visualizationUid: String): List<String>? {
        return if (d2.dataStoreModule().localDataStore().value("${visualizationUid}_ou")
                .blockingExists()
        ) {
            val entry =
                d2.dataStoreModule().localDataStore()
                    .value("${visualizationUid}_ou")
                    .blockingGet()
            val type = object : TypeToken<List<String>?>() {}.type
            return entry.value()?.let { Gson().fromJson(entry.value(), type) }
        } else {
            null
        }
    }
}