package com.dhis2.data.metadata;

import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityModel;

import io.reactivex.Observable;

/**
 * Created by ppajuelo on 04/12/2017.
 */

public class MetadataRepositoryImpl implements MetadataRepository {

    private final String TRACKED_ENTITY_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ",
            TrackedEntityModel.TABLE, TrackedEntityModel.TABLE, TrackedEntityModel.Columns.UID);

    private final BriteDatabase briteDatabase;

    public MetadataRepositoryImpl(@NonNull BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @Override
    public Observable<TrackedEntityModel> getTrackedEntity(String trackedEntityUid) {
        return briteDatabase
                .createQuery(TrackedEntityModel.TABLE, TRACKED_ENTITY_QUERY + "'" + trackedEntityUid + "'")
                .mapToOne(TrackedEntityModel::create);
    }
}
