package com.dhis2.usescases.eventDetail;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Cristian E. on 02/11/2017.
 *
 */

public interface EventDetailRepository {

    @NonNull
    Observable<EventModel> eventModelDetail(String uid);

    @NonNull
    Observable<List<TrackedEntityDataValueModel>> dataValueModelList(String eventUid);
}
