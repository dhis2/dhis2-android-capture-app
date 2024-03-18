package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import android.content.Intent
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.LifecycleOwner
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.StageSection
import org.dhis2.usescases.general.AbstractActivityContracts
import org.dhis2.utils.EventMode
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage

class TEIDataContracts {
    interface View : AbstractActivityContracts.View {
        fun viewLifecycleOwner(): LifecycleOwner
        fun setEvents(events: List<EventViewModel>)
        fun displayScheduleEvent()
        fun showDialogCloseProgram()
        fun areEventsCompleted(): Consumer<Single<Boolean>>
        fun switchFollowUp(followUp: Boolean)
        fun displayGenerateEvent(eventUid: String)
        fun restoreAdapter(programUid: String, teiUid: String, enrollmentUid: String)
        fun openEventDetails(intent: Intent, options: ActivityOptionsCompat)
        fun openEventInitial(intent: Intent)
        fun openEventCapture(intent: Intent)
        fun observeStageSelection(
            currentProgram: Program,
            currentEnrollment: Enrollment,
        ): Flowable<StageSection>

        fun setEnrollmentData(program: Program?, enrollment: Enrollment?)

        fun showSyncDialog(eventUid: String, enrollmentUid: String)
        fun displayCatComboOptionSelectorForEvents(data: List<EventViewModel>)

        fun showProgramRuleErrorMessage()
        fun goToEventInitial(eventCreationType: EventCreationType, programStage: ProgramStage)
        fun updateEnrollment(update: Boolean)
        fun displayOrgUnitSelectorForNewEvent(programUid: String, programStageUid: String)

        fun goToEventDetails(
            eventUid: String,
            eventMode: EventMode,
            programUid: String,
        )
    }
}
