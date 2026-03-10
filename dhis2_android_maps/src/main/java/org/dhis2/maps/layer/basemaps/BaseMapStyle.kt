package org.dhis2.maps.layer.basemaps

object BaseMapStyleBuilder {
    fun build(
        id: String,
        tileUrls: List<String>,
        attribution: String,
        overlays: List<Overlay>,
        isDefault: Boolean,
    ) = BaseMapStyle(
        version = 8,
        sources =
            mapOf(
                "raster-tiles" to
                    RasterTiles(
                        type = "raster",
                        tiles = tileUrls,
                        tileSize = 256,
                    ),
            ) +
                overlays.associate { overlay ->
                    overlay.id to
                        RasterTiles(
                            type = "raster",
                            tiles = overlay.tiles,
                            tileSize = 256,
                        )
                },
        layers =
            listOf(
                StyleLayers(
                    id = "simple-tiles",
                    type = "raster",
                    source = "raster-tiles",
                    minZoom = 0,
                    maxZoom = 22,
                ),
            ) +
                overlays.map { overlay ->
                    StyleLayers(
                        id = overlay.id,
                        type = "raster",
                        source = overlay.id,
                        minZoom = 0,
                        maxZoom = 22,
                    )
                },
        id = id,
        glyphs = DEFAULT_GLYPH_URL,
        isDefault = isDefault,
        attribution =
            (listOf(attribution) + overlays.map { overlay -> overlay.attribution }).joinToString(
                separator = ", ",
            ),
    )

    fun internalBaseMap(): BaseMapStyle =
        build(
            id = OSM_LIGHT,
            tileUrls =
                listOf(
                    DEFAULT_TILE_URL.replace("{s}", "a"),
                    DEFAULT_TILE_URL.replace("{s}", "b"),
                    DEFAULT_TILE_URL.replace("{s}", "c"),
                    DEFAULT_TILE_URL.replace("{s}", "d"),
                ),
            attribution = DEFAULT_ATTRIBUTION,
            isDefault = true,
            overlays = emptyList(),
        )
}

data class BaseMapStyle(
    val version: Int,
    val sources: Map<String, RasterTiles>,
    val layers: List<StyleLayers>,
    val id: String,
    var glyphs: String,
    val isDefault: Boolean,
    val attribution: String,
)

data class Overlay(
    val id: String,
    val tiles: List<String>,
    val attribution: String,
)

data class RasterTiles(
    val type: String,
    val tiles: List<String>,
    val tileSize: Int,
)

data class StyleLayers(
    val id: String,
    val type: String,
    val source: String,
    val minZoom: Int,
    val maxZoom: Int,
)
