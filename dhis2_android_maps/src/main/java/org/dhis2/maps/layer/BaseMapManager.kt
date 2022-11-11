package org.dhis2.maps.layer

import android.content.res.AssetManager
import com.mapbox.mapboxsdk.maps.Style
import java.io.IOException
import java.io.InputStream
import java.nio.charset.Charset
import org.dhis2.maps.R

object BaseMapManager {
    fun getBaseMaps() = BaseMapType.values().map { baseMapType ->
        when (baseMapType) {
            BaseMapType.OSM_LIGHT ->
                BaseMap(
                    baseMapType,
                    R.string.dialog_layer_base_map_osm_light,
                    R.drawable.basemap_osm_light
                )
            BaseMapType.OSM_DETAILED ->
                BaseMap(
                    baseMapType,
                    R.string.dialog_layer_base_map_osm_detailed,
                    R.drawable.basemap_osm_detailed
                )
            BaseMapType.BING_ROAD ->
                BaseMap(
                    baseMapType,
                    R.string.dialog_layer_base_map_bing_road,
                    R.drawable.basemap_bing_road
                )
            BaseMapType.BING_DARK ->
                BaseMap(
                    baseMapType,
                    R.string.dialog_layer_base_map_bing_dark,
                    R.drawable.basemap_bing_dark
                )
            BaseMapType.BING_AERIAL ->
                BaseMap(
                    baseMapType,
                    R.string.dialog_layer_base_map_bing_aerial,
                    R.drawable.basemap_bing_aerial
                )
            BaseMapType.BING_AERIAL_LABELS ->
                BaseMap(
                    baseMapType,
                    R.string.dialog_layer_base_map_bing_aerial_label,
                    R.drawable.basemap_bing_aerial_labels
                )
        }
    }

    fun loadStyle(assets: AssetManager, baseMapType: BaseMapType): Style.Builder {
        val json: String = try {
            val `is`: InputStream = assets.open("${baseMapType.styleJsonFileName}.json")
            val size = `is`.available()
            val buffer = ByteArray(size)
            `is`.read(buffer)
            `is`.close()
            String(buffer, Charset.forName("UTF-8"))
        } catch (ex: IOException) {
            ex.printStackTrace()
            ""
        }
        return Style.Builder().fromJson(json)
    }
}
