package org.dhis2.form.ui.binding

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputEditText
import org.dhis2.commons.extensions.closeKeyboard
import org.dhis2.form.R
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiRenderType
import org.dhis2.form.ui.intent.FormIntent.SaveCurrentLocation
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry

@BindingAdapter(value = ["latitude_validator", "longitude"])
fun EditText.setLatitudeValidator(
    viewModel: FieldUiModel?,
    longitudeEditText: TextInputEditText?
) {
    doOnTextChanged { text, start, before, count ->
        if (validateFilledCoordinates(
            this.text.toString(),
            longitudeEditText?.text?.toString() ?: ""
        )
        ) {
            val lon = longitudeEditText?.text?.toString()?.toDoubleOrNull()
            val latitude = this.text.toString().toDoubleOrNull()
            val value = if (lon != null && latitude != null) {
                GeometryHelper.createPointGeometry(lon, latitude)?.coordinates()
            } else {
                null
            }
            viewModel?.onTextChange(value)
        }
    }
}

@BindingAdapter(value = ["longitude_validator", "latitude"])
fun EditText.setLongitudeValidator(
    viewModel: FieldUiModel?,
    latitudeEditText: TextInputEditText?
) {
    doOnTextChanged { text, start, before, count ->
        if (validateFilledCoordinates(
            latitudeEditText?.text?.toString() ?: "",
            this.text.toString()
        )
        ) {
            val lon = this.text.toString().toDoubleOrNull()
            val latitude = latitudeEditText?.text?.toString()?.toDoubleOrNull()
            val value = if (lon != null && latitude != null) {
                GeometryHelper.createPointGeometry(lon, latitude)?.coordinates()
            } else {
                null
            }
            viewModel?.onTextChange(value)
        }
    }
}

@BindingAdapter("field_edition_alpha")
fun View.setFieldEdition(isFieldEditable: Boolean) {
    alpha = if (isFieldEditable) 1f else 0.5f
}

@BindingAdapter("geometry_latitude_value")
fun TextInputEditText.setGeometryLatitudeValue(item: FieldUiModel?) {
    item?.let {
        val latitudeValue = when (it.renderingType) {
            UiRenderType.POINT -> getCurrentGeometry(it)?.let { geometry ->
                GeometryHelper.getPoint(geometry)[1].toString()
            }
            else -> null
        }
        setText(latitudeValue)
        setSelection(latitudeValue?.length ?: 0)
    }
}

@BindingAdapter("geometry_longitude_value")
fun TextInputEditText.setGeometryLongitudeValue(item: FieldUiModel?) {
    item?.let {
        val latitudeValue = when (it.renderingType) {
            UiRenderType.POINT -> getCurrentGeometry(it)?.let { geometry ->
                GeometryHelper.getPoint(geometry)[0].toString()
            }
            else -> null
        }
        setText(latitudeValue)
        if (hasFocus()) setSelection(latitudeValue?.length ?: 0)
        setSelection(latitudeValue?.length ?: 0)
    }
}

private fun getCurrentGeometry(item: FieldUiModel): Geometry? {
    return item.value?.let {
        val featureType = getFeatureType(item.renderingType)
        Geometry.builder()
            .coordinates(it)
            .type(featureType)
            .build()
    }
}

fun getFeatureType(renderingType: UiRenderType?): FeatureType {
    return when (renderingType) {
        UiRenderType.DEFAULT -> FeatureType.NONE
        UiRenderType.POINT -> FeatureType.POINT
        UiRenderType.POLYGON -> FeatureType.POLYGON
        UiRenderType.MULTI_POLYGON -> FeatureType.MULTI_POLYGON
        else -> FeatureType.NONE
    }
}

@BindingAdapter("geometry_polygon_value")
fun TextInputEditText.setGeometryPolygonValue(item: FieldUiModel?) {
    item?.let {
        val latitudeValue = if (it.value != null && it.renderingType?.isPolygon() == true) {
            context.getString(R.string.polygon_captured)
        } else {
            null
        }

        setText(latitudeValue)
    }
}

@BindingAdapter(
    value = [
        "geometry_editor_latitude_listener",
        "geometry_editor_latitude_listener_view",
        "error_view"
    ],
    requireAll = true
)
fun TextInputEditText.setOnGeometryLatitudeEditorActionListener(
    item: FieldUiModel?,
    editTextToActivate: TextInputEditText,
    errorText: TextView
) {
    setOnEditorActionListener { _, _, _ ->
        onFilledCoordinate(
            context = context,
            lat = this.text.toString(),
            long = editTextToActivate.text.toString(),
            item = item,
            errorTextView = errorText
        )
        editTextToActivate.requestFocus()
        editTextToActivate.performClick()
        true
    }
}

@BindingAdapter(
    value = [
        "geometry_editor_longitude_listener",
        "geometry_editor_longitude_listener_view",
        "error_view"
    ],
    requireAll = true
)
fun TextInputEditText.setOnGeometryLongitudeEditorActionListener(
    item: FieldUiModel?,
    editTextToActivate: TextInputEditText,
    errorText: TextView
) {
    setOnEditorActionListener { _, _, _ ->
        val result = onFilledCoordinate(
            context = context,
            lat = editTextToActivate.text.toString(),
            long = this.text.toString(),
            item = item,
            errorTextView = errorText
        )
        if (result) {
            closeKeyboard()
            clearFocus()
            item?.onNext()
        } else {
            editTextToActivate.requestFocus()
            editTextToActivate.performClick()
        }
        true
    }
}

private fun onFilledCoordinate(
    context: Context,
    lat: String,
    long: String,
    item: FieldUiModel?,
    errorTextView: TextView
): Boolean {
    return if (validateFilledCoordinates(lat, long)) {
        val doubleLat = lat.toDoubleOrNull()
        val doubleLong = long.toDoubleOrNull()
        if (doubleLat != null && doubleLong != null) {
            if (areLngLatCorrect(doubleLong, doubleLat)) {
                item?.invokeIntent(
                    SaveCurrentLocation(
                        uid = item.uid,
                        value = GeometryHelper.createPointGeometry(doubleLong, doubleLat)
                            ?.coordinates(),
                        featureType = getFeatureType(item.renderingType).name
                    )
                )
            } else {
                errorTextView.apply {
                    text = context.getString(R.string.coordinates_error)
                    visibility = View.VISIBLE
                    setTextColor(resources.getColor(R.color.error_color))
                }
                return false
            }
        } else {
            item?.invokeIntent(
                SaveCurrentLocation(
                    uid = item.uid,
                    value = null,
                    featureType = getFeatureType(item.renderingType).name
                )
            )
        }
        true
    } else {
        false
    }
}

fun validateFilledCoordinates(lat: String, long: String) =
    lat.isNotEmpty() && long.isNotEmpty() ||
        lat.isEmpty() && long.isEmpty()

fun areLngLatCorrect(lon: Double, lat: Double) = isLatitudeValid(lat) && isLongitudeValid(lon)

fun isLongitudeValid(longitude: Double) = longitude >= -180 && longitude <= 180

fun isLatitudeValid(latitude: Double) = latitude >= -90 && latitude <= 90
