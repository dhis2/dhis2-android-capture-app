package com.dhis2.usescases.appInfo;

import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 31/01/2018.
 */

public class InfoRepositoryImpl implements InfoRepository {

    private final BriteDatabase briteDatabase;

    InfoRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @NonNull
    @Override
    public Observable<List<ProgramModel>> programs() {
        return briteDatabase.createQuery(ProgramModel.TABLE, "SELECT * FROM " + ProgramModel.TABLE)
                .mapToList(ProgramModel::create);
    }

    @NonNull
    @Override
    public Observable<List<EventModel>> events() {
        return briteDatabase.createQuery(EventModel.TABLE, "SELECT * FROM " + EventModel.TABLE)
                .mapToList(EventModel::create);
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits() {
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, "SELECT * FROM " + OrganisationUnitModel.TABLE)
                .mapToList(OrganisationUnitModel::create);
    }

    @NonNull
    @Override
    public Observable<List<TrackedEntityInstanceModel>> trackedEntityInstances() {
        return briteDatabase.createQuery(TrackedEntityInstanceModel.TABLE, "SELECT * FROM " + TrackedEntityInstanceModel.TABLE)
                .mapToList(TrackedEntityInstanceModel::create);
    }
}
