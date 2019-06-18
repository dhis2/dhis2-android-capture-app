package org.dhis2.usescases.teiDashboard;

import android.content.Context;
import androidx.annotation.NonNull;

import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.enrollment.note.NoteModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramIndicatorModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.relationship.RelationshipTypeModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;

import java.util.Calendar;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 30/11/2017.
 */

public interface DashboardRepository {

    Observable<List<ProgramStageModel>> getProgramStages(String programStages);

    Observable<EnrollmentModel> getEnrollment(String programUid, String teiUid);

    Observable<List<EventModel>> getTEIEnrollmentEvents(String programUid, String teiUid);

    Observable<List<EventModel>> getEnrollmentEventsWithDisplay(String programUid, String teiUid);

    Observable<List<TrackedEntityAttributeValueModel>> getTEIAttributeValues(String programUid, String teiUid);

    Flowable<List<ProgramIndicatorModel>> getIndicators(String programUid);

    boolean setFollowUp(String enrollmentUid);

    Flowable<List<NoteModel>> getNotes(String programUid, String teUid);

    Consumer<Pair<String, Boolean>> handleNote();

    void setDashboardDetails(String teiUid, String programUid);

    Observable<List<TrackedEntityAttributeValueModel>> mainTrackedEntityAttributes(String teiUid);

    EventModel updateState(EventModel eventModel, EventStatus newStatus);

    Flowable<Long> updateEnrollmentStatus(@NonNull String uid, @NonNull EnrollmentStatus value);

    Observable<ProgramStageModel> displayGenerateEvent(String eventUid);

    Observable<String> generateNewEvent(String lastModifiedEventUid, Integer standardInterval);

    Observable<Trio<ProgramIndicatorModel, String, String>> getLegendColorForIndicator(ProgramIndicatorModel programIndicator, String value);

    Observable<String> generateNewEventFromDate(String lastModifiedEventUid, Calendar chosenDate);

    void updateTeiState();

    Integer getObjectStyle(Context context, String uid);

    Observable<List<Pair<RelationshipTypeModel,String>>> relationshipsForTeiType(String teType);

    Observable<CategoryCombo> catComboForProgram(String program);

    void setDefaultCatOptCombToEvent(String eventUid);
}