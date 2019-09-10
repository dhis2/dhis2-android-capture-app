package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.content.Context;

import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.Date;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * QUADRAM. Created by Cristian E. on 02/11/2017.
 */

public interface EventInitialRepository {

    @NonNull
    Observable<Event> event(String eventId);

    @NonNull
    Observable<CategoryCombo> catCombo(String programUid);

    @NonNull
    Observable<List<OrganisationUnit>> filteredOrgUnits(String date, String programId, String parentId);

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
    Observable<ProgramStageModel> programStage(String programUid);

    @NonNull
    Observable<ProgramStageModel> programStageWithId(String programStageUid);

    @NonNull
    Observable<Event> editEvent(String trackedEntityInstance, String eventUid, String date, String orgUnitUid, String catComboUid, String catOptionCombo, String latitude, String longitude);

    Observable<Boolean> accessDataWrite(String programId);

    void deleteEvent(String eventId, String trackedEntityInstance);

    boolean isEnrollmentOpen();

    Flowable<Map<String,CategoryOption>> getOptionsFromCatOptionCombo(String eventId);

    Date getStageLastDate(String programStageUid,String enrollmentUid);

    Observable<OrganisationUnit> getOrganisationUnit(String orgUnitUid);

}
