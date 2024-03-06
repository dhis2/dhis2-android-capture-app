package org.dhis2.maps.layer.basemaps

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import com.google.gson.Gson
import com.mapbox.mapboxsdk.maps.Style
import org.dhis2.maps.R
import org.dhis2.maps.layer.basemaps.BaseMapStyleBuilder.internalBaseMap

const val OSM_LIGHT = "OSM Light"
const val OSM_DETAILED = "OSM Detailed"
const val BING_ROAD = "Bing Road"
const val BING_DARK = "Bing Dark"
const val BING_AERIAL = "Bing Aerial"
const val BING_AERIAL_LABELS = "Bing Aerial Labels"
const val DEFAULT_TILE_URL =
    "https://cartodb-basemaps-{s}.global.ssl.fastly.net/light_all/{z}/{x}/{y}@2x.png"
const val DEFAULT_GLYPH_URL =
    "http://fonts.openmaptiles.org/{fontstack}/{range}.pbf"
const val DEFAULT_FONT =
    "Klokantech Noto Sans Regular"
const val DEFAULT_ATTRIBUTION = "© OpenStreetMap contributors, © Carto"

class BaseMapManager(
    private val context: Context,
    val baseMapStyles: List<BaseMapStyle>
) {
    fun getBaseMaps() = baseMapStyles.map {
        BaseMap(
            baseMapStyle = it,
            basemapName = baseMapName(it.id),
            basemapImage = baseMapImage(it.id)
        )
    }

    private fun baseMapName(basemapId: String): String {
        val id = when (basemapId) {
            OSM_LIGHT -> R.string.dialog_layer_base_map_osm_light
            OSM_DETAILED -> R.string.dialog_layer_base_map_osm_detailed
            BING_ROAD -> R.string.dialog_layer_base_map_bing_road
            BING_DARK -> R.string.dialog_layer_base_map_bing_dark
            BING_AERIAL -> R.string.dialog_layer_base_map_bing_aerial
            BING_AERIAL_LABELS -> R.string.dialog_layer_base_map_bing_aerial_label
            else -> null
        }
        return id?.let {
            context.getString(it)
        } ?: basemapId
    }

    private fun baseMapImage(basemapId: String): Drawable? {
        val id = when (basemapId) {
            OSM_LIGHT -> R.drawable.basemap_osm_light
            OSM_DETAILED -> R.drawable.basemap_osm_detailed
            BING_ROAD -> R.drawable.basemap_bing_road
            BING_DARK -> R.drawable.basemap_bing_dark
            BING_AERIAL -> R.drawable.basemap_bing_aerial
            BING_AERIAL_LABELS -> R.drawable.basemap_bing_aerial_labels
            else -> null
        }
        return id?.let {
            AppCompatResources.getDrawable(context, it)
        }
    }

    fun styleJson(baseMapStyle: BaseMapStyle): Style.Builder {
        return Style.Builder()
            .fromJson(Gson().toJson(baseMapStyle.copy(glyphs = DEFAULT_GLYPH_URL)))
    }

    fun getDefaultBasemap(): BaseMapStyle {
        return baseMapStyles.firstOrNull() ?: internalBaseMap()
    }
}
