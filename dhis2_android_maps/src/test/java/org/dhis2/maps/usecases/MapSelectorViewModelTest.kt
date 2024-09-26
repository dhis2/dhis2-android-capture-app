package org.dhis2.maps.usecases

import android.location.Geocoder
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onStart
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

    private val mapSelectorViewModel = MapSelectorViewModel(
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

    private val mapSelectorViewModelNoInitialGeometry = MapSelectorViewModel(
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

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
        whenever(
            d2.dataStoreModule().localDataStore().value(STORED_LOCATION).blockingGet(),
        ) doReturn null
    }

    @Test
    fun shouldInit() = runTest {
        mapSelectorViewModel.init()
        assertTrue(mapSelectorViewModel.featureCollection.firstOrNull() != null)
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
    fun shouldUpdateSelectedLocation() = runTest {
        mapSelectorViewModel.updateSelectedGeometry(mockedSearchResult)
        assertTrue(mapSelectorViewModel.featureCollection.firstOrNull() != null)
        assertTrue(mapSelectorViewModel.selectedLocation.firstOrNull() == mockedSearchResult)
    }

    @Test
    fun shouldSaveSelectedLocation() = runTest {
        mapSelectorViewModel.updateSelectedGeometry(mockedSearchResult)
        mapSelectorViewModel.onSaveCurrentGeometry { coordinates ->
            assertTrue(coordinates == "[2.0,1.0]")
        }
    }

    @Test
    fun shouldSetNewLocationFromGps() = runTest {
        mapSelectorViewModelNoInitialGeometry.accuracyRange.test {
            mapSelectorViewModelNoInitialGeometry.init()
            mapSelectorViewModelNoInitialGeometry.onNewLocation(mockedGpsResult)
            val item = awaitItem()
            assertTrue(item == AccuracyRange.Good(10))
        }
    }

    @Test
    fun shouldSearchLocation() = runTest {
        whenever(
            d2.dataStoreModule().localDataStore().value(STORED_LOCATION).blockingGet(),
        ) doReturn null
        whenever(geocoderApi.searchFor("Address", maxResults = 10)) doReturn mockedLocationItemSearchResults

        mapSelectorViewModel.locationItems.test {
            mapSelectorViewModel.init()
            val item = awaitItem()
            assertTrue(item.isEmpty())
            mapSelectorViewModel.onSearchLocation("Address")
            val item2 = awaitItem()
            assertTrue(item2 == mockedLocationItemSearchResults)
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
        mapSelectorViewModel.onClearSelectedLocation()
        assertTrue(mapSelectorViewModel.selectedLocation.firstOrNull() is SelectedLocation.None)
    }

    @Test
    fun shouldDisplayAccuracyIndicator() = runTest {
        assertTrue(mapSelectorViewModel.shouldDisplayAccuracyIndicator(SelectedLocation.None()))
    }

    @Test
    fun shouldDisplayManualResult() = runTest {
        assertTrue(
            mapSelectorViewModel.shouldDisplayManualResult(
                SelectedLocation.ManualResult(0.0, 0.0),
            ),
        )
    }

    @Test
    fun shouldDisplaySearchResult() = runTest {
        assertTrue(
            mapSelectorViewModel.shouldDisplaySearchResult(
                SelectedLocation.SearchResult(
                    "",
                    "",
                    0.0,
                    0.0,
                ),
            ),
        )
    }

    @Test
    fun combineTest() = runTest {
        val flow1 = MutableStateFlow(1)
        val flow2 = MutableSharedFlow<String>(1)
        val flow3 = MutableStateFlow("A")
        val combinedFlow = combine(
            flow1.debounce(1000),
            flow2.onStart {
                emit("Q")
            },
            flow3,
        ) { a, b, c ->
            "$a $b $c"
        }

        combinedFlow.test {
            assertTrue(awaitItem() == "1 Q A")
            flow1.emit(2)
            assertTrue(awaitItem() == "2 Q A")
            flow2.emit("W")
            assertTrue(awaitItem() == "2 W A")
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
