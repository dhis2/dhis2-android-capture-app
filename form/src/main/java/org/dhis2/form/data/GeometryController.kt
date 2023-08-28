package org.dhis2.form.data

import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry

class GeometryController(private val geometryParser: GeometryParser) {

    fun generateLocationFromCoordinates(featureType: FeatureType, coordinates: String?): Geometry? {
        if (coordinates == null) {
            return null
        }
        return when (featureType) {
            FeatureType.POINT -> {
                GeometryHelper.createPointGeometry(
                    geometryParser.parsePoint(coordinates),
                )
            }
            FeatureType.POLYGON -> {
                GeometryHelper.createPolygonGeometry(
                    geometryParser.parsePolygon(coordinates),
                )
            }
            else -> {
                GeometryHelper.createMultiPolygonGeometry(
                    geometryParser.parseMultipolygon(coordinates),
                )
            }
        }
    }

    fun getCoordinatesCallback(
        updateCoordinates: (value: String?) -> Unit,
        currentLocation: (fieldUid: String) -> Unit,
        mapRequest: (fieldUid: String, featureType: String, initCoordinate: String?) -> Unit,
    ): FieldUiModel.Callback {
        return object : FieldUiModel.Callback {

            override fun intent(intent: FormIntent) {
                when (intent) {
                    is FormIntent.SaveCurrentLocation -> updateCoordinates(intent.value)
                    is FormIntent.ClearValue -> updateCoordinates(null)
                    else -> {
                    }
                }
            }

            override fun recyclerViewUiEvents(uiEvent: RecyclerViewUiEvents) {
                when (uiEvent) {
                    is RecyclerViewUiEvents.RequestCurrentLocation -> currentLocation(uiEvent.uid)
                    is RecyclerViewUiEvents.RequestLocationByMap -> mapRequest(
                        uiEvent.uid,
                        uiEvent.featureType.name,
                        uiEvent.value,
                    )
                    else -> {
                    }
                }
            }
        }
    }
}
