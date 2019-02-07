package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

/**
 * QUADRAM. Created by Cristian E. on 02/11/2017.
 */

public interface EventInitialRepository {

    @NonNull
    Observable<EventModel> event(String eventId);

    @NonNull
    Observable<List<OrganisationUnitModel>> orgUnits(String programId);

    @NonNull
    Observable<CategoryComboModel> catComboModel(String programUid);

    @NonNull
    Observable<List<CategoryOptionComboModel>> catCombo(String programUid);

    @NonNull
    Observable<List<OrganisationUnitModel>> filteredOrgUnits(String date, String programId);

    Observable<String> createEvent(String enrollmentUid, @Nullable String trackedEntityInstanceUid,
                                   @NonNull Context context, @NonNull String program,
                                   @NonNull String programStage, @NonNull Date date,
                                   @NonNull String orgUnitUid, @NonNull String catComboUid,
                                   @NonNull String catOptionUid, @NonNull String latitude, @NonNull String longitude);

    Observable<String> scheduleEvent(String enrollmentUid, @Nullable String trackedEntityInstanceUid,
                                     @NonNull Context context, @NonNull String program,
                                     @NonNull String programStage, @NonNull Date dueDate,
                                     @NonNull String orgUnitUid, @NonNull String catComboUid,
                                     @NonNull String catOptionUid, @NonNull String latitude, @NonNull String longitude);

    Observable<String> updateTrackedEntityInstance(String eventId, String trackedEntityInstanceUid, String orgUnitUid);

    @NonNull
    Observable<EventModel> newlyCreatedEvent(long rowId);

    @NonNull
    Observable<ProgramStageModel> programStage(String programUid);

    @NonNull
    Observable<ProgramStageModel> programStageWithId(String programStageUid);

    @NonNull
    Observable<EventModel> editEvent(String trackedEntityInstance, String eventUid, String date, String orgUnitUid, String catComboUid, String catOptionCombo, String latitude, String longitude);

    @NonNull
    Observable<List<EventModel>> getEventsFromProgramStage(String programUid, String enrollmentUid, String programStageUid);

    Observable<Boolean> accessDataWrite(String programId);

    void deleteEvent(String eventId, String trackedEntityInstance);

    boolean isEnrollmentOpen();
}
