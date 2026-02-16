package org.dhis2.tracker.search.model

import org.dhis2.tracker.ui.input.model.TrackerInputType

data class SearchParameterModel(
    val uid: String,
    val label: String,
    val inputType: TrackerInputType,
    val optionSet: String?,
    // val legend: LegendData?, TODO(CHECK)
    // val optionSetConfiguration: TrackerOptionSetConfiguration? = null, TODO(Maybe it's better with a ViewModel)
    val customIntentUid: String? = null,
    val minCharactersToSearch: Int?,
    val searchOperator: SearchOperator?,
    val isUnique: Boolean,
)
