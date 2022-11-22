package org.dhis2.form.ui

import android.text.Editable
import android.text.TextWatcher
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper

class LatitudeLongitudeTextWatcher(val onValueChanged: (value: String?) -> Unit) {

    private var currentLatitude: String? = null
    private var currentLongitude: String? = null

    private val latitudeWatcher = object : TextWatcher {

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // Not needed
        }

        override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if (validateFilledCoordinates(text.toString(), currentLongitude ?: "")) {
                val lon = currentLongitude?.toDoubleOrNull()
                val latitude = text.toString().toDoubleOrNull()
                val value = if (lon != null && latitude != null) {
                    GeometryHelper.createPointGeometry(lon, latitude)?.coordinates()
                } else {
                    null
                }
                onValueChanged(value)
            }
        }

        override fun afterTextChanged(p0: Editable?) {
            // Not needed
        }
    }

    private val longitudeTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // Not needed
        }

        override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if (validateFilledCoordinates(currentLatitude ?: "", text.toString())) {
                val lon = text.toString().toDoubleOrNull()
                val latitude = currentLatitude?.toDoubleOrNull()
                val value = if (lon != null && latitude != null) {
                    GeometryHelper.createPointGeometry(lon, latitude)?.coordinates()
                } else {
                    null
                }
                onValueChanged(value)
            }
        }

        override fun afterTextChanged(p0: Editable?) {
            // Not needed
        }
    }

    private fun validateFilledCoordinates(lat: String, long: String) =
        lat.isNotEmpty() && long.isNotEmpty() ||
            lat.isEmpty() && long.isEmpty()

    fun latitudeWatcher(): TextWatcher {
        return latitudeWatcher
    }

    fun longitudeWatcher(): TextWatcher {
        return longitudeTextWatcher
    }

    fun resetCurrentValues(newLatitude: String? = null, newLongitude: String? = null) {
        currentLatitude = newLatitude
        currentLongitude = newLongitude
    }
}
