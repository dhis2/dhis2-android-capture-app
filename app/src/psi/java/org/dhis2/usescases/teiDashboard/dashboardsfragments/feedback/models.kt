package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

enum class ProgramType { HNQIS, RDQA }

data class FeedbackProgram(val uid: String, val programType: ProgramType)

data class FeedbackItemValue(val data: String?, val color: String)

data class FeedbackItem(val name: String, val value: FeedbackItemValue? = null)

data class FeedbackHelpItem(val text: String, var showingAll: Boolean = false)
