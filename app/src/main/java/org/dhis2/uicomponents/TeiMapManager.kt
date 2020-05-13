package org.dhis2.uicomponents

import androidx.core.content.ContextCompat
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import org.dhis2.R
import org.dhis2.utils.ColorUtils
import org.dhis2.utils.maps.MapLayerManager
import org.dhis2.utils.maps.MarkerUtils
import org.hisp.dhis.android.core.common.FeatureType
import java.util.HashMap

class TeiMapManager(
    private val teiFeatureCollections: HashMap<String, FeatureCollection>,
    private val boundingBox: BoundingBox
): MapManager() {

    companion object {
        const val TEIS_TAG = "teis"
        const val ENROLLMENT_TAG = "enrollments"

    }

    override fun setStyle() {
        when {
            map == null -> {
                mapView.getMapAsync {
                    map = it
                    map?.setStyle(
                        Style.MAPBOX_STREETS
                    ) { style: Style ->
                        loadDataForStyle(
                            style,
                            teiFeatureCollections,
                            boundingBox
                        )
                    }
                }
            }
            changingStyle -> {
                map?.setStyle(
                    Style.MAPBOX_STREETS
                ) { style: Style ->
                    loadDataForStyle(
                        style,
                        teiFeatureCollections,
                        boundingBox
                    )
                }
            }
            else -> {
                (map?.style?.getSource(TEIS_TAG) as GeoJsonSource?)?.setGeoJson(teiFeatureCollections["TEI"])
                (map?.style?.getSource(ENROLLMENT_TAG) as GeoJsonSource?)?.setGeoJson(
                    teiFeatureCollections["ENROLLMENT"]
                )
                initCameraPosition(map!!, boundingBox)
            }
        }
    }

    private fun loadDataForStyle(
        style: Style,
        teiFeatureCollection: HashMap<String, FeatureCollection>,
        boundingBox: BoundingBox
    ) {
        MapLayerManager.run {
            when {
                !changingStyle -> {
                    init(style, TEIS_TAG, featureType ?: FeatureType.NONE)
                    instance().setEnrollmentLayerData(
                        ColorUtils.getColorFrom(
                            mapStyle?.programColor,
                            ColorUtils.getPrimaryColor(
                                mapView.context,
                                ColorUtils.ColorType.PRIMARY
                            )
                        ),
                        ColorUtils.getPrimaryColor(
                            mapView.context,
                            ColorUtils.ColorType.PRIMARY_DARK
                        ),
                        featureType ?: FeatureType.NONE
                    )
                }
                else -> instance().updateStyle(style)
            }
        }
        onMapClickListener?.let { map?.addOnMapClickListener(it) }
        style.addImage(
            "ICON_ID",
            MarkerUtils.getMarker(
                mapView.context,
                mapStyle?.teiSymbolIcon ?: ContextCompat.getDrawable(
                    mapView.context,
                    R.drawable.mapbox_marker_icon_default
                )!!,
                mapStyle?.teiColor ?: ColorUtils.getPrimaryColor(
                    mapView.context,
                    ColorUtils.ColorType.PRIMARY
                )
            )
        )
        style.addImage(
            "ICON_ENROLLMENT_ID",
            MarkerUtils.getMarker(
                mapView.context,
                mapStyle?.enrollmentSymbolIcon!!,
                mapStyle?.enrollmentColor!!
            )
        )
        setSource(style)
        setLayer(style)
        initCameraPosition(map!!, boundingBox)
        if(markerViewManager == null) {
            markerViewManager = MarkerViewManager(mapView, map)
        }
        if (symbolManager == null) {
            symbolManager = SymbolManager(
                mapView, map!!, style, null,
                GeoJsonOptions().withTolerance(0.4f)
            ).apply {
                iconAllowOverlap = true
                textAllowOverlap = true
                iconIgnorePlacement = true
                textIgnorePlacement = true
                symbolPlacement = "line-center"
                teiFeatureCollection["TEI"]?.let { it -> create(it) }
            }

        }
    }

    override fun setSource(style: Style) {
        style.addSource(GeoJsonSource(TEIS_TAG, teiFeatureCollections["TEI"]))
        style.addSource(GeoJsonSource(ENROLLMENT_TAG, teiFeatureCollections["ENROLLMENT"]))
    }

    override fun setLayer(style: Style) {
        val symbolLayer = SymbolLayer("POINT_LAYER", TEIS_TAG).withProperties(
            PropertyFactory.iconImage(
                Expression.get(
                    "teiImage"
                )
            ),
            PropertyFactory.iconOffset(
                arrayOf(
                    0f,
                    -25f
                )
            ),
            PropertyFactory.iconAllowOverlap(true),
            PropertyFactory.textAllowOverlap(true)
        )
        symbolLayer.setFilter(
            Expression.eq(
                Expression.literal(
                    "\$type"
                ), Expression.literal("Point")
            )
        )
        style.addLayer(symbolLayer)
        if (featureType != FeatureType.POINT) {
            style.addLayerBelow(
                FillLayer("POLYGON_LAYER", TEIS_TAG)
                    .withProperties(
                        PropertyFactory.fillColor(
                            ColorUtils.getPrimaryColorWithAlpha(
                                mapView.context,
                                ColorUtils.ColorType.PRIMARY_LIGHT,
                                150f
                            )
                        )
                    )
                    .withFilter(
                        Expression.eq(
                            Expression.literal(
                                "\$type"
                            ), Expression.literal("Polygon")
                        )
                    ),
                "POINT_LAYER"
            )
            style.addLayerAbove(
                LineLayer("POLYGON_BORDER_LAYER", TEIS_TAG)
                    .withProperties(
                        PropertyFactory.lineColor(
                            ColorUtils.getPrimaryColor(
                                mapView.context,
                                ColorUtils.ColorType.PRIMARY_DARK
                            )
                        ),
                        PropertyFactory.lineWidth(2f)
                    )
                    .withFilter(
                        Expression.eq(
                            Expression.literal(
                                "\$type"
                            ), Expression.literal("Polygon")
                        )
                    ),
                "POLYGON_LAYER"
            )
        }
    }
}