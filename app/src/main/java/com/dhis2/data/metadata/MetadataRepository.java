package com.dhis2.data.metadata;

import org.hisp.dhis.android.core.trackedentity.TrackedEntityModel;

import io.reactivex.Observable;


/**
 * Created by ppajuelo on 04/12/2017.
 */

public interface MetadataRepository {

    Observable<TrackedEntityModel> getTrackedEntity(String trackedEntityUid);

}
