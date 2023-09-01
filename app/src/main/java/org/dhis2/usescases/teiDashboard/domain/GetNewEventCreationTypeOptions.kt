package org.dhis2.usescases.teiDashboard.domain

import org.dhis2.R
import org.dhis2.commons.data.EventCreationType.ADDNEW
import org.dhis2.commons.data.EventCreationType.REFERAL
import org.dhis2.commons.data.EventCreationType.SCHEDULE
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.teiDashboard.data.ProgramConfigurationRepository
import org.dhis2.usescases.teiDashboard.ui.EventCreationOptions
import org.hisp.dhis.android.core.program.ProgramStage

class GetNewEventCreationTypeOptions(
    private val resources: ResourceManager,
    private val programConfigurationRepository: ProgramConfigurationRepository,
) {

    operator fun invoke(programStage: ProgramStage): List<EventCreationOptions> {
        val options: MutableList<EventCreationOptions> = mutableListOf()

        if (shouldShowScheduleEvents(programStage)) {
            options.add(
                EventCreationOptions(
                    SCHEDULE,
                    resources.getString(R.string.schedule_new),
                ),
            )
        }

        options.add(
            EventCreationOptions(
                ADDNEW,
                resources.getString(R.string.add_new),
            ),
        )

        if (shouldShowReferralEvents(programStage)) {
            options.add(
                EventCreationOptions(
                    REFERAL,
                    resources.getString(R.string.referral),
                ),
            )
        }
        return options
    }

    private fun shouldShowReferralEvents(programStage: ProgramStage): Boolean {
        programStage.program()?.uid()?.let { uid ->
            programConfigurationRepository.getConfigurationByProgram(uid)
                ?.let { programConfiguration ->
                    return programConfiguration.disableReferrals() != true
                }
        }
        return true
    }

    private fun shouldShowScheduleEvents(programStage: ProgramStage) =
        programStage.hideDueDate() != true
}
