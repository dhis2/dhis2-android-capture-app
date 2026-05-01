package org.dhis2.maps.usecases

import org.dhis2.commons.data.ProgramConfigurationRepository
import org.dhis2.maps.model.MapScope
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.filters.internal.EnumFilterConnector
import org.hisp.dhis.android.core.arch.repositories.filters.internal.StringFilterConnector
import org.hisp.dhis.android.core.map.layer.MapLayer
import org.hisp.dhis.android.core.map.layer.MapLayerCollectionRepository
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
    private val d2: D2 = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)
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

    private val stringFilterConnector: StringFilterConnector<MapLayerCollectionRepository> = mock()
    private val enumFilterConnector: EnumFilterConnector<MapLayerCollectionRepository, MapLayerPosition> =
        mock()

    @Test
    fun shouldFetchMapStyles() {
        mockBasemaps(
            d2,
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
            ),
        )

        mockOverlays(d2, emptyList())

        mapStyleConfiguration.fetchMapStyles().let { result ->
            assertTrue(result.size == 2)
            assertTrue(
                result[0]
                    .sources.entries
                    .first()
                    .value.tiles.size == 2,
            )
            assertTrue(
                result[0]
                    .sources.entries
                    .first()
                    .value.tiles[0] == "http://a.test.test/x/y/z",
            )
            assertTrue(result[0].attribution == "© Maplibre")
            assertTrue(
                result[0]
                    .sources.entries
                    .first()
                    .value.tiles[1] == "http://b.test.test/x/y/z",
            )
            assertTrue(
                result[1]
                    .sources.entries
                    .first()
                    .value.tiles.size == 1,
            )
            assertTrue(result[1].attribution == "© Maplibre, © Carto")
        }
    }

    @Test
    fun shouldFetchCustomMaps() {
        mockBasemaps(
            d2,
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
            ),
        )

        mockOverlays(d2, emptyList())

        mapStyleConfiguration.fetchMapStyles().let { result ->
            assertTrue(result.size == 2)
            assertTrue(
                result[0]
                    .sources.entries
                    .first()
                    .value.tiles.size == 4,
            )
            assertTrue(
                result[0]
                    .sources.entries
                    .first()
                    .value.tiles[0] == "http://a.test.test/x/y/z",
            )
            assertTrue(result[0].attribution == "© Maplibre")
            assertTrue(
                result[0]
                    .sources.entries
                    .first()
                    .value.tiles[1] == "http://b.test.test/x/y/z",
            )
            assertTrue(
                result[1]
                    .sources.entries
                    .first()
                    .value.tiles.size == 4,
            )
            assertTrue(result[1].attribution == "© Maplibre, © Carto")
        }
    }

    @Test
    fun shouldFetchBasemapWithOverlay() {
        mockBasemaps(
            d2,
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
            ),
        )

        mockOverlays(
            d2,
            listOf(
                mockMapLayer(
                    displayName = "overlay 1",
                    imageUrl = "http://{s}.overlay.test/x/y/z",
                    subdomains = null,
                    subDomainPlaceHolder = null,
                    imaginaryProviders =
                        listOf(
                            mockImaginaryProvider("© Overlay"),
                        ),
                ),
            ),
        )

        mapStyleConfiguration.fetchMapStyles().let { result ->
            assertTrue(result[0].sources.size == 2)
            assertTrue(
                result[0]
                    .sources.keys
                    .toList()[1] == "overlay 1",
            )
            assertTrue(result[0].sources["overlay 1"]?.tiles[0] == "http://a.overlay.test/x/y/z")
            assertEquals("© Maplibre, © Overlay", result[0].attribution)
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
