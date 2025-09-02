package org.dhis2.maps.usecases

import android.location.Geocoder
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.data.ProgramConfigurationRepository
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.maps.api.GeocoderApi
import org.dhis2.maps.extensions.withPlacesProperties
import org.dhis2.maps.model.AccuracyRange
import org.dhis2.maps.model.MapScope
import org.dhis2.maps.model.MapSelectorScreenState
import org.dhis2.maps.utils.AvailableLatLngBounds
import org.dhis2.maps.views.MapSelectorViewModel
import org.dhis2.maps.views.SelectedLocation
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.map.layer.MapLayer
import org.hisp.dhis.android.core.map.layer.MapLayerPosition
import org.hisp.dhis.android.core.settings.SystemSetting
import org.hisp.dhis.mobile.ui.designsystem.component.model.LocationItemModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.geojson.Feature
import org.maplibre.geojson.Point
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

typealias ExpectedItems = Int

class MapSelectorViewModelTest {
    @get:Rule
    val rule = InstantTaskExecutorRule()
    private val testingDispatcher = StandardTestDispatcher()

    private val d2: D2 = mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val androidGeocoder: Geocoder = mock()
    private val geocoderApi: GeocoderApi = mock()

    private val programUid = null
    private val programConfigurationRepository: ProgramConfigurationRepository = mock()

    private val mapStyleConfiguration =
        MapStyleConfiguration(d2, programUid, MapScope.PROGRAM, programConfigurationRepository)

    private val dispatcherProvider =
        object : DispatcherProvider {
            override fun io() = testingDispatcher

            override fun computation() = testingDispatcher

            override fun ui() = testingDispatcher
        }

    private val geocoder =
        GeocoderSearchImpl(
            geocoder = androidGeocoder,
            geocoderApi = geocoderApi,
            dispatcherProvider = dispatcherProvider,
        )

    private val searchLocationManager = SearchLocationManager(d2)

    private lateinit var mapSelectorViewModel: MapSelectorViewModel

    private lateinit var mapSelectorViewModelNoInitialGeometry: MapSelectorViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
        whenever(
            d2
                .dataStoreModule()
                .localDataStore()
                .value(STORED_LOCATION)
                .blockingGet(),
        ) doReturn null

        mapSelectorViewModel =
            MapSelectorViewModel(
                featureType = FeatureType.POINT,
                initialCoordinates = "[1.0,-1.0]",
                mapStyleConfig = mapStyleConfiguration,
                geocoder = geocoder,
                searchLocationManager = searchLocationManager,
                dispatchers = dispatcherProvider,
            )

