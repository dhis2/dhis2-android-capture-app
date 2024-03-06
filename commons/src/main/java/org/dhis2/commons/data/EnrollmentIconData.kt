package org.dhis2.commons.data

data class EnrollmentIconData(
    val color: Int,
    val imageResource: Int,
    val isIcon: Boolean,
    val remainingEnrollments: Int
)
