package org.dhis2.mobile.aggregates.model

internal data class DataSetDetails(
    val customTitle: DataSetCustomTitle,
    val dataSetTitle: String,
    val dateLabel: String,
    val orgUnitLabel: String,
    val catOptionComboLabel: String?,
    val isCompleted: Boolean,
    val edition: DataSetEdition,
)

data class DataSetCustomTitle(
    val header: String?,
    val subHeader: String?,
    val textAlignment: TextAlignment?,
    val isConfiguredTitle: Boolean,
)

data class DataSetEdition(
    val editable: Boolean,
    val nonEditableReason: NonEditableReason,
)

sealed class NonEditableReason {
    data object None : NonEditableReason()

    data object NoDataWriteAccess : NonEditableReason()

    data class NoAttributeOptionComboAccess(
        val attributeOptionComboLabel: String,
    ) : NonEditableReason()

    data class OrgUnitNotInCaptureScope(
        val orgUnitLabel: String,
    ) : NonEditableReason()

    data class AttributeOptionComboNotAssignedToOrgUnit(
        val attributeOptionComboLabel: String,
        val orgUnitLabel: String,
    ) : NonEditableReason()

    data class PeriodIsNotInOrgUnitRange(
        val periodLabel: String,
        val orgUnitLabel: String,
    ) : NonEditableReason()

    data class PeriodIsNotInAttributeOptionComboRange(
        val periodLabel: String,
        val attributeOptionComboLabel: String,
    ) : NonEditableReason()

    data object Closed : NonEditableReason()

    data object Expired : NonEditableReason()
}

enum class TextAlignment {
    LEFT,
    CENTER,
    RIGHT,
}
