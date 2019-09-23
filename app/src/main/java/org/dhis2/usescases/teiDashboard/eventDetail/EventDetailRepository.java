package org.dhis2.usescases.teiDashboard.eventDetail;

import androidx.annotation.NonNull;

import org.dhis2.data.tuples.Pair;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageSection;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * QUADRAM. Created by Cristian E. on 02/11/2017.
 */

public interface EventDetailRepository {

    @NonNull
    Observable<Event> eventModelDetail(String uid);

    @NonNull
    Observable<List<ProgramStageSection>> programStageSection(String eventUid);

    @NonNull
    Observable<ProgramStage> programStage(String eventUid);

    void deleteNotPostedEvent(String eventUid);

    void deletePostedEvent(Event eventModel);

    @NonNull
    Observable<String> orgUnitName(String eventUid);

    @NonNull
    Observable<OrganisationUnit> orgUnit(String eventUid);

    Observable<List<OrganisationUnit>> getOrgUnits();

    Observable<Pair<String, List<CategoryOptionCombo>>> getCategoryOptionCombos();

    @NonNull
    Flowable<EventStatus> eventStatus(String eventUid);

    Observable<Program> getProgram(String eventUid);

    void saveCatOption(CategoryOptionCombo selectedOption);

    Observable<Boolean> isEnrollmentActive(String eventUid);

    Observable<Program> getExpiryDateFromEvent(String eventUid);
}
