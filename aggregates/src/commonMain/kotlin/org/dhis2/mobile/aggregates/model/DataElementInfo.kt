package org.dhis2.mobile.aggregates.model

import org.dhis2.mobile.commons.input.InputType

internal data class DataElementInfo(
    val label: String,
    val inputType: InputType,
    val description: String?,
    val isRequired: Boolean,
)
