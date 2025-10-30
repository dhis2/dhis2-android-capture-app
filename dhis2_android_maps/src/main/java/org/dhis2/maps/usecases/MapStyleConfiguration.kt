package org.dhis2.maps.usecases

import org.dhis2.commons.data.ProgramConfigurationRepository
import org.dhis2.maps.layer.basemaps.BaseMapStyle
import org.dhis2.maps.layer.basemaps.BaseMapStyleBuilder.build
import org.dhis2.maps.model.MapScope
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.map.layer.MapLayerImageryProvider

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
            .blockingGet()
            .map { mapLayer ->
                val id = mapLayer.displayName()

                val tileUrls =
                    mapLayer.imageUrl().mapTileUrls(
                        mapLayer.subdomainPlaceholder(),
                        mapLayer.subdomains(),
                    )
                val attribution = mapLayer.imageryProviders().mapAttribution()
                build(id, tileUrls, attribution, defaultMap == mapLayer.uid())
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
