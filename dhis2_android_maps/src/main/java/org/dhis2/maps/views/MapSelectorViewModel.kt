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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
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
import org.dhis2.maps.extensions.toLatLng
import org.dhis2.maps.geometry.getPointLatLng
import org.dhis2.maps.layer.basemaps.BaseMapStyle
import org.dhis2.maps.model.AccuracyRange
import org.dhis2.maps.model.toAccuracyRance
import org.dhis2.maps.usecases.GeocoderSearch
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.maps.usecases.SearchLocationManager
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
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
    private val _selectedLocation = MutableStateFlow<SelectedLocation>(
        SelectedLocation.None(),
    )
    val selectedLocation = _selectedLocation.asStateFlow()
        .onStart {
            getInitialLocation()
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            SelectedLocation.None(),
        )
    private var _currentFeature = MutableStateFlow<Feature?>(null)
    val canSave = _currentFeature.map { it != null }
        .onStart { emit(initialCoordinates != null) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            initialCoordinates != null,
        )

    /*   private val _featureCollection =
           MutableStateFlow<FeatureCollection>(FeatureCollection.fromFeatures(emptyList()))*/
    private val _accuracyRange = MutableSharedFlow<AccuracyRange>()
    val accuracyRange: Flow<AccuracyRange> = _accuracyRange
    private val _locationItems = MutableStateFlow<List<LocationItemModel>>(emptyList())
    val locationItems = _locationItems.asStateFlow()
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
    private val _swipeToChangeLocationVisible = MutableStateFlow(initialCoordinates != null)
    val swipeToChangeLocationVisible = _swipeToChangeLocationVisible.asStateFlow()
    private val _searchOnThisAreaVisible = MutableStateFlow(false)
    val searchOnThisAreaVisible = _searchOnThisAreaVisible.asStateFlow()

    fun init() {
        registerSearchListener()
    }

    private suspend fun getInitialLocation() {
        val geometry: MapGeometry? = initialCoordinates?.let {
            val geometry = Geometry.builder()
                .type(featureType)
                .coordinates(initialCoordinates)
                .build()
            when (featureType) {
                FeatureType.POINT -> buildPointGeometry(geometry)
                FeatureType.POLYGON -> buildPolygonGeometry(geometry)
                else ->
                    null
            }
        }
        geometry?.let {
            updateCurrentGeometry(it)
            if (it is Point) {
                updateSelectedLocation(
                    SelectedLocation.ManualResult(
                        it.latitude(),
                        it.longitude(),
                    ),
                )
            }
        }
    }

    fun fetchMapStyles(): List<BaseMapStyle> {
        return mapStyleConfig.fetchMapStyles()
    }

    private suspend fun updateCurrentGeometry(geometry: MapGeometry) {
        _currentFeature.emit(Feature.fromGeometry(geometry))
    }

    private fun buildPointGeometry(geometry: Geometry): MapGeometry {
        return with(GeometryHelper.getPoint(geometry).toLatLng()) {
            Point.fromLngLat(longitude, latitude)
        }
    }

    private fun buildPolygonGeometry(geometry: Geometry): MapGeometry {
        return with(GeometryHelper.getPolygon(geometry)) {
            val polygonPointList = map { polygon ->
                polygon.map {
                    Point.fromLngLat(it[0], it[1])
                }
            }
            Polygon.fromLngLats(polygonPointList)
        }
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

    fun updateSelectedGeometry(point: SelectedLocation) {
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

    private suspend fun updateSelectedLocation(selectedLocation: SelectedLocation) {
        _selectedLocation.emit(selectedLocation)
        if (selectedLocation !is SelectedLocation.None) {
            _swipeToChangeLocationVisible.emit(true)
        }
    }

    private fun onNewLocationAccuracy(accuracy: Float) {
        viewModelScope.launch(dispatchers.io()) {
            _accuracyRange.emit(accuracy.toAccuracyRance())
        }
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
            viewModelScope.launch {
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
            viewModelScope.launch {
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
            _searchOnThisAreaVisible.emit(false)
        }
    }

    private fun loadAvailableLocations() {
        viewModelScope.launch(dispatchers.io()) {
            _locationItems.emit(
                searchLocationManager.getAvailableLocations(),
            )
        }
    }

    private fun updateLocationItems(locationItems: List<LocationItemModel>) {
        viewModelScope.launch(dispatchers.io()) {
            if (locationItems.isNotEmpty() && _currentFeature.value != null) {
                onClearSelectedLocation()
            }
            _locationItems.emit(locationItems)
        }
    }

    fun onSearchLocation(searchQuery: String) {
        viewModelScope.launch(dispatchers.io()) {
            _searchLocationQuery.emit(searchQuery)
        }
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

    fun onClearSelectedLocation() {
        updateSelectedGeometry(SelectedLocation.None())
    }

    fun setCaptureMode(selectedMode: CaptureMode) {
        viewModelScope.launch(dispatchers.io()) {
            onClearSelectedLocation()
            _captureMode.emit(selectedMode)
        }
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
        viewModelScope.launch(dispatchers.io()) {
            _currentVisibleRegion.emit(mapBounds)
            val canSearch = _searchLocationQuery.value.isNotBlank()
            _searchOnThisAreaVisible.emit(canSearch)
        }
    }
}
