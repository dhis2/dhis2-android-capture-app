package org.dhis2.data.forms.dataentry.fields.coordinate

import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputEditText
import org.dhis2.Bindings.closeKeyboard
import org.dhis2.R
import org.dhis2.utils.ColorUtils
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry

@BindingAdapter("latitude_validator")
fun EditText.setLatitudeValidator(viewModel: CoordinateViewModel?) {
    setOnFocusChangeListener { view, hasFocus ->
        viewModel?.onLatitudeFocusChanged(
            hasFocus,
            text.toString(),
            context.getString(R.string.coordinates_error)
        ) { latitudeValue ->
            setText(latitudeValue)
        }
    }
}

@BindingAdapter("longitude_validator")
fun EditText.setLongitudeValidator(viewModel: CoordinateViewModel?) {
    setOnFocusChangeListener { view, hasFocus ->
        viewModel?.onLongitudeFocusChanged(
            hasFocus,
            text.toString(),
            context.getString(R.string.coordinates_error)
        ) { longitudeValue ->
            setText(longitudeValue)
        }
    }
}

@BindingAdapter(value = ["error_message", "warning_message"], requireAll = true)
fun TextView.setErrorMessage(errorMessage: String?, warningMessage: String?) {
    when {
        errorMessage == null && warningMessage == null -> visibility = View.GONE
        errorMessage != null -> {
            setTextColor(ContextCompat.getColor(context, R.color.error_color))
            text = errorMessage
            visibility = View.VISIBLE
        }
        warningMessage != null -> {
            setTextColor(ContextCompat.getColor(context, R.color.warning_color))
            text = warningMessage
            visibility = View.VISIBLE
        }
    }
}

@BindingAdapter("field_edition_alpha")
fun View.setFieldEdition(isFieldEditable: Boolean) {
    alpha = if (isFieldEditable) 1f else 0.5f
}

@BindingAdapter("input_text_color")
fun TextInputEditText.setInputTextColor(useDefault: Boolean) {
    setTextColor(
        if (useDefault) {
            ContextCompat.getColor(context, R.color.textPrimary)
        } else {
            ColorUtils.getPrimaryColor(context, ColorUtils.ColorType.ACCENT)
        }
    )
}

@BindingAdapter("geometry_latitude_value")
fun TextInputEditText.setGeometryLatitudeValue(geometry: Geometry?) {
    val latitudeValue = when (geometry?.type()) {
        FeatureType.POINT -> GeometryHelper.getPoint(geometry)[1].toString()
        else -> null
    }
    setText(latitudeValue)
}

@BindingAdapter("geometry_longitude_value")
fun TextInputEditText.setGeometryLongitudeValue(geometry: Geometry?) {
    val latitudeValue = when (geometry?.type()) {
        FeatureType.POINT -> GeometryHelper.getPoint(geometry)[0].toString()
        else -> null
    }
    setText(latitudeValue)
}

@BindingAdapter("geometry_polygon_value")
fun TextInputEditText.setGeometryPolygonValue(geometry: Geometry?) {
    val latitudeValue = when (geometry?.type()) {
        FeatureType.POLYGON, FeatureType.MULTI_POLYGON ->
            context.getString(R.string.polygon_captured)
        else -> null
    }
    setText(latitudeValue)
}

@BindingAdapter(
    value = ["geometry_editor_listener", "geometry_editor_listener_view"],
    requireAll = true
)
fun TextInputEditText.setOnGeometryEditorActionListener(
    viewModel: CoordinateViewModel,
    editTextToActivate: TextInputEditText
) {
    setOnEditorActionListener { _, _, _ ->
        val result = viewModel.onFilledCoordinate(context.getString(R.string.coordinates_error))
        if (result) {
            closeKeyboard()
        } else {
            editTextToActivate.requestFocus()
            editTextToActivate.performClick()
        }
        true
    }
}
