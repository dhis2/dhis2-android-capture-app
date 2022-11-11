package org.dhis2.maps.layer

enum class BaseMapType(val styleJsonFileName: String) {
    OSM_LIGHT("basemap_osm_light"),
    OSM_DETAILED("basemap_osm_detailed"),
    BING_ROAD("basemap_canvas_light"),
    BING_DARK("basemap_canvas_dark"),
    BING_AERIAL("basemap_aerial"),
    BING_AERIAL_LABELS("basemap_aerial_labels")
}
