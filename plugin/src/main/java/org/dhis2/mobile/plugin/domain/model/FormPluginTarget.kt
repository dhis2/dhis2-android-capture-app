package org.dhis2.mobile.plugin.domain.model

/** The record a data entry form is editing, which decides the plugins it shows. */
sealed interface FormPluginTarget {
    data class Event(
        val eventUid: String,
    ) : FormPluginTarget

    data class Enrollment(
        val enrollmentUid: String,
    ) : FormPluginTarget
}
