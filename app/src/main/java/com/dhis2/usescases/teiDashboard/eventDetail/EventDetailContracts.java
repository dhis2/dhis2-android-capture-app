package com.dhis2.usescases.teiDashboard.eventDetail;

import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.usescases.general.AbstractActivityContracts;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;

/**
 * Created by ppajuelo on 19/12/2017.
 *
 */

public class EventDetailContracts {

    interface View extends AbstractActivityContracts.View {

        void setData(EventDetailModel eventDetailModel, MetadataRepository metadataRepository);

        void setDataEditable();

        void showConfirmDeleteEvent();

        void showEventWasDeleted();
    }

    public interface Presenter {
        void init(EventDetailContracts.View view);

        void getEventData(String eventUid);

        void saveData(String uid, String value);

        void back();

        void eventStatus(EventModel eventModel, ProgramStageModel stageModel);

        void editData();

        void confirmDeleteEvent();

        void deleteEvent();
    }


}
