package org.dhis2.maps.layer.basemaps

object BaseMapStyleBuilder {
    fun build(
        id: String,
        tileUrls: List<String>,
        attribution: String,
        overlays: List<Overlay>,
        isDefault: Boolean,
    ) = if (tileUrls.size > 1) {
        BaseMapStyle(
            version = 8,
            sources =
                mapOf(
                    "raster-tiles" to
                        RasterTiles(
                            type = "raster",
                            tiles = tileUrls,
                            tileSize = 256,
                        ),
                ) + setUpOverlaySources(overlays),
            layers = setUpLayers(overlays),
            id = id,
            glyphs = DEFAULT_GLYPH_URL,
            isDefault = isDefault,
            attribution =
                (listOf(attribution) + overlays.map { overlay -> overlay.attribution }).joinToString(
                    separator = ", ",
                ),
        )
    } else {
        BaseMapStyle(
            version = 8,
            sources = setUpOverlaySources(overlays),
            layers = setUpLayers(overlays),
            id = id,
            glyphs = DEFAULT_GLYPH_URL,
            isDefault = isDefault,
            attribution = attribution,
            styleUrl = tileUrls.first(),
        )
    }

    fun internalBaseMap(): BaseMapStyle =
        BaseMapStyle(
            version = 8,
            sources = emptyMap(),
            layers = emptyList(),
            id = OSM_LIGHT,
            glyphs = DEFAULT_GLYPH_URL,
            isDefault = true,
            attribution = DEFAULT_ATTRIBUTION,
            styleUrl = DEFAULT_STYLE_URL,
        )

    private fun setUpOverlaySources(overlays: List<Overlay>) =
        overlays.associate { overlay ->
            overlay.id to
                RasterTiles(
                    type = "raster",
                    tiles = overlay.tiles,
                    tileSize = 256,
                )
        }

    private fun setUpLayers(overlays: List<Overlay>) =
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
            }
}

data class BaseMapStyle(
    val version: Int,
    val sources: Map<String, RasterTiles>,
    val layers: List<StyleLayers>,
    val id: String,
    var glyphs: String,
    val isDefault: Boolean,
    val attribution: String,
    val styleUrl: String? = null,
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
