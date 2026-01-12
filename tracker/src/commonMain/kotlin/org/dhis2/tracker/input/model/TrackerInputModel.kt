package org.dhis2.tracker.input.model

data class TrackerInputModel(
    val uid: String,
    val label: String,
    val value: String?,
    val focused: Boolean,
    val valueType: TrackerInputType,
    val optionSet: String?,
    val onItemClick: () -> Unit,
    val onValueChange: (String?) -> Unit,
)
