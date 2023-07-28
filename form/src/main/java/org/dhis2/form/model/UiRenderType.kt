package org.dhis2.form.model

enum class UiRenderType {
    DEFAULT,
    POINT,
    POLYGON,
    MULTI_POLYGON,
    VERTICAL_RADIOBUTTONS,
    HORIZONTAL_RADIOBUTTONS,
    VERTICAL_CHECKBOXES,
    HORIZONTAL_CHECKBOXES,
    AUTOCOMPLETE,
    MATRIX,
    SEQUENCIAL,
    QR_CODE,
    BAR_CODE,
    CANVAS;

    fun isRadioButton() = this == VERTICAL_RADIOBUTTONS || this == HORIZONTAL_RADIOBUTTONS
    fun isCheckBox() = this == VERTICAL_CHECKBOXES || this == HORIZONTAL_CHECKBOXES
    fun isVisualOptionSet() = this == MATRIX || this == SEQUENCIAL
    fun isPolygon() = this == POLYGON || this == MULTI_POLYGON
}
