package org.dhis2.usescases.teiDashboard.eventDetail;

import androidx.annotation.NonNull;

import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.custom_views.OrgUnitDialog;

import org.dhis2.utils.custom_views.OrgUnitDialog_2;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 19/12/2017.
 */

public class EventDetailContracts {

    interface View extends AbstractActivityContracts.View {

        void setData(EventDetailModel eventDetailModel, MetadataRepository metadataRepository);

        void setDataEditable();

        void isEventExpired(ProgramModel programModel);

        void showConfirmDeleteEvent();

        void showEventWasDeleted();

        void goBack(boolean changedEventStatus);

        void showOrgUnitSelector(OrgUnitDialog_2 orgUnitDialog);

        void setSelectedOrgUnit(OrganisationUnit selectedOrgUnit);

        void updateActionButton(EventStatus eventStatus);

        @NonNull
        Consumer<EventStatus> updateStatus(EventStatus eventStatus);

        void setDate(String result);

        void showCatOptionDialog();
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(EventDetailContracts.View view);

        void getEventData(String eventUid);

        void saveData(String uid, String value);

        void back();

        void eventStatus(android.view.View view, Event eventModel, ProgramStage stageModel);

        void editData();

        void getExpiryDate(String eventUid);

        void confirmDeleteEvent();

        void deleteEvent();

        void onOrgUnitClick();

        void setDate();

        void setDueDate();

        void selectCatOption();

        void changeCatOption(CategoryOptionComboModel selectedOption);
    }


}
