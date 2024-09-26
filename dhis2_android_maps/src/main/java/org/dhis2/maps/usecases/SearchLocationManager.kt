package org.dhis2.maps.usecases

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

    suspend fun getAvailableLocations(query: String? = null): List<LocationItemModel.StoredResult> {
        if (cachedResults.isEmpty()) {
            cachedResults =
                d2.dataStoreModule().localDataStore().value(STORED_LOCATION).blockingGet()?.value()
                    ?.let { storedValue ->
                        gson.fromJson(
                            storedValue,
                            object : TypeToken<List<LocationItemModel.StoredResult>>() {}.type,
                        )
                    } ?: emptyList()
        }

        return cachedResults.filter { item ->
            query?.let {
                item.title.contains(query) or item.subtitle.contains(query)
            } ?: true
        }.reversed()
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
