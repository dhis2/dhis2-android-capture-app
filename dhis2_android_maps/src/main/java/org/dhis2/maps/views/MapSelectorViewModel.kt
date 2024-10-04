package org.dhis2.maps.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.dhis2.commons.extensions.truncate
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.maps.geometry.getPointLatLng
import org.dhis2.maps.layer.basemaps.BaseMapStyle
import org.dhis2.maps.model.AccuracyRange
import org.dhis2.maps.model.MapSelectorScreenState
import org.dhis2.maps.model.toAccuracyRance
import org.dhis2.maps.usecases.GeocoderSearch
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.maps.usecases.SearchLocationManager
import org.dhis2.maps.utils.CoordinateUtils
import org.dhis2.maps.utils.GetMapData
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.mobile.ui.designsystem.component.model.LocationItemModel
import com.mapbox.geojson.Geometry as MapGeometry

class MapSelectorViewModel(
    val featureType: FeatureType,
    private val initialCoordinates: String?,
    private val mapStyleConfig: MapStyleConfiguration,
    private val geocoder: GeocoderSearch,
    private val searchLocationManager: SearchLocationManager,
    private val dispatchers: DispatcherProvider,
) : ViewModel() {

    enum class CaptureMode {
        NONE,
        GPS,
        MANUAL,
        SEARCH,
        ;

        fun isNone() = this == NONE
        fun isGps() = this == GPS
        fun isManual() = this == MANUAL
        fun isSearch() = this == SEARCH
    }

    private val initialSelectedLocation: SelectedLocation

    private var _currentFeature: Feature? = null
    private var _currentVisibleRegion: LatLngBounds? = null
    private var searchRegion: LatLngBounds? = null

    private val _searchLocationQuery = MutableStateFlow("")

    private val _screenState = MutableStateFlow(
        MapSelectorScreenState(
            mapData = GetMapData(null, emptyList(), CaptureMode.NONE),
            locationItems = emptyList(),
            selectedLocation = SelectedLocation.None(),
            captureMode = if (initialCoordinates == null) CaptureMode.GPS else CaptureMode.NONE,
            accuracyRange = AccuracyRange.None(),
            searchOnAreaVisible = false,
            displayPolygonInfo = featureType == FeatureType.POLYGON,
        ),
    )

    val screenState = _screenState
        .onStart {
            initState()
        }
        .debounce(500)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            MapSelectorScreenState(
                mapData = GetMapData(null, emptyList(), CaptureMode.NONE),
                locationItems = emptyList(),
                selectedLocation = SelectedLocation.None(),
                captureMode = if (initialCoordinates == null) CaptureMode.GPS else CaptureMode.NONE,
                accuracyRange = AccuracyRange.None(),
                searchOnAreaVisible = false,
                displayPolygonInfo = featureType == FeatureType.POLYGON,
            ),
        )

    init {
        val initialGeometry =
            CoordinateUtils.geometryFromStringCoordinates(featureType, initialCoordinates)
        initialGeometry?.let {
            viewModelScope.launch(dispatchers.io()) {
                updateCurrentGeometry(it)
            }
        }
        initialSelectedLocation = if (initialGeometry is Point) {
            SelectedLocation.ManualResult(
                initialGeometry.latitude(),
                initialGeometry.longitude(),
            )
        } else {
            SelectedLocation.None()
        }

        registerSearchListener()
    }

    private fun initState() {
        viewModelScope.launch(dispatchers.io()) {
            _screenState.value = _screenState.value.copy(selectedLocation = initialSelectedLocation)
        }
    }

    fun fetchMapStyles(): List<BaseMapStyle> {
        return mapStyleConfig.fetchMapStyles()
    }

    private suspend fun updateCurrentGeometry(geometry: MapGeometry) =
        withContext(dispatchers.io()) {
            updateMapData(feature = Feature.fromGeometry(geometry))
        }

    private suspend fun updateMapData(
        feature: Feature? = null,
        locationItems: List<LocationItemModel> = _screenState.value.locationItems,
        captureMode: CaptureMode = _screenState.value.captureMode,
    ) = withContext(dispatchers.io()) {
        _currentFeature = feature
        _screenState.value = _screenState.value.copy(
            mapData = GetMapData(feature, locationItems, captureMode),
            locationItems = locationItems,
            captureMode = captureMode,
        )
    }

    private suspend fun updateSelectedGeometry(
        point: SelectedLocation,
    ) = withContext(dispatchers.io()) {
        if (point !is SelectedLocation.None) {
            val newPoint = Point.fromLngLat(point.longitude, point.latitude)
            when (featureType) {
                FeatureType.POINT -> updateCurrentGeometry(newPoint)

                FeatureType.POLYGON -> {
                    val coordinates =
                        (_currentFeature?.geometry() as Polygon?)?.coordinates()
                            ?.map { points ->
                                val newList = points.toMutableList()
                                newList.add(newPoint)
                                newList
                            } ?: listOf(listOf(newPoint))
                    updateCurrentGeometry(
                        Polygon.fromLngLats(coordinates),
                    )
                }

                else -> {
                    // no-op
                }
            }
        } else {
            updateMapData()
        }
        updateSelectedLocation(point)
    }

    private fun updateSelectedLocation(selectedLocation: SelectedLocation) {
        _screenState.value = _screenState.value.copy(selectedLocation = selectedLocation)
    }

    private fun onNewLocationAccuracy(accuracy: Float) {
        _screenState.value = _screenState.value.copy(accuracyRange = accuracy.toAccuracyRance())
    }

    fun onSaveCurrentGeometry(onValueReady: (String?) -> Unit) {
        viewModelScope.launch(dispatchers.io()) {
            val result = async {
                when (val geometry = _currentFeature?.geometry()) {
                    is Point -> {
                        Gson().toJson(
                            geometry.coordinates().map { coordinate -> coordinate.truncate() },
                        )
                    }

                    is Polygon -> {
                        val value = geometry.coordinates().map { polygon ->
                            polygon.map { point ->
                                point.coordinates().map { coordinate -> coordinate.truncate() }
                            }
                        }
                        Gson().toJson(value)
                    }

                    else -> null
                }
            }
            onValueReady(result.await())
        }
    }

    fun onNewLocation(gpsResult: SelectedLocation.GPSResult) {
        viewModelScope.launch(dispatchers.io()) {
            if (gpsResult.accuracy < _screenState.value.accuracyRange.value.toFloat()) {
                onNewLocationAccuracy(gpsResult.accuracy)
            }
            when {
                featureType == FeatureType.POINT && _screenState.value.captureMode.isGps() ->
                    updateSelectedGeometry(gpsResult)
            }
        }
    }

    fun addPointToPolygon(polygonPoint: List<Double>) {
        if (featureType == FeatureType.POLYGON) {
            viewModelScope.launch(dispatchers.io()) {
                _currentFeature?.let { feature ->
                    val geometry = (feature.geometry() as Polygon)
                    geometry.coordinates().first()
                        .add(Point.fromLngLat(polygonPoint[0], polygonPoint[1]))
                    updateCurrentGeometry(geometry)
                } ?: updateCurrentGeometry(
                    Polygon.fromLngLats(
                        listOf(listOf(Point.fromLngLat(polygonPoint[0], polygonPoint[1]))),
                    ),
                )
            }
        }
    }

    fun removePointFromPolygon(index: Int) {
        if (featureType == FeatureType.POLYGON) {
            viewModelScope.launch(dispatchers.io()) {
                _currentFeature?.let { feature ->
                    val geometry = (feature.geometry() as Polygon)
                    geometry.coordinates().first()
                        .removeAt(index)
                    updateCurrentGeometry(geometry)
                }
            }
        }
    }

    private fun registerSearchListener() {
        viewModelScope.launch(dispatchers.io()) {
            _searchLocationQuery.debounce(1000)
                .collectLatest(::performLocationSearch)
        }
    }

    fun onSearchOnAreaClick() {
        viewModelScope.launch(dispatchers.io()) {
            setCaptureMode(CaptureMode.SEARCH)
            performLocationSearch()
        }
    }

    private suspend fun performLocationSearch(query: String = _searchLocationQuery.value) =
        withContext(dispatchers.io()) {
            if (_screenState.value.captureMode.isSearch()) {
                searchRegion = _currentVisibleRegion
                val filteredPreviousLocation =
                    searchLocationManager.getAvailableLocations(query)
                geocoder.getLocationFromName(query, searchRegion) { searchItems ->
                    onSearchResults(filteredPreviousLocation + searchItems)
                }
                updateSearchOnThisAreaVisible(false)
            }
        }

    private fun onSearchResults(locationItemModels: List<LocationItemModel>) {
        viewModelScope.launch(dispatchers.io()) {
            onClearSelectedLocation()
            updateLocationItems(locationItemModels)
        }
    }

    private suspend fun updateLocationItems(
        locationItems: List<LocationItemModel>,
    ) = withContext(dispatchers.io()) {
        if (locationItems.isNotEmpty() && _currentFeature != null) {
            onClearSelectedLocation()
        }
        updateMapData(locationItems = locationItems)
    }

    fun onSearchLocation(searchQuery: String) {
        _searchLocationQuery.value = searchQuery
    }

    fun onLocationSelected(selectedLocation: LocationItemModel) {
        viewModelScope.launch(dispatchers.io()) {
            updateSelectedGeometry(
                SelectedLocation.SearchResult(
                    title = selectedLocation.title,
                    address = selectedLocation.subtitle,
                    resultLatitude = selectedLocation.latitude,
                    resultLongitude = selectedLocation.longitude,
                ),
            )
            searchLocationManager.storeLocation(selectedLocation)
        }
    }

    fun onClearSearchClicked() {
        viewModelScope.launch(dispatchers.io()) {
            setCaptureMode(CaptureMode.NONE)
            onClearSelectedLocation()
            onSearchLocation("")
        }
    }

    fun onPinClicked(feature: Feature) {
        viewModelScope.launch(dispatchers.io()) {
            updateSelectedGeometry(
                SelectedLocation.SearchResult(
                    title = feature.getStringProperty("title"),
                    address = feature.getStringProperty("subtitle"),
                    resultLatitude = feature.getPointLatLng().latitude,
                    resultLongitude = feature.getPointLatLng().longitude,
                ),
            )
        }
    }

    fun onMapClicked(point: LatLng) {
        viewModelScope.launch(dispatchers.io()) {
            setCaptureMode(CaptureMode.MANUAL)
            updateSelectedGeometry(
                SelectedLocation.ManualResult(point.latitude, point.longitude),
            )
        }
    }

    private suspend fun onClearSelectedLocation() {
        updateSelectedGeometry(
            if (initialCoordinates != null) {
                initialSelectedLocation
            } else {
                SelectedLocation.None()
            },
        )
    }

    suspend fun setCaptureMode(selectedMode: CaptureMode) = withContext(dispatchers.io()) {
        val locationItems =
            if (selectedMode.isSearch() && _screenState.value.locationItems.isEmpty()) {
                searchLocationManager.getAvailableLocations()
            } else {
                emptyList()
            }
        updateMapData(captureMode = selectedMode, locationItems = locationItems)
    }

    fun updateCurrentVisibleRegion(mapBounds: LatLngBounds?) {
        _currentVisibleRegion = mapBounds
        updateSearchOnThisAreaVisible()
    }

    private fun updateSearchOnThisAreaVisible(
        canSearch: Boolean = _searchLocationQuery.value.isNotBlank(),
    ) {
        _screenState.value = _screenState.value.copy(searchOnAreaVisible = canSearch)
    }
}
