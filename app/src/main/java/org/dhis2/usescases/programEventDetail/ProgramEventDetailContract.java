package org.dhis2.usescases.programEventDetail;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.Period;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * QUADRAM. Created by Cristian on 13/02/2017.
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

        void showHideFilter();

        void apply();

        void setWritePermission(Boolean aBoolean);

        Flowable<Integer> currentPage();
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(View view, String programId, Period period);

        void onTimeButtonClick();

        void onDateRangeButtonClick();

        void onOrgUnitButtonClick();

        void addEvent();

        void onBackClick();

        void setProgram(ProgramModel program);

        void onCatComboSelected(CategoryOptionComboModel categoryOptionComboModel/*, String orgUnitQuery*/);

        void clearCatComboFilters(/*String orgUnitQuery*/);

        void onEventClick(String eventId, String orgUnit);

        Observable<List<String>> getEventDataValueNew(EventModel event);

        void showFilter();

        void getProgramEventsWithDates();

        List<OrganisationUnitModel> getOrgUnits();

        void setFilters(List<Date> selectedDates, Period currentPeriod, String orgUnits);
    }
}
