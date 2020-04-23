package org.dhis2.utils.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.eq
import com.mapbox.mapboxsdk.style.expressions.Expression.heatmapDensity
import com.mapbox.mapboxsdk.style.expressions.Expression.interpolate
import com.mapbox.mapboxsdk.style.expressions.Expression.linear
import com.mapbox.mapboxsdk.style.expressions.Expression.literal
import com.mapbox.mapboxsdk.style.expressions.Expression.rgb
import com.mapbox.mapboxsdk.style.expressions.Expression.rgba
import com.mapbox.mapboxsdk.style.expressions.Expression.stop
import com.mapbox.mapboxsdk.style.expressions.Expression.zoom
import com.mapbox.mapboxsdk.style.layers.FillLayer
import com.mapbox.mapboxsdk.style.layers.HeatmapLayer
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property
import com.mapbox.mapboxsdk.style.layers.PropertyFactory
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapColor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.heatmapRadius
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.utils.ColorUtils
import org.hisp.dhis.android.core.common.FeatureType

class MapLayerManager private constructor(
    var style: Style,
    private val sourceName: String,
    private val sourceFeatureType: FeatureType
) {

    private var satelliteStyle: MutableLiveData<Boolean> = MutableLiveData(false)
    private var showTei: MutableLiveData<Boolean> = MutableLiveData(true)
    private var showEnrollment: MutableLiveData<Boolean> = MutableLiveData(false)
    private var showHeatMap: MutableLiveData<Boolean> = MutableLiveData(false)
    private var enrollmentColor: Int = -1
    private var enrollmentDarkColor: Int = -1
    private var enrollmentFeatureType: FeatureType? = FeatureType.NONE
    private var disposable: CompositeDisposable = CompositeDisposable()

    companion object {

        const val ENROLLMENT_POINT_LAYER_ID: String = "ENROLLMENT_POINT_LAYER"
        const val ENROLLMENT_POLYGON_LAYER_ID: String = "ENROLLMENT_POLYGON_LAYER"
        const val ENROLLMENT_POLYGON_BORDER_LAYER_ID: String = "ENROLLMENT_POLYGON_BORDER_LAYER"
        const val HEATMAP_LAYER_ID: String = "HEATMAP_LAYER"
        const val POLYGON_LAYER_ID: String = "POLYGON_LAYER"
        const val POINT_LAYER_ID: String = "POINT_LAYER"

        private var instance: MapLayerManager? = null

        /**
         * Initialization must be done after the map style has been provided
         * */
        fun init(
            style: Style,
            sourceName: String,
            sourceFeatureType: FeatureType
        ): MapLayerManager {
            instance = MapLayerManager(style, sourceName, sourceFeatureType)
            return instance!!
        }

        fun instance(): MapLayerManager {
            if (instance == null) {
                throw NullPointerException(
                    "MapLayerManager needs to be initialized before getting an instance."
                )
            }
            return instance!!
        }

        /**
         * Call it when activity ends
         * */
        fun onDestroy() {
            instance?.clearDisposable()
            instance = null
        }
    }

    fun updateStyle(style: Style) {
        this.style = style
        handleTeiLayer(showTei.value == true)
        handleEnrollmentLayer(showEnrollment.value == true)
        handleHeatMapLayer(showHeatMap.value == true)
    }

    private fun clearDisposable() {
        disposable.clear()
    }

    fun setEnrollmentLayerData(color: Int, colorDark: Int, enrollmentFeatureType: FeatureType) {
        this.enrollmentColor = color
        this.enrollmentDarkColor = colorDark
        this.enrollmentFeatureType = enrollmentFeatureType
    }

    fun setSatelliteStyle(): LiveData<Boolean> {
        return satelliteStyle
    }

    fun showTeiLayer(): LiveData<Boolean> {
        return showTei
    }

    fun showEnrollmentLayer(): LiveData<Boolean> {
        return showEnrollment
    }

    fun showHeatMapLayer(): LiveData<Boolean> {
        return showHeatMap
    }

    fun setSatelliteLayer(check: Boolean) {
        satelliteStyle.value = check
    }

    fun setTeiLayer(check: Boolean) {
        showTei.value = check
        handleTeiLayer(check)
    }

    fun setEnrollmentLayer(check: Boolean) {
        showEnrollment.value = check
        handleEnrollmentLayer(check)
    }

    fun setHeapMapLayer(check: Boolean) {
        showHeatMap.value = check
        handleHeatMapLayer(check)
    }

    private fun handleTeiLayer(showLayer: Boolean) {
        val pointLayer = style.getLayer("POINT_LAYER")
        val polygonLayer = style.getLayer("POLYGON_LAYER")
        val polygonBorderLayer = style.getLayer("POLYGON_BORDER_LAYER")
        if (pointLayer != null) {
            if (showLayer) {
                pointLayer.setProperties(visibility(Property.VISIBLE))
            } else {
                pointLayer.setProperties(visibility(Property.NONE))
            }
        }

        if (polygonBorderLayer != null) {
            if (showLayer) {
                polygonBorderLayer.setProperties(visibility(Property.VISIBLE))
                polygonLayer?.setProperties(visibility(Property.VISIBLE))
            } else {
                polygonBorderLayer.setProperties(visibility(Property.NONE))
                polygonLayer?.setProperties(visibility(Property.NONE))
            }
        }
    }

    private fun handleHeatMapLayer(showLayer: Boolean) {
        val layer = style.getLayer(HEATMAP_LAYER_ID)
        if (layer != null) {
            if (showLayer) {
                layer.setProperties(visibility(Property.VISIBLE))
            } else {
                layer.setProperties(visibility(Property.NONE))
            }
        } else {
            style.addLayerBelow(
                HeatmapLayer(HEATMAP_LAYER_ID, sourceName).withProperties(
                    heatmapColor(
                        interpolate(
                            linear(), heatmapDensity(),
                            literal(0), rgba(33, 102, 172, 0),
                            literal(0.2), rgb(103, 169, 207),
                            literal(0.4), rgb(209, 229, 240),
                            literal(0.6), rgb(253, 219, 199),
                            literal(0.8), rgb(239, 138, 98),
                            literal(1), rgb(178, 24, 43)
                        )
                    ),
                    heatmapRadius(
                        interpolate(
                            linear(), zoom(),
                            stop(0, 2),
                            stop(9, 20)
                        )
                    ),
                    visibility(if (showLayer) Property.VISIBLE else Property.NONE)
                ),
                if (sourceFeatureType != FeatureType.POINT) POLYGON_LAYER_ID else POINT_LAYER_ID
            )
        }
    }

    private fun handleEnrollmentLayer(showLayer: Boolean) {
        if (enrollmentFeatureType != FeatureType.NONE && enrollmentColor != -1) {
            val pointLayer = style.getLayer(ENROLLMENT_POINT_LAYER_ID)
            val polygonLayer = style.getLayer(ENROLLMENT_POLYGON_LAYER_ID)
            val polygonBorderLayer = style.getLayer(ENROLLMENT_POLYGON_BORDER_LAYER_ID)
            if (pointLayer != null) {
                if (showLayer) {
                    pointLayer.setProperties(visibility(Property.VISIBLE))
                    polygonLayer?.setProperties(visibility(Property.VISIBLE))
                    polygonBorderLayer?.setProperties(visibility(Property.VISIBLE))
                } else {
                    pointLayer.setProperties(visibility(Property.NONE))
                    polygonLayer?.setProperties(visibility(Property.NONE))
                    polygonBorderLayer?.setProperties(visibility(Property.NONE))
                }
            } else {
                val symbolLayer =
                    SymbolLayer(ENROLLMENT_POINT_LAYER_ID, "enrollments").withProperties(
                        PropertyFactory.iconImage("ICON_ENROLLMENT_ID"),
                        iconOffset(arrayOf(0f, -25f)),
                        iconAllowOverlap(true)
                    )
                symbolLayer.setFilter(eq(literal("\$type"), literal("Point")))

                style.addLayerBelow(symbolLayer, "POINT_LAYER")

                if (enrollmentFeatureType != FeatureType.POINT) {
                    style.addLayerBelow(
                        FillLayer(ENROLLMENT_POLYGON_LAYER_ID, "enrollments")
                            .withProperties(
                                fillColor(ColorUtils.withAlpha(enrollmentColor))
                            )
                            .withFilter(eq(literal("\$type"), literal("Polygon"))),
                        ENROLLMENT_POINT_LAYER_ID
                    )
                    style.addLayerAbove(
                        LineLayer(ENROLLMENT_POLYGON_BORDER_LAYER_ID, "enrollments")
                            .withProperties(
                                lineColor(enrollmentDarkColor),
                                lineWidth(2f)
                            )
                            .withFilter(eq(literal("\$type"), literal("Polygon"))),
                        ENROLLMENT_POLYGON_LAYER_ID
                    )
                }
            }
        }
    }
}
