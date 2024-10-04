package org.dhis2.maps.usecases

import android.location.Geocoder
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.geometry.LatLngBounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.maps.api.GeocoderApi
import org.dhis2.maps.model.AccuracyRange
import org.dhis2.maps.views.MapSelectorViewModel
import org.dhis2.maps.views.SelectedLocation
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.map.layer.MapLayer
import org.hisp.dhis.android.core.map.layer.MapLayerPosition
import org.hisp.dhis.android.core.settings.SystemSetting
import org.hisp.dhis.mobile.ui.designsystem.component.model.LocationItemModel
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class MapSelectorViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()
    private val testingDispatcher = StandardTestDispatcher()

    private val d2: D2 = mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val androidGeocoder: Geocoder = mock()
    private val geocoderApi: GeocoderApi = mock()

    private val mapStyleConfiguration = MapStyleConfiguration(d2)

    private val geocoder = GeocoderSearchImpl(
        geocoder = androidGeocoder,
        geocoderApi = geocoderApi,
    )

    private val searchLocationManager = SearchLocationManager(d2)

    private lateinit var mapSelectorViewModel: MapSelectorViewModel

    private lateinit var mapSelectorViewModelNoInitialGeometry: MapSelectorViewModel

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
        whenever(
            d2.dataStoreModule().localDataStore().value(STORED_LOCATION).blockingGet(),
        ) doReturn null

        mapSelectorViewModel = MapSelectorViewModel(
            featureType = FeatureType.POINT,
            initialCoordinates = "[1.0,-1.0]",
            mapStyleConfig = mapStyleConfiguration,
            geocoder = geocoder,
            searchLocationManager = searchLocationManager,
            dispatchers = object : DispatcherProvider {
                override fun io() = testingDispatcher

                override fun computation() = testingDispatcher

                override fun ui() = testingDispatcher
            },
        )

        mapSelectorViewModelNoInitialGeometry = MapSelectorViewModel(
            featureType = FeatureType.POINT,
            initialCoordinates = null,
            mapStyleConfig = mapStyleConfiguration,
            geocoder = geocoder,
            searchLocationManager = searchLocationManager,
            dispatchers = object : DispatcherProvider {
                override fun io() = testingDispatcher

                override fun computation() = testingDispatcher

                override fun ui() = testingDispatcher
            },
        )
    }

    @Test
    fun shouldInit() = runTest {
        mapSelectorViewModel.screenState.test {
            val initialItem = awaitItem()
            assertTrue(initialItem.mapData.featureCollection.features()?.isEmpty() == true)
            val item = awaitItem()
            assertTrue(item.mapData.featureCollection.features()?.isNotEmpty() == true)
        }
    }

    @Test
    fun shouldFetchBaseMaps() = runTest {
        whenever(
            d2.settingModule().systemSetting().defaultBaseMap().blockingGet(),
        ) doReturn SystemSetting.builder().value("defaultBaseMapUid").build()
        whenever(
            d2.mapsModule().mapLayers().withImageryProviders().blockingGet(),
        ) doReturn listOf(
            MapLayer.builder()
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
    fun shouldSaveSelectedLocation() = runTest {
        mapSelectorViewModel.onPinClicked(
            Feature.fromGeometry(
                Point.fromLngLat(
                    mockedSearchResult.longitude,
                    mockedSearchResult.latitude,
                ),
            ).also {
                it.addStringProperty("title", mockedSearchResult.title)
                it.addStringProperty("subtitle", mockedSearchResult.address)
            },
        )
        mapSelectorViewModel.onSaveCurrentGeometry { coordinates ->
            assertTrue(coordinates == "[2.0,1.0]")
        }
    }

    @Test
    fun shouldSetNewLocationFromGps() = runTest {
        mapSelectorViewModelNoInitialGeometry.screenState.test {
            val initialItem = awaitItem()
            assertTrue(initialItem.accuracyRange is AccuracyRange.None)
            mapSelectorViewModelNoInitialGeometry.onNewLocation(mockedGpsResult)
            val item = awaitItem()
            assertTrue(item.accuracyRange == AccuracyRange.Good(10))
        }
    }

    @Test
    fun shouldSearchLocation() = runTest {
        whenever(
            d2.dataStoreModule().localDataStore().value(STORED_LOCATION).blockingGet(),
        ) doReturn null
        whenever(
            geocoderApi.searchFor(
                "Address",
                maxResults = 10,
            ),
        ) doReturn mockedLocationItemSearchResults

        mapSelectorViewModel.screenState.test {
            val item = awaitItem()
            assertTrue(item.locationItems.isEmpty())
            mapSelectorViewModel.setCaptureMode(MapSelectorViewModel.CaptureMode.SEARCH)
            awaitItem()
            mapSelectorViewModel.onSearchLocation("Address")
            val item2 = awaitItem()
            assertTrue(item2.locationItems == mockedLocationItemSearchResults)
        }
    }

    @Test
    fun shouldSetSelectedLocation() = runTest {
        whenever(
            d2.dataStoreModule().localDataStore().value(STORED_LOCATION).blockingGet(),
        ) doReturn null
        mapSelectorViewModel.onLocationSelected(mockedLocationItemSearchResults.first())
        testScheduler.advanceUntilIdle()
        verify(d2.dataStoreModule().localDataStore().value(STORED_LOCATION)).blockingSet(any())
    }

    @Test
    fun shouldClearSelectedLocation() = runTest {
        mapSelectorViewModel.screenState.test {
            awaitItem()
            mapSelectorViewModel.setCaptureMode(MapSelectorViewModel.CaptureMode.NONE)
            val result = awaitItem()
            assertTrue(result.selectedLocation is SelectedLocation.ManualResult)
        }
    }

    @Test
    fun shouldClearSearch() = runTest {
        mapSelectorViewModel.screenState.test {
            awaitItem()
            mapSelectorViewModel.onClearSearchClicked()
            val item2 = awaitItem()
            assertTrue(item2.captureMode == MapSelectorViewModel.CaptureMode.NONE)
            assertTrue(item2.selectedLocation is SelectedLocation.ManualResult)
        }

        mapSelectorViewModelNoInitialGeometry.screenState.test {
            awaitItem()
            mapSelectorViewModelNoInitialGeometry.onClearSearchClicked()
            val item2 = awaitItem()
            assertTrue(item2.captureMode == MapSelectorViewModel.CaptureMode.NONE)
            assertTrue(item2.selectedLocation is SelectedLocation.None)
        }
    }

    @Test
    fun shouldSetValueFromMapClick() = runTest {
        mapSelectorViewModel.screenState.test {
            awaitItem()
            mapSelectorViewModel.onMapClicked(mockedGpsResult.asLatLng())
            val item = awaitItem()
            assertTrue(item.captureMode == MapSelectorViewModel.CaptureMode.MANUAL)
            assertTrue(item.selectedLocation is SelectedLocation.ManualResult)
        }
    }

    @Test
    fun shouldNotDisplayPolygonInfo() {
        assertTrue(!mapSelectorViewModel.screenState.value.displayPolygonInfo)
    }

    @Test
    fun shouldDisplaySearchOnThisArea() = runTest {
        whenever(
            geocoderApi.searchFor(
                "Hospital",
                maxResults = 10,
            ),
        ) doReturn mockedLocationItemSearchResults

        mapSelectorViewModel.screenState.test {
            mapSelectorViewModel.setCaptureMode(MapSelectorViewModel.CaptureMode.SEARCH)
            mapSelectorViewModel.onSearchLocation("Hospital")
            awaitItem()
            mapSelectorViewModel.updateCurrentVisibleRegion(LatLngBounds.world())
            assertTrue(awaitItem().searchOnAreaVisible)
        }
    }

    private val mockedLocationItemSearchResults = listOf(
        LocationItemModel.SearchResult(
            searchedTitle = "Search title",
            searchedSubtitle = "Search subtitle",
            searchedLatitude = 1.0,
            searchedLongitude = 1.0,
        ),
    )

    private val mockedSearchResult = SelectedLocation.SearchResult(
        title = "Title",
        address = "Address",
        resultLatitude = 1.0,
        resultLongitude = 2.0,
    )

    private val mockedGpsResult = SelectedLocation.GPSResult(
        selectedLatitude = 1.0,
        selectedLongitude = 2.0,
        accuracy = 10f,
    )
}
