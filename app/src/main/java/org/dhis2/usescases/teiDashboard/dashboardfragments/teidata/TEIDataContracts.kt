package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import android.content.Intent
import android.os.Bundle
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.StageSection
import org.dhis2.commons.filters.FilterItem
import org.dhis2.commons.filters.FilterManager.PeriodRequest
import org.dhis2.usescases.general.AbstractActivityContracts
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

class TEIDataContracts {
    interface View : AbstractActivityContracts.View {
        fun hideDueDate()
        fun setEvents(events: List<EventViewModel>, canAddEvents: Boolean)
        fun displayGenerateEvent(): Consumer<ProgramStage>
        fun areEventsCompleted(): Consumer<Single<Boolean>>
        fun enrollmentCompleted(): Consumer<EnrollmentStatus>
        fun switchFollowUp(followUp: Boolean)
        fun displayGenerateEvent(eventUid: String)
        fun restoreAdapter(programUid: String, teiUid: String, enrollmentUid: String)
        fun seeDetails(intent: Intent, bundle: Bundle)
        fun openEventDetails(intent: Intent, bundle: Bundle)
        fun openEventInitial(intent: Intent)
        fun openEventCapture(intent: Intent)
        fun showTeiImage(fileName: String, defaultIcon: String)
        fun setFilters(filterItems: MutableList<FilterItem>)
        fun hideFilters()
        fun observeStageSelection(
            currentProgram: Program,
            currentEnrollment: Enrollment
        ): Flowable<StageSection>

        fun showNewEventOptions(view: android.view.View, stageUid: ProgramStage)
        fun setEnrollmentData(program: Program?, enrollment: Enrollment?)
        fun setTrackedEntityInstance(
            trackedEntityInstance: TrackedEntityInstance,
            organisationUnit: OrganisationUnit
        )

        fun showPeriodRequest(periodRequest: PeriodRequest)
        fun openOrgUnitTreeSelector(programUid: String)
        fun setEnrollment(enrollment: Enrollment)
        fun showSyncDialog(eventUid: String, enrollmentUid: String)
        fun displayCatComboOptionSelectorForEvents(data: List<EventViewModel>)

        fun showProgramRuleErrorMessage(message: String)
    }
}
