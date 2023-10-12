package org.dhis2.form.ui.event

import java.util.Date
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiRenderType
import org.hisp.dhis.android.core.common.FeatureType

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
        val renderingType: UiRenderType?
    ) : RecyclerViewUiEvents()

    data class DisplayQRCode(
        val uid: String,
        val optionSet: String?,
        val value: String,
        val renderingType: UiRenderType?,
        val editable: Boolean
    ) : RecyclerViewUiEvents()

    data class OpenOrgUnitDialog(
        val uid: String,
        val label: String,
        val value: String?
    ) : RecyclerViewUiEvents()

    data class AddImage(
        val uid: String
    ) : RecyclerViewUiEvents()

    data class AddSignature(
        val uid: String,
        val label: String
    ) : RecyclerViewUiEvents()

    data class ShowImage(
        val label: String,
        val value: String
    ) : RecyclerViewUiEvents()

    data class CopyToClipboard(
        val value: String?
    ) : RecyclerViewUiEvents()

    data class OpenOptionSetDialog(
        val field: FieldUiModel
    ) : RecyclerViewUiEvents()

    data class OpenFileSelector(
        val field: FieldUiModel
    ) : RecyclerViewUiEvents()

    data class OpenFile(
        val field: FieldUiModel
    ) : RecyclerViewUiEvents()

    data class OpenChooserIntent(
        val action: String,
        val value: String?,
        val uid: String
    ) : RecyclerViewUiEvents()
}
