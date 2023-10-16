package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import android.content.Intent
import androidx.core.app.ActivityOptionsCompat
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.StageSection
import org.dhis2.commons.filters.FilterItem
import org.dhis2.commons.filters.FilterManager.PeriodRequest
import org.dhis2.usescases.general.AbstractActivityContracts
import org.dhis2.usescases.teiDashboard.DashboardProgramModel
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import java.util.Date

class TEIDataContracts {
    interface View : AbstractActivityContracts.View {
        fun setEvents(events: List<EventViewModel>, canAddEvents: Boolean)

        fun showCatComboDialog(eventUid: String?, eventDate: Date?, categoryComboUid: String?)
        fun displayGenerateEvent(): Consumer<ProgramStage>
        fun areEventsCompleted(): Consumer<Single<Boolean>>
        fun enrollmentCompleted(): Consumer<EnrollmentStatus>
        fun switchFollowUp(followUp: Boolean)
        fun displayGenerateEvent(eventUid: String)
        fun restoreAdapter(programUid: String, teiUid: String, enrollmentUid: String)
        fun setAttributeValues(attributeValues: List<TrackedEntityAttributeValue?>?)
        fun seeDetails(intent: Intent, options: ActivityOptionsCompat)
        fun openEventDetails(intent: Intent, options: ActivityOptionsCompat)
        fun openEventInitial(intent: Intent)
        fun openEventCapture(intent: Intent)
        fun showTeiImage(filePath: String, defaultIcon: String)
        fun setFilters(filterItems: List<FilterItem>)

        fun setRiskColor(risk: String?)

        fun setProgramAttributes(programTrackedEntityAttributes: List<ProgramTrackedEntityAttribute?>?)
        fun hideFilters()
        fun observeStageSelection(
            currentProgram: Program,
            currentEnrollment: Enrollment,
        ): Flowable<StageSection>

        fun setEnrollmentData(program: Program?, enrollment: Enrollment?)
        fun setTrackedEntityInstance(trackedEntityInstance: TrackedEntityInstance?, organisationUnit: OrganisationUnit?, trackedEntityAttributeValues: List<TrackedEntityAttributeValue?>?)

        fun showPeriodRequest(periodRequest: PeriodRequest)
        fun openOrgUnitTreeSelector(programUid: String)
        fun setEnrollment(enrollment: Enrollment)
        fun showSyncDialog(eventUid: String, enrollmentUid: String)
        fun displayCatComboOptionSelectorForEvents(data: List<EventViewModel>)

        fun showProgramRuleErrorMessage(message: String)
        fun showCatOptComboDialog(catComboUid: String)
        fun goToEventInitial(eventCreationType: EventCreationType, programStage: ProgramStage)
    }

    interface Presenter : AbstractActivityContracts.Presenter {
        fun init()
        fun setProgram(program: Program?, enrollmentUid: String?)
        fun showDescription(description: String?)
        fun getEnrollment(enrollmentUid: String?)
        fun setOpeningFilterToNone()
        fun setOrgUnitFilters(selectedOrgUnits: List<OrganisationUnit?>?)
    }
}
