package com.dhis2.usescases.eventDetail;

import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.usescases.general.AbstractActivityContracts;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

import java.util.List;

/**
 * Created by ppajuelo on 19/12/2017.
 */

public class EventDetailContracts {

    interface View extends AbstractActivityContracts.View {

        void setData(EventModel eventModel, List<TrackedEntityDataValueModel> dataValueModelList, MetadataRepository metadataRepository);
    }

    public interface Presenter {
        void init(EventDetailContracts.View view);

        void getEventData(String eventUid);

    }


}
