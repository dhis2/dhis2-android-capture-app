package org.dhis2.maps.layer.basemaps

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.google.gson.Gson
import org.dhis2.maps.R
import org.maplibre.android.maps.Style

const val OSM_LIGHT = "OSM Light"
const val OSM_DETAILED = "OSM Detailed"
const val BING_ROAD = "Bing Road"
const val BING_DARK = "Bing Dark"
const val BING_AERIAL = "Bing Aerial"
const val BING_AERIAL_LABELS = "Bing Aerial Labels"
const val AZURE_ROAD = "Azure Road"
const val AZURE_DARK = "Azure Dark"
const val AZURE_AERIAL = "Azure Aerial"
const val AZURE_AERIAL_LABELS = "Azure Aerial Labels"

const val DEFAULT_STYLE_URL =
    "https://tiles.openfreemap.org/styles/positron"
const val DEFAULT_GLYPH_URL =
    "https://fonts.openmaptiles.org/{fontstack}/{range}.pbf"

// Must be served by both DEFAULT_GLYPH_URL and the glyphs of DEFAULT_STYLE_URL
const val DEFAULT_FONT =
    "Noto Sans Regular"
const val DEFAULT_ATTRIBUTION =
    "© OpenFreeMap © OpenMapTiles © OpenStreetMap contributors"

class BaseMapManager(
    private val context: Context,
    val baseMapStyles: List<BaseMapStyle>,
) {
    fun getBaseMaps() =
        baseMapStyles.map {
            BaseMap(
                baseMapStyle = it,
                basemapName = baseMapName(it.id),
                basemapImage = baseMapImage(it.id),
            )
        }

    private fun baseMapName(basemapId: String): String {
        val id =
            when (basemapId) {
                OSM_LIGHT -> R.string.dialog_layer_base_map_osm_light
                OSM_DETAILED -> R.string.dialog_layer_base_map_osm_detailed
                BING_ROAD -> R.string.dialog_layer_base_map_bing_road
                BING_DARK -> R.string.dialog_layer_base_map_bing_dark
                BING_AERIAL -> R.string.dialog_layer_base_map_bing_aerial
                BING_AERIAL_LABELS -> R.string.dialog_layer_base_map_bing_aerial_label
                AZURE_ROAD -> R.string.dialog_layer_base_map_azure_road
                AZURE_DARK -> R.string.dialog_layer_base_map_azure_dark
                AZURE_AERIAL -> R.string.dialog_layer_base_map_azure_aerial
                AZURE_AERIAL_LABELS -> R.string.dialog_layer_base_map_azure_aerial_label
                else -> null
            }
        return id?.let {
            context.getString(it)
        } ?: basemapId
    }

    private fun baseMapImage(basemapId: String): Drawable? {
        val id =
            when (basemapId) {
                OSM_LIGHT -> R.drawable.basemap_osm_light
                OSM_DETAILED -> R.drawable.basemap_osm_detailed
                BING_ROAD -> R.drawable.basemap_bing_road
                BING_DARK -> R.drawable.basemap_bing_dark
                BING_AERIAL -> R.drawable.basemap_bing_aerial
                BING_AERIAL_LABELS -> R.drawable.basemap_bing_aerial_labels
                AZURE_ROAD -> R.drawable.basemap_azure_road
                AZURE_DARK -> R.drawable.basemap_azure_dark
                AZURE_AERIAL -> R.drawable.basemap_azure_aerial
                AZURE_AERIAL_LABELS -> R.drawable.basemap_azure_hybrid
                else -> null
            }
        return id?.let {
            AppCompatResources.getDrawable(context, it)
        }
    }

    fun styleJson(baseMapStyle: BaseMapStyle): Style.Builder =
        baseMapStyle.styleUrl?.let { styleUrl ->
            Style.Builder().fromUri(styleUrl)
        } ?: Style
            .Builder()
            .fromJson(Gson().toJson(baseMapStyle.copy(glyphs = DEFAULT_GLYPH_URL)))
}
