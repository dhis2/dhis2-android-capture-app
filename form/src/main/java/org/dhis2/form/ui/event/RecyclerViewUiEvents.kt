package org.dhis2.form.ui.event

import java.util.Date
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ValueTypeRenderingType

sealed class RecyclerViewUiEvents {

    data class OpenYearMonthDayAgeCalendar(
        val uid: String,
        val year: Int,
        val month: Int,
        val day: Int
    ) : RecyclerViewUiEvents()

    data class OpenCustomCalendar(
        val uid: String,
        val label: String,
        val date: Date?,
        val allowFutureDates: Boolean,
        val isDateTime: Boolean? = false
    ) : RecyclerViewUiEvents()

    data class OpenTimePicker(
        val uid: String,
        val label: String,
        val date: Date?,
        val isDateTime: Boolean? = false
    ) : RecyclerViewUiEvents()

    data class ShowDescriptionLabelDialog(
        val title: String,
        val message: String?
    ) : RecyclerViewUiEvents()

    data class RequestCurrentLocation(
        val uid: String
    ) : RecyclerViewUiEvents()

    data class RequestLocationByMap(
        val uid: String,
        val featureType: FeatureType,
        val value: String?
    ) : RecyclerViewUiEvents()

    data class ScanQRCode(
        val uid: String,
        val optionSet: String?,
        val renderingType: ValueTypeRenderingType?
    ) : RecyclerViewUiEvents()

    data class DisplayQRCode(
        val uid: String,
        val optionSet: String?,
        val value: String,
        val renderingType: ValueTypeRenderingType?,
        val editable: Boolean
    ) : RecyclerViewUiEvents()
}
