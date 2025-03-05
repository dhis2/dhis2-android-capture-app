package org.dhis2.mobile.aggregates.model

internal data class DataSetDetails(
    val customTitle: DataSetCustomTitle,
    val dataSetTitle: String,
    val dateLabel: String,
    val orgUnitLabel: String,
    val catOptionComboLabel: String?,
)

data class DataSetCustomTitle(
    val header: String?,
    val subHeader: String?,
    val textAlignment: TextAlignment?,
    val isConfiguredTitle: Boolean,
)

enum class TextAlignment {
    LEFT, CENTER, RIGHT
}
