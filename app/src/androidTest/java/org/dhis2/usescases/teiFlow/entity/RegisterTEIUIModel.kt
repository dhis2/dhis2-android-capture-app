package org.dhis2.usescases.teiFlow.entity

data class RegisterTEIUIModel(
    val name: String,
    val lastName: String,
    val firstSpecificDate: DateRegistration,
    val enrollmentDate: DateRegistration
)

data class DateRegistration(
    val year: Int,
    val month: Int,
    val day: Int
)
