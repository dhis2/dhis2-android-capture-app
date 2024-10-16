package org.dhis2.maps.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.mapboxsdk.geometry.LatLng
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.maps.geometry.getPointLatLng
import org.dhis2.maps.layer.basemaps.BaseMapStyle
import org.dhis2.maps.location.LocationState
import org.dhis2.maps.location.LocationState.FIXED
import org.dhis2.maps.location.LocationState.NOT_FIXED
import org.dhis2.maps.model.AccuracyRange
import org.dhis2.maps.model.MapData
import org.dhis2.maps.model.MapSelectorScreenState
import org.dhis2.maps.model.toAccuracyRance
import org.dhis2.maps.usecases.GeocoderSearch
import org.dhis2.maps.usecases.MapStyleConfiguration
import org.dhis2.maps.usecases.SearchLocationManager
import org.dhis2.maps.utils.AvailableLatLngBounds
import org.dhis2.maps.utils.CoordinateUtils
import org.dhis2.maps.utils.GeometryCoordinate
import org.dhis2.maps.utils.GetMapData
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.mobile.ui.designsystem.component.model.LocationItemModel

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
        MANUAL_SWIPE,
        SEARCH,
        ;

        fun isNone() = this == NONE
        fun isGps() = this == GPS
        fun isManual() = this == MANUAL
        fun isSwipe() = this == MANUAL_SWIPE
        fun isSearch() = this == SEARCH
    }

    private val initialGeometry =
        CoordinateUtils.geometryFromStringCoordinates(featureType, initialCoordinates)
    private var initialSelectedLocation = if (initialGeometry is Point) {
        SelectedLocation.ManualResult(
            selectedLatitude = initialGeometry.latitude(),
            selectedLongitude = initialGeometry.longitude(),
        )
    } else {
        SelectedLocation.None()
    }

    private var _lastGPSLocation: SelectedLocation? = null
    private var _currentFeature: Feature? = initialGeometry?.let { Feature.fromGeometry(it) }
    private var _currentVisibleRegion: AvailableLatLngBounds? = null
    private var searchRegion: AvailableLatLngBounds? = null
    private val _searchLocationQuery = MutableStateFlow("")

    private val _geometryCoordinateResultChannel = Channel<GeometryCoordinate?>()
    val geometryCoordinateResultChannel = _geometryCoordinateResultChannel.receiveAsFlow()

    private val _screenState = MutableStateFlow(
        MapSelectorScreenState(
            mapData = GetMapData(_currentFeature, emptyList(), CaptureMode.NONE),
            locationItems = emptyList(),
            selectedLocation = initialSelectedLocation,
            captureMode = if (initialCoordinates == null) CaptureMode.GPS else CaptureMode.NONE,
            accuracyRange = AccuracyRange.None(),
            searchOnAreaVisible = false,
            displayPolygonInfo = featureType == FeatureType.POLYGON,
            locationState = NOT_FIXED,
            isManualCaptureEnabled = mapStyleConfig.isManualCaptureEnabled(),
            forcedLocationAccuracy = mapStyleConfig.getForcedLocationAccuracy(),
            zoomLevel = 0f,
        ),
    )

    val screenState = _screenState.asStateFlow()

    init {
        registerSearchListener()
    }

    fun fetchMapStyles(): List<BaseMapStyle> {
        return mapStyleConfig.fetchMapStyles()
    }

    private fun updateScreenState(
        mapData: MapData = _screenState.value.mapData,
        locationItems: List<LocationItemModel> = _screenState.value.locationItems,
        selectedLocation: SelectedLocation = _screenState.value.selectedLocation,
        captureMode: CaptureMode = _screenState.value.captureMode,
        accuracyRange: AccuracyRange = _screenState.value.accuracyRange,
        searchOnAreaVisible: Boolean = _screenState.value.searchOnAreaVisible,
        displayPolygonInfo: Boolean = _screenState.value.displayPolygonInfo,
        locationState: LocationState = _screenState.value.locationState,
        zoomLevel: Float = _screenState.value.zoomLevel,
    ) {
        _screenState.update {
            it.copy(
                mapData = mapData,
                locationItems = locationItems,
                selectedLocation = selectedLocation,
                captureMode = captureMode,
                accuracyRange = accuracyRange,
                searchOnAreaVisible = searchOnAreaVisible,
                displayPolygonInfo = displayPolygonInfo,
                locationState = locationState,
                zoomLevel = zoomLevel,
            )
        }
    }

    private fun updateSelectedGeometry(point: SelectedLocation) =
        if (point !is SelectedLocation.None) {
            val newPoint = Point.fromLngLat(point.longitude, point.latitude)
            when (featureType) {
                FeatureType.POINT -> Feature.fromGeometry(newPoint)

                FeatureType.POLYGON -> {
                    val coordinates =
                        (_currentFeature?.geometry() as Polygon?)?.coordinates()
                            ?.map { points ->
                                val newList = points.toMutableList()
                                newList.add(newPoint)
                                newList
                            } ?: listOf(listOf(newPoint))
                    Feature.fromGeometry(
                        Polygon.fromLngLats(coordinates),
                    )
                }

                else -> {
                    null
                }
            }
        } else {
            null
        }

    private suspend fun onSaveCurrentGeometry() {
        val coordinates = CoordinateUtils.geometryCoordinates(_currentFeature?.geometry())
        _geometryCoordinateResultChannel.send(coordinates)
    }

    fun onNewLocation(gpsResult: SelectedLocation.GPSResult) {
        viewModelScope.launch(dispatchers.io()) {
            _lastGPSLocation = gpsResult
            if (_screenState.value.canCaptureGps(gpsResult.accuracy)) {
                _currentFeature = updateSelectedGeometry(gpsResult)
                val mapData = when {
                    featureType == FeatureType.POINT && _screenState.value.captureMode.isGps() ->
                        GetMapData(
                            currentFeature = _currentFeature,
                            locationItems = _screenState.value.locationItems,
                            captureMode = _screenState.value.captureMode,
                        )

                    else -> _screenState.value.mapData
                }

                updateScreenState(
                    mapData = mapData,
                    selectedLocation = gpsResult,
                    accuracyRange = gpsResult.accuracy.toAccuracyRance(),
                )
            }
        }
    }

    fun addPointToPolygon(polygonPoint: List<Double>) {
        if (featureType == FeatureType.POLYGON) {
            viewModelScope.launch(dispatchers.io()) {
                val newGeometry = _currentFeature?.let { feature ->
                    val geometry = (feature.geometry() as Polygon)
                    geometry.coordinates().first()
                        .add(Point.fromLngLat(polygonPoint[0], polygonPoint[1]))
                    geometry
                } ?: Polygon.fromLngLats(
                    listOf(listOf(Point.fromLngLat(polygonPoint[0], polygonPoint[1]))),
                )
                _currentFeature = Feature.fromGeometry(newGeometry)
                updateScreenState(
                    mapData = GetMapData(
                        _currentFeature,
                        _screenState.value.locationItems,
                        _screenState.value.captureMode,
                    ),
                )
            }
        }
    }

    fun removePointFromPolygon(index: Int) {
        if (featureType == FeatureType.POLYGON) {
            viewModelScope.launch(dispatchers.io()) {
                val newGeometry = _currentFeature?.let { feature ->
                    val geometry = (feature.geometry() as Polygon)
                    geometry.coordinates().first()
                        .removeAt(index)
                    geometry
                }
                _currentFeature = Feature.fromGeometry(newGeometry)
                updateScreenState(
                    mapData = GetMapData(
                        _currentFeature,
                        _screenState.value.locationItems,
                        _screenState.value.captureMode,
                    ),
                )
            }
        }
    }

    private fun registerSearchListener() {
        _searchLocationQuery
            .debounce(1000)
            .onEach(::performLocationSearch)
            .launchIn(viewModelScope)
    }

    fun onSearchOnAreaClick() {
        viewModelScope.launch(dispatchers.io()) {
            initSearchMode()
            performLocationSearch()
        }
    }

    private suspend fun performLocationSearch(
        query: String = _searchLocationQuery.value,
        regionToSearch: AvailableLatLngBounds? = _currentVisibleRegion,
    ) {
        if (_screenState.value.captureMode.isSearch()) {
            val filteredPreviousLocation =
                searchLocationManager.getAvailableLocations(query)
            val searchItems = geocoder.getLocationFromName(query, regionToSearch)
            _currentFeature = null
            val locationItems = searchItems /*+ filteredPreviousLocation*/
            searchRegion = regionToSearch
            updateScreenState(
                mapData = GetMapData(
                    currentFeature = _currentFeature,
                    locationItems = locationItems,
                    captureMode = _screenState.value.captureMode,
                ),
                locationItems = locationItems,
                selectedLocation = SelectedLocation.None(),
                searchOnAreaVisible = false,
            )
        }
    }

    fun onSearchLocation(searchQuery: String) {
        _searchLocationQuery.value = searchQuery
    }

    fun onLocationSelected(selectedLocation: LocationItemModel) {
        viewModelScope.launch(dispatchers.io()) {
            val location = SelectedLocation.SearchResult(
                title = selectedLocation.title,
                address = selectedLocation.subtitle,
                resultLatitude = selectedLocation.latitude,
                resultLongitude = selectedLocation.longitude,
            )

            _currentFeature = updateSelectedGeometry(location)

            async {
                updateScreenState(
                    mapData = GetMapData(
                        _currentFeature,
                        _screenState.value.locationItems,
                        _screenState.value.captureMode,
                    ),
                    selectedLocation = location,
                )
            }.await()

            async {
                searchLocationManager.storeLocation(selectedLocation)
            }.await()
        }
    }

    fun onClearSearchClicked() {
        viewModelScope.launch(dispatchers.io()) {
            val (selectedLocation, currentFeature) = onClearSelectedLocation()
            _currentFeature = currentFeature

            updateScreenState(
                captureMode = CaptureMode.NONE,
                mapData = GetMapData(
                    _currentFeature,
                    _screenState.value.locationItems,
                    CaptureMode.NONE,
                ),
                selectedLocation = selectedLocation,
            )

            onSearchLocation("")
        }
    }

    fun onPinClicked(feature: Feature) {
        viewModelScope.launch(dispatchers.io()) {
            val selectedLocation = SelectedLocation.SearchResult(
                title = feature.getStringProperty("title"),
                address = feature.getStringProperty("subtitle"),
                resultLatitude = feature.getPointLatLng().latitude,
                resultLongitude = feature.getPointLatLng().longitude,
            )
            _currentFeature = feature
            updateScreenState(
                mapData = GetMapData(
                    feature,
                    _screenState.value.locationItems,
                    _screenState.value.captureMode,
                ),
                selectedLocation = selectedLocation,
            )
        }
    }

    private fun onClearSelectedLocation(): Pair<SelectedLocation, Feature?> {
        val selectedLocation = if (initialCoordinates != null) {
            initialSelectedLocation
        } else {
            SelectedLocation.None()
        }
        val feature = updateSelectedGeometry(selectedLocation)
        return Pair(selectedLocation, feature)
    }

    fun updateCurrentVisibleRegion(mapBounds: AvailableLatLngBounds?, zoomLevel: Float) {
        _currentVisibleRegion = mapBounds
        updateScreenState(searchOnAreaVisible = _searchLocationQuery.value.isNotBlank(), zoomLevel = zoomLevel)
    }

    fun initSearchMode() {
        viewModelScope.launch(dispatchers.io()) {
            val locationItems = _screenState.value.locationItems.ifEmpty {
                searchLocationManager.getAvailableLocations()
            }

            updateScreenState(
                captureMode = CaptureMode.SEARCH,
                locationItems = locationItems,
            )
        }
    }

    fun onMyLocationButtonClick() {
        viewModelScope.launch(dispatchers.io()) {
            _currentFeature = _lastGPSLocation?.let { updateSelectedGeometry(it) }
            updateScreenState(
                mapData = GetMapData(
                    _currentFeature,
                    _screenState.value.locationItems,
                    CaptureMode.GPS,
                ),
                selectedLocation = _lastGPSLocation ?: SelectedLocation.None(),
                captureMode = CaptureMode.GPS,
                locationState = FIXED,
            )
        }
    }

    fun onDoneClick() {
        viewModelScope.launch(dispatchers.io()) {
            onSaveCurrentGeometry()
        }
    }

    fun onMove(point: LatLng) {
        if (canCaptureWithSwipe()) {
            val captureMode = if (!_screenState.value.captureMode.isSwipe()) {
                CaptureMode.MANUAL_SWIPE
            } else {
                _screenState.value.captureMode
            }

            val selectedLocation = SelectedLocation.ManualResult(
                selectedLatitude = point.latitude,
                selectedLongitude = point.longitude,
            )

            _currentFeature = updateSelectedGeometry(selectedLocation)

            updateScreenState(
                mapData = GetMapData(
                    _currentFeature,
                    _screenState.value.locationItems,
                    captureMode,
                ),
                selectedLocation = selectedLocation,
                captureMode = captureMode,
            )
        }
    }

    fun onMoveEnd() {
        if (_screenState.value.captureMode.isSwipe()) {
            updateScreenState(
                mapData = GetMapData(
                    _currentFeature,
                    screenState.value.locationItems,
                    CaptureMode.MANUAL,
                ),
                captureMode = CaptureMode.MANUAL,
            )
        }
    }

    private fun canCaptureWithSwipe() = _screenState.value.isManualCaptureEnabled &&
        _screenState.value.selectedLocation !is SelectedLocation.None

    fun canCaptureManually(): Boolean {
        return _screenState.value.isManualCaptureEnabled
    }

    fun updateLocationState(locationState: LocationState) {
        if (locationState == FIXED && _lastGPSLocation != null) {
            _currentFeature = updateSelectedGeometry(_lastGPSLocation!!)
            updateScreenState(
                mapData = GetMapData(
                    _currentFeature,
                    _screenState.value.locationItems,
                    CaptureMode.GPS,
                ),
                selectedLocation = _lastGPSLocation ?: _screenState.value.selectedLocation,
                captureMode = CaptureMode.GPS,
                locationState = locationState,
            )
        } else {
            updateScreenState(
                captureMode = if (locationState == FIXED) CaptureMode.GPS else _screenState.value.captureMode,
                locationState = locationState,
            )
        }
    }
}
