package org.dhis2.usescases.teiDashboard.eventDetail;

import android.support.annotation.NonNull;

import org.dhis2.data.tuples.Pair;

import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageDataElementModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * QUADRAM. Created by Cristian E. on 02/11/2017.
 */

public interface EventDetailRepository {

    @NonNull
    Observable<EventModel> eventModelDetail(String uid);

    @NonNull
    Observable<List<ProgramStageSectionModel>> programStageSection(String eventUid);

    @NonNull
    Observable<List<ProgramStageDataElementModel>> programStageDataElement(String eventUid);

    @NonNull
    Observable<List<TrackedEntityDataValueModel>> dataValueModelList(String eventUid);

    @NonNull
    Observable<ProgramStageModel> programStage(String eventUid);

    void deleteNotPostedEvent(String eventUid);

    void deletePostedEvent(EventModel eventModel);

    @NonNull
    Observable<String> orgUnitName(String eventUid);

    @NonNull
    Observable<OrganisationUnitModel> orgUnit(String eventUid);

    Observable<List<OrganisationUnitModel>> getOrgUnits();

    Observable<Pair<String,List<CategoryOptionComboModel>>> getCategoryOptionCombos();

    @NonNull
    Flowable<EventStatus> eventStatus(String eventUid);

    Observable<ProgramModel> getProgram(String eventUid);

    void saveCatOption(CategoryOptionComboModel selectedOption);
}
