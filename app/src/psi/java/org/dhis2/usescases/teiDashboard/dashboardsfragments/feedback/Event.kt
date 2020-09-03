package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

data class Value(
    val name: String,
    val value: String,
    val colorByLegend: String? = null,
    val feedbackHelp: String? = null,
    val feedbackOrder: String? = null
)

data class Event(val name: String, val values: List<Value>)