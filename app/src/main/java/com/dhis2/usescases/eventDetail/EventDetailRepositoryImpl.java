package com.dhis2.usescases.eventDetail;

import android.support.annotation.NonNull;

import com.dhis2.usescases.main.program.ProgramModule;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.program.ProgramStageSection;
import org.hisp.dhis.android.core.program.ProgramStageSectionModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 02/11/2017.
 */

public class EventDetailRepositoryImpl implements EventDetailRepository {

    private final String PROGRAM_STAGE_SECTION = String.format("SELECT * FROM %s JOIN %s ON %s.%s = %s.%s WHERE %s.%s = ",
            ProgramStageSectionModel.TABLE, EventModel.TABLE,
            EventModel.TABLE, EventModel.Columns.PROGRAM_STAGE, ProgramStageSectionModel.TABLE, ProgramStageSectionModel.Columns.PROGRAM_STAGE,
            EventModel.TABLE, EventModel.Columns.UID
            );

    private final BriteDatabase briteDatabase;


    EventDetailRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @NonNull
    @Override
    public Observable<List<ProgramStageSectionModel>> getEventSections(String eventUid) {
        return briteDatabase.createQuery(ProgramStageSectionModel.TABLE, PROGRAM_STAGE_SECTION+"'"+eventUid+"'")
                .mapToList(ProgramStageSectionModel::create);
    }

    @NonNull
    @Override
    public Observable<EventModel> eventModelDetail(String uid) {
        String SELECT_EVENT_WITH_UID = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.TABLE + "." + EventModel.Columns.UID + "=";
        return briteDatabase.createQuery(EventModel.TABLE, SELECT_EVENT_WITH_UID + "'" + uid + "'")
                .mapToOne(EventModel::create);
    }

    @NonNull
    @Override
    public Observable<List<TrackedEntityDataValueModel>> dataValueModelList(String eventUid) {
        String SELECT_TRACKED_ENTITY_DATA_VALUE_WITH_EVENT_UID = "SELECT * FROM " + TrackedEntityDataValueModel.TABLE + " WHERE " + TrackedEntityDataValueModel.TABLE + "." + TrackedEntityDataValueModel.Columns.EVENT + "=";
        return briteDatabase.createQuery(TrackedEntityDataValueModel.TABLE, SELECT_TRACKED_ENTITY_DATA_VALUE_WITH_EVENT_UID + "'" + eventUid + "'")
                .mapToList(TrackedEntityDataValueModel::create);
    }
}
