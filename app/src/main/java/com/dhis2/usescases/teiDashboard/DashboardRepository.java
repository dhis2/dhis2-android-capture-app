package com.dhis2.usescases.teiDashboard;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.relationship.RelationshipModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 30/11/2017.
 *
 */

public interface DashboardRepository {

    Observable<ProgramModel> getProgramData(String programUid);

    Observable<List<TrackedEntityAttributeModel>> getAttributes(String programId);

    Observable<OrganisationUnitModel> getOrgUnit(String orgUnitId);

    Observable<List<ProgramStageModel>> getProgramStages(String programStages);

    Observable<EnrollmentModel> getEnrollment(String programUid, String teiUid);

    Observable<List<EventModel>> getTEIEnrollmentEvents(String programUid, String teiUid);

    Observable<List<TrackedEntityAttributeValueModel>> getTEIAttributeValues(String programUid, String teiUid);

    Observable<List<RelationshipModel>> getRelationships(String programUid, String teiUid);

    Observable<Void> setFollowUp(String enrollmentUid, boolean followUp);
}
