package org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data

import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStage
import java.util.Date

data class EventViewModel(
    val type: EventViewModelType,
    val stage: ProgramStage?,
    val event: Event?,
    val eventCount: Int,
    val lastUpdate: Date?,
    val canAddNewEvent: Boolean
)