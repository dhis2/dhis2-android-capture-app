package com.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.app.DatePickerDialog;
import android.support.annotation.Nullable;

import com.dhis2.usescases.general.AbstractActivityContracts;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.List;

/**
 * Created by Cristian on 01/03/2018.
 *
 */

public class EventInitialContract {

    public interface View extends AbstractActivityContracts.View {
        void setProgram(ProgramModel program);

        void setCatComboOptions(CategoryComboModel catCombo, List<CategoryOptionComboModel> catComboList);

        void showDateDialog(DatePickerDialog.OnDateSetListener listener);

        void openDrawer();

        void renderError(String message);

        void addTree(TreeNode treeNode);

        void setEvent(EventModel event);

        void setCatOption(CategoryOptionComboModel categoryOptionComboModel);

        void setLocation(double latitude, double longitude);

        void onEventCreated(String eventUid);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(EventInitialContract.View view, String programId, String eventId);

        void setProgram(ProgramModel program);

        void onBackClick();

        void createEvent(String date, String orgUnitUid, String catComboUid, String catOptionUid, String latitude, String longitude);

        void editEvent(String eventUid, String date, String orgUnitUid, String catComboUid, String latitude, String longitude);

        void onDateClick(@Nullable DatePickerDialog.OnDateSetListener listener);

        void onOrgUnitButtonClick();

        void onLocationClick();

        void onLocation2Click();

        void getCatOption(String categoryOptionComboId);

        void filterOrgUnits(String date);
    }

    public interface Interactor extends AbstractActivityContracts.Interactor {

        void init(EventInitialContract.View view, String programId, String eventId);

        void getOrgUnits();

        void getCatOption(String categoryOptionComboId);

        void getFilteredOrgUnits(String date);

        void createNewEvent(String programUid, String date, String orgUnitUid, String catComboUid, String catOptionUid, String latitude, String longitude);

        void editEvent(String eventUid, String date, String orgUnitUid, String catComboUid, String latitude, String longitude);
    }
}
