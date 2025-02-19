package org.dhis2.mobile.aggregates.model

internal data class DataElementInfo(
    val label: String,
    val inputType: InputType,
    val description: String?,
    val isRequired: Boolean,
)
