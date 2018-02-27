package com.dhis2.usescases.programEventDetail;

import com.dhis2.usescases.general.AbstractActivityContracts;
import com.dhis2.utils.Period;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.Date;
import java.util.List;

/**
 * Created by Cristian on 13/02/2017.
 *
 */

public class ProgramEventDetailContract {

    public interface View extends AbstractActivityContracts.View {
        void setData(List<EventModel> events);

        void addTree(TreeNode treeNode);

        void openDrawer();

        void showTimeUnitPicker();

        void showRageDatePicker();

        void setProgram(ProgramModel programModel);

        void renderError(String message);

        void setCatComboOptions(CategoryComboModel catCombo, List<CategoryOptionComboModel> catComboList);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(View view, String programId);

        void onTimeButtonClick();

        void onDateRangeButtonClick();

        void onOrgUnitButtonClick();

        void onSearchClick();

        void onBackClick();

        void onTEIClick(String TEIuid, String programUid);

        void setProgram(ProgramModel program);

        void getEvents(Date fromDate, Date toDate);

        void getProgramEventsWithDates(List<Date> dates, Period period);

        void onCatComboSelected(CategoryOptionComboModel categoryOptionComboModel);

        void clearCatComboFilters();
    }

    public interface Interactor extends AbstractActivityContracts.Interactor {
        void init(View view, String programId);

        void getEvents(String programId, Date fromDate, Date toDate);

        void getOrgUnits();

        void getProgramEventsWithDates(String programId, List<Date> dates, Period period);

        void updateFilters(CategoryOptionComboModel categoryOptionComboModel);
    }
}
