package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

enum class ProgramType { HNQIS, RDQA }

data class FeedbackProgram(val uid: String, val programType: ProgramType)

data class FeedbackItemValue(
    val data: String?,
    val color: String?,
    val success: Boolean,
    val critical: Boolean,
    val isNumeric: Boolean
)

data class FeedbackItem(
    val name: String,
    val value: FeedbackItemValue? = null,
    val code: String
)

data class FeedbackHelpItem(val text: String, var showingAll: Boolean = false)
