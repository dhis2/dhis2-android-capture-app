package org.dhis2.form.data

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry

class GeometryController {

    fun generateLocationFromCoordinates(featureType: FeatureType, coordinates: String?): Geometry? {
        if (coordinates == null) {
            return null
        }
        return when (featureType) {
            FeatureType.POINT -> {
                val type = object : TypeToken<List<Double?>?>() {}.type
                GeometryHelper.createPointGeometry(
                    Gson().fromJson(coordinates, type)
                )
            }
            FeatureType.POLYGON -> {
                val type = object : TypeToken<List<List<List<Double?>?>?>?>() {}.type
                GeometryHelper.createPolygonGeometry(
                    Gson().fromJson(coordinates, type)
                )
            }
            else -> {
                val type = object : TypeToken<List<List<List<List<Double?>?>?>?>?>() {}.type
                GeometryHelper.createMultiPolygonGeometry(
                    Gson().fromJson(coordinates, type)
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
