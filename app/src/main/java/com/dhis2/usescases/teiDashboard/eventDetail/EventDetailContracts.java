package com.dhis2.usescases.teiDashboard.eventDetail;

import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.usescases.general.AbstractActivityContracts;

/**
 * Created by ppajuelo on 19/12/2017.
 *
 */

public class EventDetailContracts {

    interface View extends AbstractActivityContracts.View {

        void setData(EventDetailModel eventDetailModel, MetadataRepository metadataRepository);
    }

    public interface Presenter {
        void init(EventDetailContracts.View view);

        void getEventData(String eventUid);

        void saveData(String uid, String value);

        void back();

    }


}
