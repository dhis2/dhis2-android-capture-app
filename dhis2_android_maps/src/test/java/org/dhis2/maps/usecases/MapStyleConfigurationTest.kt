package org.dhis2.maps.usecases

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.map.layer.MapLayer
import org.hisp.dhis.android.core.map.layer.MapLayerImageryProvider
import org.hisp.dhis.android.core.map.layer.MapLayerPosition
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

class MapStyleConfigurationTest {
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val mapStyleConfiguration = MapStyleConfiguration(d2)

    @Test
    fun shouldFetchMapStyles() {
        whenever(d2.mapsModule().mapLayers()) doReturn mock()
        whenever(d2.mapsModule().mapLayers().withImageryProviders()) doReturn mock()
        whenever(d2.mapsModule().mapLayers().withImageryProviders().blockingGet()) doReturn listOf(
            mockMapLayer(
                displayName = "basemap 1",
                imageUrl = "http://{s}.test.test/x/y/z",
                subDomainPlaceHolder = "{s}",
                subdomains = listOf("a", "b"),
                imaginaryProviders = listOf(
                    mockImaginaryProvider("© Maplibre")
                )
            ),
            mockMapLayer(
                displayName = "basemap 2",
                imageUrl = "http://test.test/x/y/z",
                subDomainPlaceHolder = null,
                subdomains = null,
                imaginaryProviders = listOf(
                    mockImaginaryProvider("© Maplibre"),
                    mockImaginaryProvider("© Carto")
                )
            )
        )

        mapStyleConfiguration.fetchMapStyles().let { result ->
            assertTrue(result.size == 2)
            assertTrue(result[0].sources.rasterTiles.tiles.size == 2)
            assertTrue(result[0].sources.rasterTiles.tiles[0] == "http://a.test.test/x/y/z")
            assertTrue(result[0].sources.attribution == "© Maplibre")
            assertTrue(result[0].sources.rasterTiles.tiles[1] == "http://b.test.test/x/y/z")
            assertTrue(result[1].sources.rasterTiles.tiles.size == 1)
            assertTrue(result[1].sources.attribution == "© Maplibre, © Carto")
        }
    }

    private fun mockMapLayer(
        displayName: String,
        imageUrl: String,
        subDomainPlaceHolder: String?,
        subdomains: List<String>?,
        imaginaryProviders: List<MapLayerImageryProvider>
    ) = MapLayer.builder()
        .displayName(displayName)
        .imageUrl(imageUrl)
        .subdomainPlaceholder(subDomainPlaceHolder)
        .subdomains(subdomains)
        .imageryProviders(imaginaryProviders)
        .mapLayerPosition(MapLayerPosition.BASEMAP)
        .uid(displayName)
        .name(displayName)
        .external(false)
        .build()

    private fun mockImaginaryProvider(attribution: String) = MapLayerImageryProvider.builder()
        .attribution(attribution)
        .mapLayer("mapLayerId")
        .build()
}
