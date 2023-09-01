package org.dhis2.usescases.teiDashboard.domain

import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.data.EventCreationType.ADDNEW
import org.dhis2.commons.data.EventCreationType.REFERAL
import org.dhis2.commons.data.EventCreationType.SCHEDULE
import org.dhis2.usescases.teiDashboard.data.ProgramConfigurationRepository
import org.hisp.dhis.android.core.program.ProgramStage

class GetNewEventCreationTypeOptions(
    private val programConfigurationRepository: ProgramConfigurationRepository,
) {

    operator fun invoke(
        programStage: ProgramStage?,
        programUid: String,
    ): List<EventCreationType> {
        val options: MutableList<EventCreationType> = mutableListOf()

        programStage?.let {
            if (shouldShowScheduleEvents(it)) {
                options.add(SCHEDULE)
            }
        } ?: options.add(SCHEDULE)

        options.add(ADDNEW)

        if (shouldShowReferralEvents(programUid)) {
            options.add(REFERAL)
        }
        return options
    }

    private fun shouldShowReferralEvents(programUid: String): Boolean {
        programConfigurationRepository.getConfigurationByProgram(programUid)
            ?.let { programConfiguration ->
                return programConfiguration.disableReferrals() != true
            }
        return true
    }

    private fun shouldShowScheduleEvents(programStage: ProgramStage) =
        programStage.hideDueDate() != true
}
