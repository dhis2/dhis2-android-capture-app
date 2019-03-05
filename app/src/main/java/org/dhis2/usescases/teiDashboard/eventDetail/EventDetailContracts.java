package org.dhis2.usescases.teiDashboard.eventDetail;

import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.custom_views.OrgUnitDialog;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStage;

import androidx.annotation.NonNull;
import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 19/12/2017.
 */

public class EventDetailContracts {

    interface EventDetailView extends AbstractActivityContracts.View {

        void setData(EventDetailModel eventDetailModel, MetadataRepository metadataRepository);

        void setDataEditable();

        void isEventExpired(ProgramModel programModel);

        void showConfirmDeleteEvent();

        void showEventWasDeleted();

        void goBack(boolean changedEventStatus);

        void showOrgUnitSelector(OrgUnitDialog orgUnitDialog);

        void setSelectedOrgUnit(OrganisationUnitModel selectedOrgUnit);

        void updateActionButton(EventStatus eventStatus);

        @NonNull
        Consumer<EventStatus> updateStatus(EventStatus eventStatus);

        void setDate(String result);

        void showCatOptionDialog();
    }

    public interface EventDetailPresenter extends AbstractActivityContracts.Presenter {
        void init(EventDetailView view);

        void getEventData(String eventUid);

        void saveData(String uid, String value);

        void back();

        void eventStatus(android.view.View view, EventModel eventModel, ProgramStage programStage);

        void editData();

        void getExpiryDate(String eventUid);

        void confirmDeleteEvent();

        void deleteEvent();

        void onOrgUnitClick();

        void setDate();

        void selectCatOption();

        void changeCatOption(CategoryOptionComboModel selectedOption);
    }


}
