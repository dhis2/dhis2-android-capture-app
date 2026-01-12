package org.dhis2.tracker.search.ui.model

data class ParameterInputModel(
    val uid: String,
    val label: String,
    val value: String?,
    val focused: Boolean,
    val valueType: ParameterInputType,
    val optionSet: String?,
    val onItemClick: () -> Unit,
    val onValueChange: (String?) -> Unit,
)
