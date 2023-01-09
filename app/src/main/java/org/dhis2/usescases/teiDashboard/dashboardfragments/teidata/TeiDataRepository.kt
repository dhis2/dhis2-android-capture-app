package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata

import io.reactivex.Single
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.StageSection
import org.dhis2.commons.filters.sorting.SortingItem
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
        selectedStage: StageSection,
        groupedByStage: Boolean,
        periodFilters: MutableList<DatePeriod>,
        orgUnitFilters: MutableList<String>,
        stateFilters: MutableList<State>,
        assignedToMe: Boolean,
        eventStatusFilters: MutableList<EventStatus>,
        catOptComboFilters: MutableList<CategoryOptionCombo>,
        sortingItem: SortingItem?
    ): Single<List<EventViewModel>>

    fun getEnrollment(): Single<Enrollment>
    fun getEnrollmentProgram(): Single<Program>
    fun getTrackedEntityInstance(): Single<TrackedEntityInstance>
    fun enrollingOrgUnit(): Single<OrganisationUnit>
    fun eventsWithoutCatCombo(): Single<List<EventViewModel>>
}
