package org.dhis2.maps.usecases

import org.dhis2.maps.layer.basemaps.BaseMapStyle
import org.dhis2.maps.layer.basemaps.BaseMapStyleBuilder.build
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.map.layer.MapLayerImageryProvider

class MapStyleConfiguration(private val d2: D2) {
    fun fetchMapStyles(): List<BaseMapStyle> {
        return d2.mapsModule().mapLayers().withImageryProviders().blockingGet()
            .map { mapLayer ->
                val id = mapLayer.displayName()

                val tileUrls = mapLayer.imageUrl().mapTileUrls(
                    mapLayer.subdomainPlaceholder(),
                    mapLayer.subdomains()
                )
                val attribution = mapLayer.imageryProviders().mapAttribution()
                build(id, tileUrls, attribution)
            }
    }
}

fun String.mapTileUrls(subdomainPlaceholder: String?, subdomains: List<String>?): List<String> {
    return subdomains
        .takeIf { subdomainPlaceholder != null && !it.isNullOrEmpty() }
        ?.map { subdomain ->
            this.replace(subdomainPlaceholder!!, subdomain)
        }
        ?: listOf(this)
}

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
