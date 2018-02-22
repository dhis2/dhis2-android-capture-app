package com.dhis2.usescases.programEventDetail;

import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.event.EventModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 02/11/2017.
 *
 */

public class ProgramEventDetailRepositoryImpl implements ProgramEventDetailRepository {

    private final BriteDatabase briteDatabase;

    ProgramEventDetailRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @NonNull
    @Override
    public Observable<List<EventModel>> programEvents(String programUid) {
        String SELECT_EVENT_WITH_PROGRAM_UID = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.PROGRAM + "=";
        return briteDatabase.createQuery(EventModel.TABLE, SELECT_EVENT_WITH_PROGRAM_UID + "'" + programUid + "'")
                .mapToList(EventModel::create);
    }
}