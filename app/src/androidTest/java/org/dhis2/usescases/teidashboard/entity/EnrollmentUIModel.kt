package org.dhis2.usescases.teidashboard.entity

data class EnrollmentUIModel(
    val enrollmentDate: String,
    val birthday: String,
    val orgUnit: String,
    val latitude: String,
    val longitude: String,
    val name: String,
    val lastName: String,
    val sex: String
)
