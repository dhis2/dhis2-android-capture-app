package org.dhis2.usescases.teiDashboard.dashboardsfragments.enrollment

import java.util.Date

data class EnrollmentInfo(
    val enrollmentUid: String,
    val programUid: String,
    val enrollmentDate: Date,
    val programName: String
)