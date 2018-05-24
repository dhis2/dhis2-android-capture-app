package com.dhis2.usescases.programEventDetail;

import com.dhis2.usescases.general.AbstractActivityContracts;
import com.dhis2.utils.Period;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

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

        void setOrgUnitFilter(StringBuilder orgUnitFilter);

        void showHideFilter();

        void apply();
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(View view, String programId, Period period);

        void onTimeButtonClick();

        void onDateRangeButtonClick();

        void onOrgUnitButtonClick();

        ProgramModel getCurrentProgram();

        void addEvent();

        void onBackClick();

        void setProgram(ProgramModel program);

        void onCatComboSelected(CategoryOptionComboModel categoryOptionComboModel, String orgUnitQuery);

        void clearCatComboFilters(String orgUnitQuery);

        void onEventClick(String eventId);

        Observable<List<TrackedEntityDataValueModel>> getEventDataValue(EventModel event);

        void showFilter();



        void getProgramEventsWithDates(List<Date> dates, Period period, String orgUnitQuery);
    }

    public interface Interactor extends AbstractActivityContracts.Interactor {
        void init(View view, String programId, Period period);

        void getEvents(String programId, Date fromDate, Date toDate, String orgUnitQuery);

        void getOrgUnits(Date date);

        void updateFilters(CategoryOptionComboModel categoryOptionComboModel, String orgUnitQuery);

        Observable<List<TrackedEntityDataValueModel>> getEventDataValue(EventModel event);



        void getProgramEventsWithDates(String programId, List<Date> dates, Period period, String orgUnitQuery);
    }
}
