package org.dhis2.usescases.teiDashboard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipViewModel;
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.tei_events.EventViewModel;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramIndicator;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.android.core.relationship.RelationshipType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 30/11/2017.
 */

public interface DashboardRepository {

    Observable<List<ProgramStage>> getProgramStages(String programStages);

    Observable<Enrollment> getEnrollment(String programUid, String teiUid);

    Observable<List<Event>> getTEIEnrollmentEvents(String programUid, String teiUid);

    Observable<List<Event>> getEnrollmentEventsWithDisplay(String programUid, String teiUid);

    Observable<List<TrackedEntityAttributeValue>> getTEIAttributeValues(String programUid, String teiUid);

    Flowable<List<ProgramIndicator>> getIndicators(String programUid);

    boolean setFollowUp(String enrollmentUid);

    Consumer<Pair<String, Boolean>> handleNote();

    Observable<List<TrackedEntityAttributeValue>> mainTrackedEntityAttributes(String teiUid);

    Event updateState(Event event, EventStatus newStatus);

    Flowable<Enrollment> completeEnrollment(@NonNull String enrollmentUid);

    Observable<ProgramStage> displayGenerateEvent(String eventUid);

    Observable<Trio<ProgramIndicator, String, String>> getLegendColorForIndicator(ProgramIndicator programIndicator, String value);

    Integer getObjectStyle(String uid);

    Observable<List<Pair<RelationshipType, String>>> relationshipsForTeiType(String teType);

    Observable<CategoryCombo> catComboForProgram(String program);

    Observable<List<CategoryOptionCombo>> catOptionCombos(String catComboUid);

    void setDefaultCatOptCombToEvent(String eventUid);

    // FROM METADATA REPOSITORY
    Observable<TrackedEntityInstance> getTrackedEntityInstance(String teiUid);

    Observable<List<ProgramTrackedEntityAttribute>> getProgramTrackedEntityAttributes(String programUid);

    Observable<List<OrganisationUnit>> getTeiOrgUnits(@NonNull String teiUid, @Nullable String programUid);

    Observable<List<Program>> getTeiActivePrograms(String teiUid, boolean showOnlyActive);

    Observable<List<Enrollment>> getTEIEnrollments(String teiUid);

    void saveCatOption(String eventUid, String catOptionComboUid);

    Single<Boolean> deleteTeiIfPossible();

    Single<Boolean> deleteEnrollmentIfPossible(String enrollmentUid);

    Flowable<List<RelationshipViewModel>> listTeiRelationships();

    Single<Integer> getNoteCount();
}