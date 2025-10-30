package org.dhis2.usescases.enrollment

import org.dhis2.R
import org.dhis2.commons.resources.EventResourcesProvider
import org.dhis2.form.data.EnrollmentRepository
import org.dhis2.form.data.metadata.EnrollmentConfiguration

class DateEditionWarningHandler(
    private val conf: EnrollmentConfiguration?,
    private val eventResourcesProvider: EventResourcesProvider,
) {
    private var hasShownIncidentDateEditionWarning = false
    private var hasShownEnrollmentDateEditionWarning = false

    fun shouldShowWarning(
        fieldUid: String,
        showWarning: (message: String) -> Unit,
    ) {
        if (fieldUid == EnrollmentRepository.ENROLLMENT_DATE_UID &&
            conf?.hasEventsGeneratedByEnrollmentDate() == true &&
            !hasShownEnrollmentDateEditionWarning
        ) {
            hasShownEnrollmentDateEditionWarning = true
            showWarning(buildMessage())
        } else if (fieldUid == EnrollmentRepository.INCIDENT_DATE_UID &&
            conf?.hasEventsGeneratedByIncidentDate() == true &&
            !hasShownIncidentDateEditionWarning
        ) {
            hasShownIncidentDateEditionWarning = true
            showWarning(buildMessage())
        }
    }

    private fun buildMessage() =
        eventResourcesProvider.formatWithProgramEventLabel(
            R.string.enrollment_date_edition_warning_event_label,
            conf?.program()?.uid(),
            2,
        )
}
