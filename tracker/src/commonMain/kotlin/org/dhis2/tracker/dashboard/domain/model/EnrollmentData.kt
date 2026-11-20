package org.dhis2.tracker.dashboard.domain.model

data class EnrollmentData(
    val enrollmentUid: String,
    val programUid: String,
    val attributeOptionCombo: String,
)
