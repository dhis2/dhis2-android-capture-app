package org.dhis2.usescases.teidashboard.dashboardfragments.teidata

import io.reactivex.Single
import org.dhis2.commons.data.EventViewModel
import org.dhis2.commons.data.StageSection
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

interface TeiDataRepository {
    fun getTEIEnrollmentEvents(
        selectedStage: StageSection,
        groupedByStage: Boolean,
    ): Single<List<EventViewModel>>

    fun getEnrollment(): Single<Enrollment?>
    fun getEnrollmentProgram(): Single<Program?>
    fun getTrackedEntityInstance(): Single<TrackedEntityInstance?>
    fun enrollingOrgUnit(): Single<OrganisationUnit>
    fun eventsWithoutCatCombo(): Single<List<EventViewModel>>
    fun getOrgUnitName(orgUnitUid: String): String
    fun getAttributeValues(teiUid: String): List<TrackedEntityAttributeValue>
    fun getTeiProfilePath(): String?
    fun getTeiHeader(): String?
}
