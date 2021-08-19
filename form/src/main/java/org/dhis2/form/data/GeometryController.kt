package org.dhis2.form.data

import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.ui.RecyclerViewUiEvents
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
                    geometryParser.parsePoint(coordinates)
                )
            }
            FeatureType.POLYGON -> {
                GeometryHelper.createPolygonGeometry(
                    geometryParser.parsePolygon(coordinates)
                )
            }
            else -> {
                GeometryHelper.createMultiPolygonGeometry(
                    geometryParser.parseMultipolygon(coordinates)
                )
            }
        }
    }

    fun getCoordinatesCallback(
        onItemAction: (action: RowAction) -> Unit,
        currentLocation: (fieldUid: String) -> Unit,
        mapRequest: (fieldUid: String, featureType: String, initCoordinate: String?) -> Unit
    ): FieldUiModel.Callback {
        return object : FieldUiModel.Callback {
            override fun onNext() {
                TODO("Not yet implemented")
            }

            override fun intent(intent: FormIntent) {
                TODO("Not yet implemented")
            }

            override fun recyclerViewUiEvents(uiEvent: RecyclerViewUiEvents) {
                TODO("Not yet implemented")
            }

            override fun onItemAction(action: RowAction) {
                onItemAction(action)
            }

            override fun currentLocation(coordinateFieldUid: String) {
                currentLocation(coordinateFieldUid)
            }

            override fun mapRequest(
                coordinateFieldUid: String,
                featureType: String,
                initialCoordinates: String?
            ) {
                mapRequest(coordinateFieldUid, featureType, initialCoordinates)
            }
        }
    }
}
