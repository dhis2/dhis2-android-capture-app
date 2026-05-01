package org.dhis2.maps.usecases

import org.dhis2.commons.data.ProgramConfigurationRepository
import org.dhis2.maps.layer.basemaps.BaseMapStyle
import org.dhis2.maps.layer.basemaps.BaseMapStyleBuilder.build
import org.dhis2.maps.layer.basemaps.Overlay
import org.dhis2.maps.model.MapScope
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.map.layer.MapLayerImageryProvider
import org.hisp.dhis.android.core.map.layer.MapLayerPosition

const val DEFAULT_FORCED_LOCATION_ACCURACY = -1

class MapStyleConfiguration(
    private val d2: D2,
    val uid: String? = null,
    val scope: MapScope,
    programConfigurationRepository: ProgramConfigurationRepository,
) {
    private val canCaptureManually =
        when (scope) {
            MapScope.PROGRAM ->
                programConfigurationRepository
                    .getConfigurationByProgram(uid ?: "")
                    ?.let { programConfiguration ->
                        programConfiguration.disableManualLocation() != true
                    } ?: true

            MapScope.DATA_SET ->
                programConfigurationRepository
                    .getConfigurationByDataSet(uid ?: "")
                    ?.let { dataSetConfiguration ->
                        dataSetConfiguration.disableManualLocation() != true
                    } ?: true
        }

    private val forcedLocationPrecision =
        when (scope) {
            MapScope.PROGRAM ->
                programConfigurationRepository
                    .getConfigurationByProgram(uid ?: "")
                    ?.let { programConfiguration ->
                        programConfiguration.minimumLocationAccuracy() ?: DEFAULT_FORCED_LOCATION_ACCURACY
                    } ?: DEFAULT_FORCED_LOCATION_ACCURACY

            MapScope.DATA_SET ->
                programConfigurationRepository
                    .getConfigurationByDataSet(uid ?: "")
                    ?.let { dataSetConfiguration ->
                        dataSetConfiguration.minimumLocationAccuracy() ?: DEFAULT_FORCED_LOCATION_ACCURACY
                    } ?: DEFAULT_FORCED_LOCATION_ACCURACY
        }

    fun fetchMapStyles(): List<BaseMapStyle> {
        val defaultMap =
            d2
                .settingModule()
                .systemSetting()
                .defaultBaseMap()
                .blockingGet()
                ?.value()
        return d2
            .mapsModule()
            .mapLayers()
            .withImageryProviders()
            .byMapLayerPosition()
            .eq(MapLayerPosition.BASEMAP)
            .blockingGet()
            .map { mapLayer ->
                val id = mapLayer.displayName()

                val tileUrls =
                    mapLayer.imageUrl().mapTileUrls(
                        mapLayer.subdomainPlaceholder(),
                        mapLayer.subdomains(),
                    )
                val attribution = mapLayer.imageryProviders().mapAttribution()

                val overlays =
                    d2
                        .mapsModule()
                        .mapLayers()
                        .withImageryProviders()
                        .byLinkedLayerUid()
                        .eq(mapLayer.uid())
                        .byMapLayerPosition()
                        .eq(MapLayerPosition.OVERLAY)
                        .blockingGet()

                val basemapOverlays =
                    overlays.map { overlay ->
                        val id = overlay.displayName()
                        val tileUrls =
                            overlay.imageUrl().mapTileUrls(
                                overlay.subdomainPlaceholder(),
                                overlay.subdomains(),
                            )
                        val attribution = overlay.imageryProviders().mapAttribution()
                        Overlay(
                            id = id,
                            tiles = tileUrls,
                            attribution = attribution,
                        )
                    }

                build(id, tileUrls, attribution, basemapOverlays, defaultMap == mapLayer.uid())
            }
    }

    fun isManualCaptureEnabled(): Boolean = canCaptureManually

    fun getForcedLocationAccuracy(): Int = forcedLocationPrecision
}

fun String.mapTileUrls(
    subdomainPlaceholder: String?,
    subdomains: List<String>?,
): List<String> =
    when {
        subdomainPlaceholder != null && !subdomains.isNullOrEmpty() ->
            subdomains.map { subdomain -> this.replace(subdomainPlaceholder, subdomain) }

        this.contains("{subdomain}") ->
            possibleSubdomainsForSubdomain().map { subdomain ->
                this.replace("{subdomain}", subdomain)
            }

        this.contains("{s}") ->
            possibleSubdomainsForS().map { subdomain ->
                this.replace("{s}", subdomain)
            }

        else -> listOf(this)
    }

private fun possibleSubdomainsForS() =
    listOf(
        "a",
        "b",
        "c",
        "d",
    )

private fun possibleSubdomainsForSubdomain() =
    listOf(
        "t0",
        "t1",
        "t2",
        "t3",
    )

fun List<MapLayerImageryProvider>?.mapAttribution(): String {
    if (this == null) return ""
    val attribution = StringBuilder()
    for (imageryProvider in this) {
        if (attribution.toString().isNotEmpty()) {
            attribution.append(", ")
        }
        attribution.append(imageryProvider.attribution())
    }
    return attribution.toString()
}
