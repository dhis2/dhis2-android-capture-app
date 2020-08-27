package org.dhis2.usescases.teiDashboard.dashboardsfragments.feedback

enum class ProgramType { HNQIS, RDQA }

data class FeedbackProgram(val uid: String, val programType: ProgramType) {}