package com.dhis2.usescases.programEventDetail;

import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.event.EventModel;

import java.util.ArrayList;
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
    public Observable<List<EventModel>> programEvents(String uid) {
        // TODO CRIS: MAKE THE CORRECT QUERY
//        String SELECT_EVENT_WITH_UID = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.UID + "=";
//        return briteDatabase.createQuery(EventModel.TABLE, SELECT_EVENT_WITH_UID + "'" + uid + "'")
//                .mapToOne(EventModel::create);
        return Observable.just(new ArrayList<>());
    }
}