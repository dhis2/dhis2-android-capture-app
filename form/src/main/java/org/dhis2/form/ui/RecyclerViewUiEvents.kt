package org.dhis2.form.ui

import org.hisp.dhis.android.core.common.FeatureType

sealed class RecyclerViewUiEvents {

    data class OpenYearMonthDayAgeCalendar(
        val uid: String,
        val year: Int,
        val month: Int,
        val day: Int
    ) : RecyclerViewUiEvents()

    data class OpenCustomAgeCalendar(
        val uid: String,
        val label: String
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
}
