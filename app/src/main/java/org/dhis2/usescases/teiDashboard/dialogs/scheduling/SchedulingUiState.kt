package org.dhis2.usescases.teiDashboard.dialogs.scheduling

import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCatCombo
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDate
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.program.ProgramStage

data class SchedulingUiState(
    val eventDate: EventDate = EventDate(),
    val eventCatCombo: EventCatCombo = EventCatCombo(),
    val programStage: ProgramStage? = null,
    val programStages: List<ProgramStage> = emptyList(),
    val enrollment: Enrollment? = null,
    val overdueEventSubtitle: String? = null,
    val programStageLabel: String? = null,
)
