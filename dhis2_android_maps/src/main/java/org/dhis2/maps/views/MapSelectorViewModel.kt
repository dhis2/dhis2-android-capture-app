package org.dhis2.maps.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.dhis2.commons.extensions.truncate
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.maps.geometry.getPointLatLng
import org.dhis2.maps.layer.basemaps.BaseMapStyle
import org.dhis2.maps.model.AccuracyRange
import org.dhis2.maps.model.toAccuracyRance
import org.dhis2.maps.usecases.GeocoderSearch
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.maps.usecases.SearchLocationManager
import org.dhis2.maps.utils.CoordinateUtils
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.mobile.ui.designsystem.component.model.LocationItemModel
import java.util.UUID
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

    private var _captureMode =
        MutableStateFlow(if (initialCoordinates == null) CaptureMode.GPS else CaptureMode.NONE)
    val captureMode = _captureMode.asStateFlow()

    private var currentUserLocation: LatLng? = null
    private var currentUserLocationAccuracy: Float? = null

    private val initialSelectedLocation: SelectedLocation
    private val _selectedLocation = MutableStateFlow<SelectedLocation>(SelectedLocation.None())
    val selectedLocation = _selectedLocation.asStateFlow()

    private var _currentFeature = MutableStateFlow<Feature?>(null)
    val canSave = _currentFeature.map { it != null }
        .onStart { emit(initialCoordinates != null) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            initialCoordinates != null,
        )

    private val _accuracyRange = MutableStateFlow<AccuracyRange>(AccuracyRange.None())
    val accuracyRange = _accuracyRange.asStateFlow()

    private val _locationItems = MutableStateFlow<List<LocationItemModel>>(emptyList())
    val locationItems = _locationItems
        .onStart { loadAvailableLocations() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            emptyList(),
        )

    private val _currentVisibleRegion = MutableStateFlow<LatLngBounds?>(null)

    val mapFeatures = combine(
        _currentFeature,
        _locationItems,
        _captureMode,
    ) { currentFeature, locationItems, captureMode ->
        val selectedFeatures = currentFeature?.let { feature ->
            feature.addStringProperty("id", UUID.randomUUID().toString())
            feature.addBooleanProperty("places", true)
            feature.addBooleanProperty("selected", true)
            listOf(feature)
        } ?: emptyList()

        val locationFeatures = locationItems
            .takeIf { captureMode.isSearch() }
            ?.map {
                Feature.fromGeometry(
                    Point.fromLngLat(it.longitude, it.latitude),
                ).also { feature ->
                    feature.addStringProperty("id", UUID.randomUUID().toString())
                    feature.addBooleanProperty("places", true)
                    feature.addBooleanProperty("selected", false)
                    feature.addStringProperty("title", it.title)
                    feature.addStringProperty("subtitle", it.subtitle)
                }
            } ?: emptyList()
        FeatureCollection.fromFeatures(selectedFeatures + locationFeatures)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000L),
        FeatureCollection.fromFeatures(emptyList()),
    )

    private val _searchLocationQuery = MutableStateFlow("")

    private val _searchOnThisAreaVisible = MutableStateFlow(false)
    val searchOnThisAreaVisible = _searchOnThisAreaVisible.asStateFlow()

    init {
        val initialGeometry =
            CoordinateUtils.geometryFromStringCoordinates(featureType, initialCoordinates)
        initialGeometry?.let { updateCurrentGeometry(it) }
        initialSelectedLocation = if (initialGeometry is Point) {
            SelectedLocation.ManualResult(
                initialGeometry.latitude(),
                initialGeometry.longitude(),
            )
        } else {
            SelectedLocation.None()
        }
        updateSelectedLocation(initialSelectedLocation)
    }

    fun init() {
        registerSearchListener()
    }

    fun fetchMapStyles(): List<BaseMapStyle> {
        return mapStyleConfig.fetchMapStyles()
    }

    private fun updateCurrentGeometry(geometry: MapGeometry) {
        _currentFeature.value = Feature.fromGeometry(geometry)
    }

    fun updateSelectedGeometry(feature: Feature) {
        updateSelectedGeometry(
            SelectedLocation.SearchResult(
                title = feature.getStringProperty("title"),
                address = feature.getStringProperty("subtitle"),
                resultLatitude = feature.getPointLatLng().latitude,
                resultLongitude = feature.getPointLatLng().longitude,
            ),
        )
    }

    private fun updateSelectedGeometry(point: SelectedLocation) {
        viewModelScope.launch(dispatchers.io()) {
            if (point !is SelectedLocation.None) {
                val newPoint = Point.fromLngLat(point.longitude, point.latitude)
                when (featureType) {
                    FeatureType.POINT -> updateCurrentGeometry(newPoint)

                    FeatureType.POLYGON -> {
                        val coordinates =
                            (_currentFeature.value?.geometry() as Polygon?)?.coordinates()
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
                _currentFeature.emit(null)
            }
            updateSelectedLocation(point)
        }
    }

    private fun updateSelectedLocation(selectedLocation: SelectedLocation) {
        _selectedLocation.value = selectedLocation
    }

    private fun onNewLocationAccuracy(accuracy: Float) {
        _accuracyRange.value = accuracy.toAccuracyRance()
    }

    fun onSaveCurrentGeometry(onValueReady: (String?) -> Unit) {
        viewModelScope.launch(dispatchers.io()) {
            val result = async {
                when (val geometry = _currentFeature.value?.geometry()) {
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
        if (currentUserLocation == null || gpsResult.accuracy < currentUserLocationAccuracy!!) {
            currentUserLocation = gpsResult.asLatLng()
            currentUserLocationAccuracy = gpsResult.accuracy
            onNewLocationAccuracy(gpsResult.accuracy)
        }
        when {
            featureType == FeatureType.POINT && _captureMode.value.isGps() ->
                updateSelectedGeometry(gpsResult)
        }
    }

    fun addPointToPolygon(polygonPoint: List<Double>) {
        if (featureType == FeatureType.POLYGON) {
            viewModelScope.launch(dispatchers.io()) {
                _currentFeature.value?.let { feature ->
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
                _currentFeature.value?.let { feature ->
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

    suspend fun performLocationSearch(query: String = _searchLocationQuery.value) {
        if (_captureMode.value.isSearch()) {
            val filteredPreviousLocation =
                searchLocationManager.getAvailableLocations(query)
            geocoder.getLocationFromName(query, _currentVisibleRegion.value) { searchItems ->
                updateLocationItems(filteredPreviousLocation + searchItems)
            }
            updateSearchOnThisAreaVisible(false)
        }
    }

    private suspend fun loadAvailableLocations() {
        val availableLocations = searchLocationManager.getAvailableLocations()
        _locationItems.value = availableLocations
    }

    private fun updateLocationItems(locationItems: List<LocationItemModel>) {
        if (locationItems.isNotEmpty() && _currentFeature.value != null) {
            onClearSelectedLocation()
        }
        _locationItems.value = locationItems
    }

    fun onSearchLocation(searchQuery: String) {
        _searchLocationQuery.value = searchQuery
    }

    fun onLocationSelected(selectedLocation: LocationItemModel) {
        updateSelectedGeometry(
            SelectedLocation.SearchResult(
                title = selectedLocation.title,
                address = selectedLocation.subtitle,
                resultLatitude = selectedLocation.latitude,
                resultLongitude = selectedLocation.longitude,
            ),
        )
        viewModelScope.launch(dispatchers.io()) {
            searchLocationManager.storeLocation(selectedLocation)
        }
    }

    fun onClearSearchClicked() {
        setCaptureMode(CaptureMode.NONE)
        onSearchLocation("")
    }

    fun onClickedOnMap(point: LatLng) {
        setCaptureMode(CaptureMode.MANUAL)
        updateSelectedGeometry(
            SelectedLocation.ManualResult(point.latitude, point.longitude),
        )
    }

    private fun onClearSelectedLocation() {
        updateSelectedGeometry(
            if (initialCoordinates != null) {
                initialSelectedLocation
            } else {
                SelectedLocation.None()
            },
        )
    }

    fun setCaptureMode(selectedMode: CaptureMode) {
        onClearSelectedLocation()
        _captureMode.value = selectedMode
    }

    fun shouldDisplayAccuracyIndicator(selectedLocation: SelectedLocation) =
        featureType == FeatureType.POINT &&
            _captureMode.value.isGps() &&
            selectedLocation is SelectedLocation.None

    fun shouldDisplayManualResult(selectedLocation: SelectedLocation) =
        featureType == FeatureType.POINT &&
            selectedLocation is SelectedLocation.ManualResult

    fun shouldDisplaySearchResult(selectedLocation: SelectedLocation) =
        featureType == FeatureType.POINT && selectedLocation is SelectedLocation.SearchResult

    fun shouldDisplayPolygonInfo() = featureType == FeatureType.POLYGON

    fun updateCurrentVisibleRegion(mapBounds: LatLngBounds?) {
        _currentVisibleRegion.value = mapBounds
        updateSearchOnThisAreaVisible()
    }

    private fun updateSearchOnThisAreaVisible(
        canSearch: Boolean = _searchLocationQuery.value.isNotBlank(),
    ) {
        _searchOnThisAreaVisible.value = canSearch
    }
}
