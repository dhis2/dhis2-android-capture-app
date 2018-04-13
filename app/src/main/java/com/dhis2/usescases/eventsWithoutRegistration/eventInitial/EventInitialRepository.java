package com.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.content.Context;
import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Cristian E. on 02/11/2017.
 *
 */

public interface EventInitialRepository {

    @NonNull
    Observable<EventModel> event(String eventId);

    @NonNull
    Observable<List<OrganisationUnitModel>> orgUnits();

    @NonNull
    Observable<List<CategoryOptionComboModel>> catCombo(String programUid);

    @NonNull
    Observable<List<OrganisationUnitModel>> filteredOrgUnits(String date);

    Observable<String> createEvent(@NonNull Context context, @NonNull String program,
                     @NonNull String programStage, @NonNull String date,
                     @NonNull String orgUnitUid, @NonNull String catComboUid,
                     @NonNull String catOptionUid, @NonNull String latitude, @NonNull String longitude);

    @NonNull
    Observable<EventModel> newlyCreatedEvent(long rowId);

    @NonNull
    Observable<ProgramStageModel> programStage(String programUid);

    @NonNull
    Observable<EventModel> editEvent(String eventUid, String date, String orgUnitUid, String catComboUid, String latitude, String longitude);
}
