package org.dhis2.usescases.teiDashboard

import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import org.dhis2.commons.data.tuples.Pair
import org.dhis2.ui.MetadataIconData
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.relationship.RelationshipType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

interface DashboardRepository {

    fun getTeiHeader(): String?

    fun getTeiProfilePath(): String?

    fun getProgramStages(programStages: String): Observable<List<ProgramStage>>

    fun getEnrollment(): Observable<Enrollment>

    fun getTEIEnrollmentEvents(programUid: String?, teiUid: String): Observable<List<Event>>

    fun getEnrollmentEventsWithDisplay(
        programUid: String?,
        teiUid: String,
    ): Observable<List<Event>>

    fun getTEIAttributeValues(
        programUid: String?,
        teiUid: String,
    ): Observable<List<TrackedEntityAttributeValue>>

    fun setFollowUp(enrollmentUid: String?): Boolean

    fun updateState(event: Event?, newStatus: EventStatus): Event

    fun completeEnrollment(enrollmentUid: String): Flowable<Enrollment>

    fun displayGenerateEvent(eventUid: String?): Observable<ProgramStage>

    fun relationshipsForTeiType(
        teType: String,
    ): Observable<List<Pair<RelationshipType?, String>>>

    fun catOptionCombo(catComboUid: String?): CategoryOptionCombo

    fun getTrackedEntityInstance(
        teiUid: String,
    ): Observable<TrackedEntityInstance>

    fun getProgramTrackedEntityAttributes(
        programUid: String?,
    ): Observable<List<ProgramTrackedEntityAttribute>>

    fun getTeiOrgUnits(
        teiUid: String,
        programUid: String?,
    ): Observable<List<OrganisationUnit>>

    fun getTeiActivePrograms(
        teiUid: String,
        showOnlyActive: Boolean,
    ): Observable<List<kotlin.Pair<Program, MetadataIconData>>>

    fun getTEIEnrollments(
        teiUid: String,
    ): Observable<List<Enrollment>>

    fun saveCatOption(eventUid: String?, catOptionComboUid: String?)

    fun checkIfDeleteTeiIsPossible(): Boolean

    fun deleteTei(): Single<Boolean>

    fun checkIfDeleteEnrollmentIsPossible(enrollmentUid: String): Boolean

    fun deleteEnrollment(enrollmentUid: String): Single<Boolean>

    fun getNoteCount(): Single<Int>

    fun getEnrollmentStatus(enrollmentUid: String?): EnrollmentStatus?

    fun updateEnrollmentStatus(
        enrollmentUid: String,
        status: EnrollmentStatus,
    ): Observable<StatusChangeResultCode>

    fun programHasRelationships(): Boolean

    fun programHasAnalytics(): Boolean

    fun getTETypeName(): String?

    fun getAttributesMap(
        programUid: String,
        teiUid: String,
    ): Observable<List<kotlin.Pair<TrackedEntityAttribute, TrackedEntityAttributeValue>>>

    fun getDashboardModel(): DashboardModel

    fun getGrouping(): Boolean

    fun setGrouping(groupEvent: Boolean)
}
