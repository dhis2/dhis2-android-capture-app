package org.dhis2.tracker.search.model

import org.dhis2.tracker.input.model.TrackerInputType

data class SearchParameterModel(
    val uid: String,
    val label: String,
    val inputType: TrackerInputType,
    val optionSet: String?,
    val customIntentUid: String? = null,
    val minCharactersToSearch: Int?,
    val searchOperator: SearchOperator?,
    val isUnique: Boolean,
)
