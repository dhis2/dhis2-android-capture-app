package com.dhis2.usescases.eventInitial;

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
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(EventInitialContract.View mview, String programId, String eventId);

        void setProgram(ProgramModel program);

        void onBackClick();

        void createEvent();

        void onDateClick(@Nullable DatePickerDialog.OnDateSetListener listener);

        void onOrgUnitButtonClick();

        void onLocationClick();

        void onLocation2Click();

        void getCatOption(String categoryOptionComboId);
    }

    public interface Interactor extends AbstractActivityContracts.Interactor {

        void init(EventInitialContract.View view, String programId, String eventId);

        void getOrgUnits();

        void getCatOption(String categoryOptionComboId);
    }
}
