package org.dhis2.uicomponents.map

import androidx.core.content.ContextCompat
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.FeatureCollection
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager
import com.mapbox.mapboxsdk.style.expressions.Expression
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import java.util.HashMap
import org.dhis2.R
import org.dhis2.uicomponents.map.layer.MapLayerManager
import org.dhis2.utils.ColorUtils
import org.hisp.dhis.android.core.common.FeatureType

class TeiMapManager(
    override var mapView: MapView,
    private val teiFeatureCollections: HashMap<String, FeatureCollection>,
    private val boundingBox: BoundingBox,
    private val changingStyle: Boolean,
    private val featureType: FeatureType,
    private val mapStyle: MapStyle
) : MapManager() {

    companion object {
        const val TEIS_TAG = "teis"
        const val ENROLLMENT_TAG = "enrollments"
        const val TEI = "TEI"
        const val ENROLLMENT = "ENROLLMENT"
        const val TEI_ICON_ID = "ICON_ID"
        const val ENROLLMENT_ICON_ID = "ICON_ENROLLMENT_ID"
    }
    override fun init() {
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
                (map?.style?.getSource(TEIS_TAG) as GeoJsonSource?)?.setGeoJson(
                    teiFeatureCollections[TEI]
                )
                (map?.style?.getSource(ENROLLMENT_TAG) as GeoJsonSource?)?.setGeoJson(
                    teiFeatureCollections[ENROLLMENT]
                )
                initCameraPosition(boundingBox)
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
                    init(style,
                        TEIS_TAG, featureType)
                    instance().setEnrollmentLayerData(
                        ColorUtils.getColorFrom(
                            mapStyle.programColor,
                            ColorUtils.getPrimaryColor(
                                mapView.context,
                                ColorUtils.ColorType.PRIMARY
                            )
                        ),
                        ColorUtils.getPrimaryColor(
                            mapView.context,
                            ColorUtils.ColorType.PRIMARY_DARK
                        ),
                        featureType
                    )
                }
                else -> instance().updateStyle(style)
            }
        }

        style.addImage(
            TEI_ICON_ID,
            TeiMarkers.getMarker(
                mapView.context,
                mapStyle.teiSymbolIcon ?: ContextCompat.getDrawable(
                    mapView.context,
                    R.drawable.mapbox_marker_icon_default
                )!!,
                mapStyle.teiColor ?: ColorUtils.getPrimaryColor(
                    mapView.context,
                    ColorUtils.ColorType.PRIMARY
                )
            )
        )
        style.addImage(
            ENROLLMENT_ICON_ID,
            TeiMarkers.getMarker(
                mapView.context,
                mapStyle.enrollmentSymbolIcon!!,
                mapStyle.enrollmentColor!!
            )
        )
        setSource(style)
        setLayer(style)
        teiFeatureCollection[TEI]?.let { setSymbolManager(style, it) }
        onMapClickListener?.let { map?.addOnMapClickListener(it) }
        initCameraPosition(boundingBox)
        markerViewManager = MarkerViewManager(mapView, map)
    }

    override fun setSource(style: Style) {
        style.addSource(GeoJsonSource(TEIS_TAG, teiFeatureCollections[TEI]))
        style.addSource(GeoJsonSource(ENROLLMENT_TAG, teiFeatureCollections[ENROLLMENT]))
    }

    override fun setLayer(style: Style) {
        val symbolLayer = SymbolLayer(POINT_LAYER,
            TEIS_TAG
        ).withProperties(
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
                ),
                Expression.literal("Point")
            )
        )
        style.addLayer(symbolLayer)
        if (featureType != FeatureType.POINT) {
            style.addLayerBelow(
                FillLayer(POLYGON_LAYER,
                    TEIS_TAG
                )
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
                            ),
                            Expression.literal("Polygon")
                        )
                    ),
                POINT_LAYER
            )
            style.addLayerAbove(
                LineLayer(POLYGON_BORDER_LAYER,
                    TEIS_TAG
                )
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
                            ),
                            Expression.literal("Polygon")
                        )
                    ),
                POLYGON_LAYER
            )
        }
    }
}
