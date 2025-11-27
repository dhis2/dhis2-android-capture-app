package org.dhis2.maps.usecases

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.commons.text.similarity.JaroWinklerDistance
import org.dhis2.commons.bindings.addIf
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.mobile.ui.designsystem.component.model.LocationItemModel

const val STORED_LOCATION = "STORED_LOCATION"

class SearchLocationManager(
    private val d2: D2,
    private val maxStoredLocations: Int = 20,
) {
    private var cachedResults = emptyList<LocationItemModel.StoredResult>()

    private val gson = Gson()
    private val jaroWinklerDistance = JaroWinklerDistance()
    private val jaroWinklerThreshold = 0.8

    suspend fun getAvailableLocations(query: String? = null): List<LocationItemModel.StoredResult> {
        if (cachedResults.isEmpty()) {
            cachedResults =
                d2
                    .dataStoreModule()
                    .localDataStore()
                    .value(STORED_LOCATION)
                    .blockingGet()
                    ?.value()
                    ?.let { storedValue ->
                        gson.fromJson(
                            storedValue,
                            object : TypeToken<List<LocationItemModel.StoredResult>>() {}.type,
                        )
                    } ?: emptyList()
        }

        return cachedResults.flexibleFilter(query).reversed()
    }

    private fun List<LocationItemModel.StoredResult>.flexibleFilter(query: String?): List<LocationItemModel.StoredResult> =
        filter { item ->
            query?.takeIf { it.isNotEmpty() }?.let {
                (jaroWinklerDistance.apply(item.title, query) >= jaroWinklerThreshold) or
                    (jaroWinklerDistance.apply(item.subtitle, query) >= jaroWinklerThreshold)
            } ?: true
        }

    suspend fun storeLocation(location: LocationItemModel) {
        val currentList = getAvailableLocations().toMutableList()
        currentList.addIf(
            currentList.none { it.latitude == location.latitude && it.longitude == location.longitude },
            LocationItemModel.StoredResult(
                storedTitle = location.title,
                storedSubtitle = location.subtitle,
                storedLatitude = location.latitude,
                storedLongitude = location.longitude,
            ),
        )
        if (currentList.size > maxStoredLocations) {
            currentList.removeAt(0)
        }
        d2.dataStoreModule().localDataStore().value(STORED_LOCATION).blockingSet(
            gson.toJson(currentList),
        )
    }
}
