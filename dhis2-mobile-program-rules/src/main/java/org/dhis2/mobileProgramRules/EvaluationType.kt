package org.dhis2.mobileProgramRules

sealed class EvaluationType(val targetUid: String) {
    data class Enrollment(private val enrollmentUid: String) : EvaluationType(enrollmentUid)
    data class Event(private val eventUid: String) : EvaluationType(eventUid)
}
