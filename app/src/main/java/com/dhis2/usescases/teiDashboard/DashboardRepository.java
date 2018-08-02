package com.dhis2.usescases.teiDashboard;

import android.support.annotation.NonNull;

import com.dhis2.data.tuples.Pair;
import com.dhis2.data.tuples.Trio;

import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.enrollment.note.NoteModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramIndicatorModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.relationship.RelationshipModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.Calendar;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * Created by ppajuelo on 30/11/2017.
 */

public interface DashboardRepository {

    Observable<ProgramModel> getProgramData(String programUid);

    Observable<List<TrackedEntityAttributeModel>> getAttributes(String programId);

    Observable<OrganisationUnitModel> getOrgUnit(String orgUnitId);

    Observable<List<ProgramStageModel>> getProgramStages(String programStages);

    Observable<EnrollmentModel> getEnrollment(String programUid, String teiUid);

    Observable<List<EventModel>> getTEIEnrollmentEvents(String programUid, String teiUid);

    Observable<List<EventModel>> getEnrollmentEventsWithDisplay(String programUid, String teiUid);

    Observable<List<TrackedEntityAttributeValueModel>> getTEIAttributeValues(String programUid, String teiUid);

    /*Observable<List<RelationshipModel>> getRelationships(String teiUid);

    void saveRelationship(String teuid_a, String teuid_b, String relationshipType);

    void deleteRelationship(RelationshipModel relationshipId);*/

    Flowable<List<ProgramIndicatorModel>> getIndicators(String programUid);

    int setFollowUp(String programUid, String enrollmentUid, boolean followUp);

    Flowable<List<NoteModel>> getNotes(String programUid, String teUid);

    Consumer<Pair<String, Boolean>> handleNote();

    void setDashboardDetails(String teiUid, String programUid);

    Flowable<List<EventModel>> getScheduleEvents(String programUid, String teUid, String filter);

    Observable<List<TrackedEntityAttributeValueModel>> mainTrackedEntityAttributes(String teiUid);

    EventModel updateState(EventModel eventModel, EventStatus newStatus);

    Flowable<Long> updateEnrollmentStatus(@NonNull String uid, @NonNull EnrollmentStatus value);

    Observable<ProgramStageModel> displayGenerateEvent(String eventUid);

    Observable<String> generateNewEvent(String lastModifiedEventUid, Integer standardInterval);

    Observable<Trio<ProgramIndicatorModel, String, String>> getLegendColorForIndicator(ProgramIndicatorModel programIndicator, String value);

    Observable<String> generateNewEventFromDate(String lastModifiedEventUid, Calendar chosenDate);
}