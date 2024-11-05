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
import org.dhis2.maps.layer.types.FEATURE_PROPERTY_PLACES
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
    } else if (initialGeometry is Polygon) {
        SelectedLocation.Polygon(
            lastPolygonLatitude = initialGeometry.coordinates().first().last().latitude(),
            lastPolygonLongitude = initialGeometry.coordinates().first().last().longitude(),
        )
    } else {
        SelectedLocation.None()
    }

    private var _currentFeature: Feature? = initialGeometry?.let { Feature.fromGeometry(it) }
    private var _currentVisibleRegion: AvailableLatLngBounds? = null
    private var searchRegion: AvailableLatLngBounds? = null
    private val _searchLocationQuery = MutableStateFlow("")

    private val _geometryCoordinateResultChannel = Channel<GeometryCoordinate?>()
    val geometryCoordinateResultChannel = _geometryCoordinateResultChannel.receiveAsFlow()

    private val _screenState = MutableStateFlow(
        MapSelectorScreenState(
            mapData = GetMapData(_currentFeature, emptyList(), initialCaptureMode()),
            locationItems = emptyList(),
            selectedLocation = initialSelectedLocation,
            captureMode = initialCaptureMode(),
            accuracyRange = AccuracyRange.None(),
            searchOnAreaVisible = false,
            displayPolygonInfo = featureType == FeatureType.POLYGON,
            locationState = NOT_FIXED,
            isManualCaptureEnabled = mapStyleConfig.isManualCaptureEnabled(),
            forcedLocationAccuracy = mapStyleConfig.getForcedLocationAccuracy(),
            lastGPSLocation = null,
        ),
    )

    val screenState = _screenState.asStateFlow()

    init {
        registerSearchListener()
    }

    private fun initialCaptureMode(): CaptureMode {
        return when {
            featureType == FeatureType.POINT && initialCoordinates == null -> CaptureMode.GPS
            else -> CaptureMode.NONE
        }
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
        lastGPSLocation: SelectedLocation.GPSResult? = _screenState.value.lastGPSLocation,
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
                lastGPSLocation = lastGPSLocation,
            )
        }
    }

    private fun updateSelectedGeometry(point: SelectedLocation) =
        if (point !is SelectedLocation.None) {
            val newPoint = Point.fromLngLat(point.longitude, point.latitude)
            when (featureType) {
                FeatureType.POINT -> Feature.fromGeometry(newPoint)

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
            updateScreenState(
                lastGPSLocation = gpsResult,
            )
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
            val newGeometry = _currentFeature?.let { feature ->
                val geometry = (feature.geometry() as Polygon)
                val data = geometry.coordinates().first().toMutableList()
                data.add(Point.fromLngLat(polygonPoint[0], polygonPoint[1]))
                Polygon.fromLngLats(listOf(data))
            } ?: Polygon.fromLngLats(
                listOf(listOf(Point.fromLngLat(polygonPoint[0], polygonPoint[1]))),
            )
            _currentFeature = Feature.fromGeometry(newGeometry)
            val selectedLocation = SelectedLocation.Polygon(
                lastPolygonLatitude = polygonPoint[0],
                lastPolygonLongitude = polygonPoint[1],
            )

            updateScreenState(
                mapData = GetMapData(
                    _currentFeature,
                    _screenState.value.locationItems,
                    _screenState.value.captureMode,
                ),
                selectedLocation = selectedLocation,
            )
        }
    }

    fun removePointFromPolygon(index: Int) {
        if (featureType == FeatureType.POLYGON) {
            viewModelScope.launch(dispatchers.io()) {
                val newGeometry = _currentFeature?.let { feature ->
                    val geometry = (feature.geometry() as Polygon)
                    geometry.coordinates().first()
                        .removeAt(index)
                    if (geometry.coordinates().first().isEmpty()) null else geometry
                }
                _currentFeature = newGeometry?.let { Feature.fromGeometry(newGeometry) }
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
            val locationItems = searchItems + filteredPreviousLocation
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
            if (featureType == FeatureType.POLYGON) {
                // not implemented
            } else if (feature.hasProperty(FEATURE_PROPERTY_PLACES)) {
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
    }

    fun onPointClicked(latLng: LatLng) {
        if (featureType == FeatureType.POLYGON) {
            addPointToPolygon(listOf(latLng.longitude, latLng.latitude))
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

    fun updateCurrentVisibleRegion(mapBounds: AvailableLatLngBounds?) {
        _currentVisibleRegion = mapBounds
        updateScreenState(searchOnAreaVisible = _searchLocationQuery.value.isNotBlank())
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
            _currentFeature = _screenState.value.lastGPSLocation?.let { updateSelectedGeometry(it) }
            updateScreenState(
                mapData = GetMapData(
                    _currentFeature,
                    _screenState.value.locationItems,
                    CaptureMode.GPS,
                ),
                selectedLocation = _screenState.value.lastGPSLocation ?: SelectedLocation.None(),
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

    private fun canCaptureWithSwipe() = featureType == FeatureType.POINT &&
        _screenState.value.isManualCaptureEnabled &&
        _screenState.value.selectedLocation !is SelectedLocation.None

    fun canCaptureManually(): Boolean {
        return _screenState.value.isManualCaptureEnabled
    }

    fun updateLocationState(locationState: LocationState) {
        when {
            _screenState.value.displayPolygonInfo -> {
                // Location updates not available in polygons
            }

            locationState == FIXED && _screenState.value.lastGPSLocation != null -> {
                _currentFeature = updateSelectedGeometry(_screenState.value.lastGPSLocation!!)
                updateScreenState(
                    mapData = GetMapData(
                        _currentFeature,
                        _screenState.value.locationItems,
                        CaptureMode.GPS,
                    ),
                    selectedLocation = _screenState.value.lastGPSLocation!!, // Use !! since it's not null
                    captureMode = CaptureMode.GPS,
                    locationState = locationState,
                )
            }

            else -> {
                updateScreenState(
                    captureMode = if (locationState == FIXED) CaptureMode.GPS else _screenState.value.captureMode,
                    locationState = locationState,
                )
            }
        }
    }
}
