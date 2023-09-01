package org.dhis2.usescases.teiDashboard.domain

import org.dhis2.R
import org.dhis2.commons.data.EventCreationType.ADDNEW
import org.dhis2.commons.data.EventCreationType.REFERAL
import org.dhis2.commons.data.EventCreationType.SCHEDULE
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.teiDashboard.ui.EventCreationOptions
import org.hisp.dhis.android.core.program.ProgramStage

class GetNewEventCreationTypeOptions(
    val resources: ResourceManager,
) {

    operator fun invoke(programStage: ProgramStage): List<EventCreationOptions> {
        /*if (programStage.hideDueDate() != null && programStage.hideDueDate()) {
            //TODO Not add SCHEDULE event type
        }
        //TODO check refereal with configuration*/
        return listOf(
            EventCreationOptions(
                SCHEDULE,
                resources.getString(R.string.schedule_new),
            ),
            EventCreationOptions(
                ADDNEW,
                resources.getString(R.string.add_new),
            ),
            EventCreationOptions(
                REFERAL,
                resources.getString(R.string.referral),
            ),
        )
    }
}
