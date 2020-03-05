package org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data

import io.reactivex.Single
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.tei_events.EventViewModel
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.DatePeriod
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

interface TeiDataRepository {
    fun getTEIEnrollmentEvents(
        selectedStage: String?,
        groupedByStage: Boolean,
        periodFilters: MutableList<DatePeriod>,
        orgUnitFilters: MutableList<String>,
        stateFilters: MutableList<State>,
        assignedToMe: Boolean,
        eventStatusFilters: MutableList<EventStatus>,
        catOptComboFilters: MutableList<CategoryOptionCombo>
    ): Single<List<EventViewModel>>

    fun getEnrollment(): Single<Enrollment>
    fun getEnrollmentProgram(): Single<Program>
    fun getTrackedEntityInstance(): Single<TrackedEntityInstance>
    fun enrollingOrgUnit(): Single<OrganisationUnit>
}