package org.dhis2.maps.layer.basemaps

import com.google.gson.Gson
import com.mapbox.mapboxsdk.maps.Style
import org.dhis2.maps.R

object BaseMapManager {
    fun getBaseMaps() = listOf(
        BaseMap(
            BaseMapStyleBuilder.build(
                listOf(
                    "https://cartodb-basemaps-a.global.ssl.fastly.net/light_all/{z}/{x}/{y}@2x.png",
                    "https://cartodb-basemaps-b.global.ssl.fastly.net/light_all/{z}/{x}/{y}@2x.png",
                    "https://cartodb-basemaps-c.global.ssl.fastly.net/light_all/{z}/{x}/{y}@2x.png",
                    "https://cartodb-basemaps-d.global.ssl.fastly.net/light_all/{z}/{x}/{y}@2x.png"
                ),
                "osm light"
            ),
            R.string.dialog_layer_base_map_osm_light,
            R.drawable.basemap_osm_light
        ),
        BaseMap(
            BaseMapStyleBuilder.build(
                listOf(
                    "https://a.tile.openstreetmap.org/{z}/{x}/{y}.png",
                    "https://b.tile.openstreetmap.org/{z}/{x}/{y}.png",
                    "https://c.tile.openstreetmap.org/{z}/{x}/{y}.png",
                    "https://d.tile.openstreetmap.org/{z}/{x}/{y}.png"
                ),
                "osm detailed"
            ),
            R.string.dialog_layer_base_map_osm_detailed,
            R.drawable.basemap_osm_detailed
        ),
        BaseMap(
            BaseMapStyleBuilder.build(
                listOf(
                    "https://t0.ssl.ak.dynamic.tiles.virtualearth.net/comp/ch/{quadkey}?mkt=en-GB&it=G,L&cstl=WL&og=2056&n=z",
                    "https://t1.ssl.ak.dynamic.tiles.virtualearth.net/comp/ch/{quadkey}?mkt=en-GB&it=G,L&cstl=WL&og=2056&n=z",
                    "https://t2.ssl.ak.dynamic.tiles.virtualearth.net/comp/ch/{quadkey}?mkt=en-GB&it=G,L&cstl=WL&og=2056&n=z",
                    "https://t3.ssl.ak.dynamic.tiles.virtualearth.net/comp/ch/{quadkey}?mkt=en-GB&it=G,L&cstl=WL&og=2056&n=z"
                ),
                "bing road"
            ),
            R.string.dialog_layer_base_map_bing_road,
            R.drawable.basemap_bing_road
        ),
        BaseMap(
            BaseMapStyleBuilder.build(
                listOf(
                    "https://t0.ssl.ak.dynamic.tiles.virtualearth.net/comp/ch/{quadkey}?mkt=en-GB&it=G,L&cstl=WD&og=2056&n=z",
                    "https://t1.ssl.ak.dynamic.tiles.virtualearth.net/comp/ch/{quadkey}?mkt=en-GB&it=G,L&cstl=WD&og=2056&n=z",
                    "https://t2.ssl.ak.dynamic.tiles.virtualearth.net/comp/ch/{quadkey}?mkt=en-GB&it=G,L&cstl=WD&og=2056&n=z",
                    "https://t3.ssl.ak.dynamic.tiles.virtualearth.net/comp/ch/{quadkey}?mkt=en-GB&it=G,L&cstl=WD&og=2056&n=z"
                ),
                "bing dark"
            ),
            R.string.dialog_layer_base_map_bing_dark,
            R.drawable.basemap_bing_dark
        ),
        BaseMap(
            BaseMapStyleBuilder.build(
                listOf(
                    "https://ecn.t0.tiles.virtualearth.net/tiles/a{quadkey}.jpeg?g=13019",
                    "https://ecn.t1.tiles.virtualearth.net/tiles/a{quadkey}.jpeg?g=13019",
                    "https://ecn.t2.tiles.virtualearth.net/tiles/a{quadkey}.jpeg?g=13019",
                    "https://ecn.t3.tiles.virtualearth.net/tiles/a{quadkey}.jpeg?g=13019"
                ),
                "bing aerial"
            ),
            R.string.dialog_layer_base_map_bing_aerial,
            R.drawable.basemap_bing_aerial
        ),
        BaseMap(
            BaseMapStyleBuilder.build(
                listOf(
                    "https://t0.ssl.ak.dynamic.tiles.virtualearth.net/comp/ch/{quadkey}?mkt=en-GB&it=A,G,L&og=2056&n=z",
                    "https://t1.ssl.ak.dynamic.tiles.virtualearth.net/comp/ch/{quadkey}?mkt=en-GB&it=A,G,L&og=2056&n=z",
                    "https://t2.ssl.ak.dynamic.tiles.virtualearth.net/comp/ch/{quadkey}?mkt=en-GB&it=A,G,L&og=2056&n=z",
                    "https://t3.ssl.ak.dynamic.tiles.virtualearth.net/comp/ch/{quadkey}?mkt=en-GB&it=A,G,L&og=2056&n=z"
                ),
                "bing aerial labels"
            ),
            R.string.dialog_layer_base_map_bing_aerial_label,
            R.drawable.basemap_bing_aerial_labels
        )
    )

    fun styleJson(
        baseMapStyle: BaseMapStyle
    ): Style.Builder {
        return Style.Builder().fromJson(Gson().toJson(baseMapStyle))
        /*return Style.Builder().fromJson(
            "" +
                    "{\n" +
                    "  \"version\": 8,\n" +
                    "  \"sources\": {\n" +
                    "    \"raster-tiles\": {\n" +
                    "      \"type\": \"raster\",\n" +
                    "      \"tiles\": [\n" +
                    "        \"https://ecn.t0.tiles.virtualearth.net/tiles/a{quadkey}.jpeg?g=13019\",\n" +
                    "        \"https://ecn.t1.tiles.virtualearth.net/tiles/a{quadkey}.jpeg?g=13019\",\n" +
                    "        \"https://ecn.t2.tiles.virtualearth.net/tiles/a{quadkey}.jpeg?g=13019\",\n" +
                    "        \"https://ecn.t3.tiles.virtualearth.net/tiles/a{quadkey}.jpeg?g=13019\"\n" +
                    "      ],\n" +
                    "      \"tileSize\": 256\n" +
                    "    }\n" +
                    "  },\n" +
                    "  \"layers\": [\n" +
                    "    {\n" +
                    "      \"id\": \"simple-tiles\",\n" +
                    "      \"type\": \"raster\",\n" +
                    "      \"source\": \"raster-tiles\",\n" +
                    "      \"minzoom\": 0,\n" +
                    "      \"maxzoom\": 22\n" +
                    "    }\n" +
                    "  ],\n" +
                    "  \"id\": \"test\"\n" +
                    "}"
        )*/
    }
}