        mapSelectorViewModelNoInitialGeometry =
            MapSelectorViewModel(
                featureType = FeatureType.POINT,
                initialCoordinates = null,
                mapStyleConfig = mapStyleConfiguration,
                geocoder = geocoder,
                searchLocationManager = searchLocationManager,
                dispatchers = dispatcherProvider,
            )
    }

    @Test
    fun shouldInit() =
        runTest {
            mapSelectorViewModel.screenState.initTest(
                given = {},
                then = { screenState ->
                    assertTrue(
                        screenState.mapData.featureCollection
                            .features()
                            ?.isNotEmpty() == true,
                    )
                },
            )
        }

    @Test
    fun shouldFetchBaseMaps() =
        runTest {
            whenever(
                d2
                    .settingModule()
                    .systemSetting()
                    .defaultBaseMap()
                    .blockingGet(),
            ) doReturn SystemSetting.builder().value("defaultBaseMapUid").build()
            whenever(
                d2
                    .mapsModule()
                    .mapLayers()
                    .withImageryProviders()
                    .blockingGet(),
            ) doReturn
                listOf(
                    MapLayer
                        .builder()
                        .imageUrl("https://fake.url/map")
                        .uid("mapLayerUid")
                        .displayName("Configured base map")
                        .subdomainPlaceholder("")
                        .subdomains(listOf())
                        .imageryProviders(emptyList())
                        .name("Configured base map")
                        .external(false)
                        .mapLayerPosition(MapLayerPosition.BASEMAP)
                        .build(),
                )
            val baseMaps = mapSelectorViewModel.fetchMapStyles()
            assertTrue(baseMaps.isNotEmpty())
        }

    @Test
    fun shouldSaveSelectedLocation() =
        runTest {
            mapSelectorViewModel.geometryCoordinateResultChannel.initTest(
                given = {
                    mapSelectorViewModel.onPinClicked(
                        Feature
                            .fromGeometry(
                                Point.fromLngLat(
                                    mockedSearchResult.longitude,
                                    mockedSearchResult.latitude,
                                ),
                            ).also {
                                it.withPlacesProperties(
                                    title = mockedSearchResult.title,
                                    subtitle = mockedSearchResult.address,
                                )
                            },
                    )
                },
                `when` = {
                    mapSelectorViewModel.onDoneClick()
                    1
                },
                then = { result ->
                    assertTrue(result == "[2.0,1.0]")
                },
            )
        }

    @Test
    fun shouldSetNewLocationFromGps() =
        runTest {
            mapSelectorViewModelNoInitialGeometry.screenState.test {
                // Given
                assertTrue(awaitItem().accuracyRange == AccuracyRange.None())

                // When
                mapSelectorViewModelNoInitialGeometry.onNewLocation(mockedGpsResult)
                with(awaitItem()) {
                    assertTrue(lastGPSLocation == mockedGpsResult)
                }

                // Then
                with(awaitItem()) {
                    assertTrue(accuracyRange == AccuracyRange.Good(10))
                    assertTrue(selectedLocation == mockedGpsResult)
                }
            }
        }

    @Test
    fun shouldNotSetAccuracyLessAccurate() =
        runTest {
            mapSelectorViewModelNoInitialGeometry.screenState.test {
                // Given
                assertTrue(awaitItem().accuracyRange == AccuracyRange.None())

                // When
                mapSelectorViewModelNoInitialGeometry.onNewLocation(mockedGpsResult)
                with(awaitItem()) {
                    assertTrue(lastGPSLocation == mockedGpsResult)
                }

                with(awaitItem()) {
                    assertTrue(accuracyRange == AccuracyRange.Good(10))
                    assertTrue(selectedLocation == mockedGpsResult)
                }
                mapSelectorViewModelNoInitialGeometry.onNewLocation(mockedGpsResultLessAccuracy)

                // Then
                expectNoEvents()
            }
        }

    @Test
    fun shouldSearchLocation() =
        runTest {
            whenever(
                d2
                    .dataStoreModule()
                    .localDataStore()
                    .value(STORED_LOCATION)
                    .blockingGet(),
            ) doReturn null
            whenever(
                geocoderApi.searchFor(
                    "Address",
                    null,
                    maxResults = 10,
                ),
            ) doReturn mockedLocationItemSearchResults

            mapSelectorViewModel.screenState.test {
                val state0 = awaitItem()
                assertTrue(state0.locationItems.isEmpty())
                mapSelectorViewModel.initSearchMode()
                val state1 = awaitItem()
                assertTrue(state1.captureMode == MapSelectorViewModel.CaptureMode.SEARCH)
                assertTrue(state1.locationItems.isEmpty())
                mapSelectorViewModel.onSearchLocation("Address")
                val searchingItem = awaitItem()
                assertTrue(searchingItem.searching)
                assertTrue(searchingItem.locationItems.isEmpty())
                val item2 = awaitItem()
                assertTrue(item2.locationItems == mockedLocationItemSearchResults)
                assertTrue(!item2.searchOnAreaVisible)
            }
        }

    @Test
    fun shouldSetSelectedLocation() =
        runTest {
            whenever(
                d2
                    .dataStoreModule()
                    .localDataStore()
                    .value(STORED_LOCATION)
                    .blockingGet(),
            ) doReturn null
            mapSelectorViewModel.onLocationSelected(mockedLocationItemSearchResults.first())
            testScheduler.advanceUntilIdle()
            verify(d2.dataStoreModule().localDataStore().value(STORED_LOCATION)).blockingSet(any())
        }

    @Test
    fun shouldClearSearchWithInitialCoordinates() =
        runTest {
            mapSelectorViewModel.screenState.initTest(
                given = { givenSearchAction {} },
                `when` = {
                    mapSelectorViewModel.onClearSearchClicked()
                    1
                },
                then = { screenState ->
                    assertEquals(MapSelectorViewModel.CaptureMode.NONE, screenState.captureMode)
                    assertTrue(screenState.selectedLocation is SelectedLocation.ManualResult)
                },
            )
        }

    @Test
    fun shouldClearSearchWithoutInitialCoordinates() =
        runTest {
            mapSelectorViewModelNoInitialGeometry.screenState.initTest(
                given = { givenSearchAction(mapSelectorViewModelNoInitialGeometry) {} },
                `when` = {
                    mapSelectorViewModelNoInitialGeometry.onClearSearchClicked()
                    1
                },
                then = { screenState ->
                    assertEquals(MapSelectorViewModel.CaptureMode.NONE, screenState.captureMode)
                    assertTrue(screenState.selectedLocation is SelectedLocation.None)
                },
            )
        }

    @Test
    fun shouldNotDisplayPolygonInfo() {
        assertTrue(!mapSelectorViewModel.screenState.value.displayPolygonInfo)
    }

    @Test
    fun shouldDisplaySearchOnThisArea() =
        runTest(testingDispatcher.scheduler) {
            mapSelectorViewModel.screenState.initTest(
                given = {
                    givenSearchAction {
                        assertEquals(false, it.searchOnAreaVisible)
                    }
                },
                `when` = {
                    mapSelectorViewModel.updateCurrentVisibleRegion(
                        AvailableLatLngBounds(
                            listOf(
                                LatLngBounds.world(),
                            ),
                        ),
                    )
                    1
                },
                then = { screenState ->
                    assertEquals(true, screenState.searchOnAreaVisible)
                },
            )
        }

    @Test
    fun shouldSearchInNewArea() =
        runTest {
            mapSelectorViewModel.screenState.initTest(
                given = {
                    givenSearchAction { }
                    mapSelectorViewModel.updateCurrentVisibleRegion(
                        AvailableLatLngBounds(
                            listOf(
                                LatLngBounds.world(),
                            ),
                        ),
                    )
                    awaitItem()
                },
                `when` = {
                    whenever(
                        geocoderApi.searchFor(
                            "Address",
                            AvailableLatLngBounds(
                                listOf(
                                    LatLngBounds.world(),
                                ),
                            ),
                            maxResults = 10,
                        ),
                    ) doReturn mockedOtherRegionLocationItemSearchResults

                    mapSelectorViewModel.onSearchOnAreaClick()
                    2
                },
                then = { screenState ->
                    assertEquals(false, screenState.searchOnAreaVisible)
                    assertEquals(mockedOtherRegionLocationItemSearchResults, screenState.locationItems)
                },
            )
        }

    @Test
    fun shouldSetGPSCaptureMode() =
        runTest {
            mapSelectorViewModel.screenState.initTest(
                given = {},
                `when` = {
                    mapSelectorViewModel.onMyLocationButtonClick()
                    1
                },
                then = {
                    assertEquals(MapSelectorViewModel.CaptureMode.GPS, it.captureMode)
                },
            )
        }

    private suspend fun ReceiveTurbine<MapSelectorScreenState>.givenSearchAction(
        viewModel: MapSelectorViewModel = mapSelectorViewModel,
        onLastState: (MapSelectorScreenState) -> Unit,
    ) {
        whenever(
            d2
                .dataStoreModule()
                .localDataStore()
                .value(STORED_LOCATION)
                .blockingGet(),
        ) doReturn null
        whenever(
            geocoderApi.searchFor(
                "Address",
                null,
                maxResults = 10,
            ),
        ) doReturn mockedLocationItemSearchResults

        viewModel.initSearchMode()
        awaitItem()
        viewModel.onSearchLocation("Address")
        assertTrue(awaitItem().searching)
        with(awaitItem()) {
            assertTrue(locationItems.isNotEmpty())
            assertTrue(captureMode == MapSelectorViewModel.CaptureMode.SEARCH)
            onLastState(this)
        }
    }

    private suspend fun <T> Flow<T>.initTest(
        given: suspend ReceiveTurbine<T>.() -> Unit,
        `when`: (suspend ReceiveTurbine<T>.() -> ExpectedItems)? = null,
        then: suspend ReceiveTurbine<T>.(finalState: T) -> Unit,
    ) = test {
        val initialItem =
            if (this@initTest is StateFlow) {
                awaitItem()
            } else {
                null
            }

        given()
        if (`when` != null) {
            val expectedItems = `when`()
            if (expectedItems > 0) {
                repeat(expectedItems - 1) {
                    awaitItem()
                }
                then(awaitItem())
            } else if (initialItem != null) {
                then(initialItem)
            }
        } else if (initialItem != null) {
            then(initialItem)
        }
    }

    @Test
    fun shouldUpdateGeometryWhenMapMoves() =
        runTest {
            with(mapSelectorViewModel) {
                screenState.test {
                    awaitItem()
                    onMove(LatLng(4.98075, 110.63242))
                    val item = awaitItem()
                    assertTrue(item.captureMode.isSwipe())
                    assertTrue(
                        item.mapData.featureCollection
                            .features()
                            ?.isEmpty() == true,
                    )
                    assertTrue(item.selectedLocation is SelectedLocation.ManualResult)
                    assertTrue(item.selectedLocation.latitude == 4.98075 && item.selectedLocation.longitude == 110.63242)
                }
            }
        }

    @Test
    fun shouldSwitchCaptureModeToManual() =
        runTest {
            with(mapSelectorViewModel) {
                onMove(LatLng(4.98075, 110.63242))
                assertTrue(screenState.value.captureMode.isSwipe())
                onMoveEnd()
                assertTrue(screenState.value.captureMode.isManual())
            }
        }

    @Test
    fun shouldAllowCapturingManually() =
        runTest {
            with(mapSelectorViewModel) {
                assertTrue(canCaptureManually())
            }
        }

    @Test
    fun shouldSwitchCaptureModeToSearchPinClickedWhenPinClicked() =
        runTest {
            mapSelectorViewModelNoInitialGeometry.screenState.initTest(
                given = { givenSearchAction(mapSelectorViewModelNoInitialGeometry) {} },
                `when` = {
                    mapSelectorViewModelNoInitialGeometry.onPinClicked(
                        Feature
                            .fromGeometry(
                                Point.fromLngLat(
                                    mockedSearchResult.longitude,
                                    mockedSearchResult.latitude,
                                ),
                            ).also {
                                it.withPlacesProperties(
                                    title = mockedSearchResult.title,
                                    subtitle = mockedSearchResult.address,
                                )
                            },
                    )
                    1
                },
                then = { screenState ->
                    assertTrue(screenState.captureMode.isSearchPinClicked())
                },
            )
        }

    @Test
    fun shouldSwitchCaptureModeToSearchManualWhenPinClickedFinishMove() =
        runTest {
            mapSelectorViewModelNoInitialGeometry.screenState.initTest(
                given = {
                    givenSearchAction(mapSelectorViewModelNoInitialGeometry) {}
                    mapSelectorViewModelNoInitialGeometry.onPinClicked(
                        Feature
                            .fromGeometry(
                                Point.fromLngLat(
                                    mockedSearchResult.longitude,
                                    mockedSearchResult.latitude,
                                ),
                            ).also {
                                it.withPlacesProperties(
                                    title = mockedSearchResult.title,
                                    subtitle = mockedSearchResult.address,
                                )
                            },
                    )
                    val item = awaitItem()
                    assertTrue(item.captureMode.isSearchPinClicked())
                },
                `when` = {
                    mapSelectorViewModelNoInitialGeometry.onMoveEnd()
                    1
                },
                then = { screenState ->
                    assertTrue(screenState.captureMode.isSearchManual())
                },
            )
        }

    private val mockedLocationItemSearchResults =
        listOf(
            LocationItemModel.SearchResult(
                searchedTitle = "Search title",
                searchedSubtitle = "Search subtitle",
                searchedLatitude = 1.0,
                searchedLongitude = 1.0,
            ),
        )

    private val mockedOtherRegionLocationItemSearchResults =
        listOf(
            LocationItemModel.SearchResult(
                searchedTitle = "Search title",
                searchedSubtitle = "Search subtitle",
                searchedLatitude = 3.0,
                searchedLongitude = 3.0,
            ),
        )

    private val mockedSearchResult =
        SelectedLocation.SearchResult(
            title = "Title",
            address = "Address",
            resultLatitude = 1.0,
            resultLongitude = 2.0,
        )

    private val mockedGpsResult =
        SelectedLocation.GPSResult(
            selectedLatitude = 1.0,
            selectedLongitude = 2.0,
            accuracy = 10f,
        )

    private val mockedGpsResultLessAccuracy =
        SelectedLocation.GPSResult(
            selectedLatitude = 1.0,
            selectedLongitude = 2.0,
            accuracy = 20f,
        )
}
