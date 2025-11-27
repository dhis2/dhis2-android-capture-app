package org.dhis2.maps.usecases

import org.dhis2.commons.data.ProgramConfigurationRepository
import org.dhis2.maps.model.MapScope
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.map.layer.MapLayer
import org.hisp.dhis.android.core.map.layer.MapLayerImageryProvider
import org.hisp.dhis.android.core.map.layer.MapLayerPosition
import org.hisp.dhis.android.core.settings.DataSetConfigurationSetting
import org.hisp.dhis.android.core.settings.ProgramConfigurationSetting
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MapStyleConfigurationTest {
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val uid = "uid"
    private val programConfigurationSetting: ProgramConfigurationSetting = mock()
    private val dataSetConfigurationSetting: DataSetConfigurationSetting = mock()
    private val programConfigurationRepository: ProgramConfigurationRepository =
        mock {
            on { getConfigurationByProgram(uid) } doReturn programConfigurationSetting
            on { getConfigurationByDataSet(uid) } doReturn dataSetConfigurationSetting
        }
    private val mapStyleConfiguration =
        MapStyleConfiguration(
            d2,
            uid,
            MapScope.PROGRAM,
            programConfigurationRepository,
        )

    @Test
    fun shouldFetchMapStyles() {
        whenever(d2.mapsModule().mapLayers()) doReturn mock()
        whenever(d2.mapsModule().mapLayers().withImageryProviders()) doReturn mock()
        whenever(
            d2
                .mapsModule()
                .mapLayers()
                .withImageryProviders()
                .blockingGet(),
        ) doReturn
            listOf(
                mockMapLayer(
                    displayName = "basemap 1",
                    imageUrl = "http://{s}.test.test/x/y/z",
                    subDomainPlaceHolder = "{s}",
                    subdomains = listOf("a", "b"),
                    imaginaryProviders =
                        listOf(
                            mockImaginaryProvider("© Maplibre"),
                        ),
                ),
                mockMapLayer(
                    displayName = "basemap 2",
                    imageUrl = "http://test.test/x/y/z",
                    subDomainPlaceHolder = null,
                    subdomains = null,
                    imaginaryProviders =
                        listOf(
                            mockImaginaryProvider("© Maplibre"),
                            mockImaginaryProvider("© Carto"),
                        ),
                ),
            )

        mapStyleConfiguration.fetchMapStyles().let { result ->
            assertTrue(result.size == 2)
            assertTrue(
                result[0]
                    .sources.rasterTiles.tiles.size == 2,
            )
            assertTrue(result[0].sources.rasterTiles.tiles[0] == "http://a.test.test/x/y/z")
            assertTrue(result[0].sources.attribution == "© Maplibre")
            assertTrue(result[0].sources.rasterTiles.tiles[1] == "http://b.test.test/x/y/z")
            assertTrue(
                result[1]
                    .sources.rasterTiles.tiles.size == 1,
            )
            assertTrue(result[1].sources.attribution == "© Maplibre, © Carto")
        }
    }

    @Test
    fun shouldFetchCustomMaps() {
        whenever(d2.mapsModule().mapLayers()) doReturn mock()
        whenever(d2.mapsModule().mapLayers().withImageryProviders()) doReturn mock()
        whenever(
            d2
                .mapsModule()
                .mapLayers()
                .withImageryProviders()
                .blockingGet(),
        ) doReturn
            listOf(
                mockMapLayer(
                    displayName = "basemap 1",
                    imageUrl = "http://{s}.test.test/x/y/z",
                    subDomainPlaceHolder = null,
                    subdomains = null,
                    imaginaryProviders =
                        listOf(
                            mockImaginaryProvider("© Maplibre"),
                        ),
                ),
                mockMapLayer(
                    displayName = "basemap 2",
                    imageUrl = "http://test.test.{subdomain}/x/y/z",
                    subDomainPlaceHolder = null,
                    subdomains = null,
                    imaginaryProviders =
                        listOf(
                            mockImaginaryProvider("© Maplibre"),
                            mockImaginaryProvider("© Carto"),
                        ),
                ),
            )

        mapStyleConfiguration.fetchMapStyles().let { result ->
            assertTrue(result.size == 2)
            assertTrue(
                result[0]
                    .sources.rasterTiles.tiles.size == 4,
            )
            assertTrue(result[0].sources.rasterTiles.tiles[0] == "http://a.test.test/x/y/z")
            assertTrue(result[0].sources.attribution == "© Maplibre")
            assertTrue(result[0].sources.rasterTiles.tiles[1] == "http://b.test.test/x/y/z")
            assertTrue(
                result[1]
                    .sources.rasterTiles.tiles.size == 4,
            )
            assertTrue(result[1].sources.attribution == "© Maplibre, © Carto")
        }
    }

    @Test
    fun shouldCaptureManuallyForProgram() {
        whenever(programConfigurationSetting.disableManualLocation()) doReturn false

        val mapConfiguration =
            MapStyleConfiguration(
                d2,
                uid,
                MapScope.PROGRAM,
                programConfigurationRepository,
            )

        assertTrue(mapConfiguration.isManualCaptureEnabled())
    }

    @Test
    fun shouldNotCaptureManuallyForProgram() {
        whenever(programConfigurationSetting.disableManualLocation()) doReturn true

        val mapConfiguration =
            MapStyleConfiguration(
                d2,
                uid,
                MapScope.PROGRAM,
                programConfigurationRepository,
            )

        assertFalse(mapConfiguration.isManualCaptureEnabled())
    }

    @Test
    fun shouldForceLocationPrecisionForProgram() {
        whenever(programConfigurationSetting.minimumLocationAccuracy()) doReturn 100

        val mapConfiguration =
            MapStyleConfiguration(
                d2,
                uid,
                MapScope.PROGRAM,
                programConfigurationRepository,
            )

        assertEquals(100, mapConfiguration.getForcedLocationAccuracy())
    }

    @Test
    fun shouldCaptureManuallyForDataSet() {
        whenever(dataSetConfigurationSetting.disableManualLocation()) doReturn false

        val mapConfiguration =
            MapStyleConfiguration(
                d2,
                uid,
                MapScope.DATA_SET,
                programConfigurationRepository,
            )

        assertTrue(mapConfiguration.isManualCaptureEnabled())
    }

    @Test
    fun shouldNotCaptureManuallyForDataSet() {
        whenever(dataSetConfigurationSetting.disableManualLocation()) doReturn true

        val mapConfiguration =
            MapStyleConfiguration(
                d2,
                uid,
                MapScope.DATA_SET,
                programConfigurationRepository,
            )

        assertFalse(mapConfiguration.isManualCaptureEnabled())
    }

    @Test
    fun shouldForceLocationPrecisionForDataSet() {
        whenever(dataSetConfigurationSetting.minimumLocationAccuracy()) doReturn 100

        val mapConfiguration =
            MapStyleConfiguration(
                d2,
                uid,
                MapScope.DATA_SET,
                programConfigurationRepository,
            )

        assertEquals(100, mapConfiguration.getForcedLocationAccuracy())
    }

    private fun mockMapLayer(
        displayName: String,
        imageUrl: String,
        subDomainPlaceHolder: String?,
        subdomains: List<String>?,
        imaginaryProviders: List<MapLayerImageryProvider>,
    ) = MapLayer
        .builder()
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

    private fun mockImaginaryProvider(attribution: String) =
        MapLayerImageryProvider
            .builder()
            .attribution(attribution)
            .mapLayer("mapLayerId")
            .build()
}
