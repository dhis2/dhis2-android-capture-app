package org.dhis2.usescases.teiDashboard.eventDetail;

import androidx.annotation.NonNull;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.custom_views.OrgUnitDialog;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;

import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 19/12/2017.
 */

public class EventDetailContracts {

    interface View extends AbstractActivityContracts.View {

        void setData(EventDetailModel eventDetailModel);

        void setDataEditable();

        void isEventExpired(Program programModel);

        void showConfirmDeleteEvent();

        void showEventWasDeleted();

        void goBack(boolean changedEventStatus);

        void showOrgUnitSelector(OrgUnitDialog orgUnitDialog);

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

        void back();

        void eventStatus(android.view.View view, Event eventModel, ProgramStage stageModel);

        void getExpiryDate(String eventUid);

        void confirmDeleteEvent();

        void deleteEvent();

        void onOrgUnitClick();

        void setDate();

        void setDueDate();

        void selectCatOption();

        void changeCatOption(CategoryOptionCombo selectedOption);
    }


}
