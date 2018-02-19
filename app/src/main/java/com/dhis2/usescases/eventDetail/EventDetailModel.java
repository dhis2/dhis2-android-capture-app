package com.dhis2.usescases.eventDetail;

import android.databinding.BaseObservable;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

import java.util.List;

/**
 * Created by Cristian on 08/02/2018.
 *
 */

public class EventDetailModel extends BaseObservable {

    private EventModel eventModel;
    private List<TrackedEntityDataValueModel> dataValueModelList;

    public EventDetailModel(EventModel eventModel, List<TrackedEntityDataValueModel> dataValueModelList) {
        this.eventModel = eventModel;
        this.dataValueModelList = dataValueModelList;
    }

    public EventModel getEventModel() {
        return eventModel;
    }

    public List<TrackedEntityDataValueModel> getDataValueModelList() {
        return dataValueModelList;
    }
}
