package org.dhis2.usescases.main.program;

import androidx.annotation.UiThread;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.Period;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 18/10/2017.
 */
public class ProgramContract {

    interface View extends AbstractActivityContracts.View {

        void showRageDatePicker();

        void showTimeUnitPicker();

        void setUpRecycler();

        void getSelectedPrograms(ArrayList<Date> dates, Period period, String orgUnitQuery);

        Consumer<List<ProgramViewModel>> swapProgramModelData();

        void setOrgUnitFilter(StringBuilder orgUnitFilter);

        @UiThread
        void renderError(String message);

        @UiThread
        void addTree(TreeNode treeNode);

        void openDrawer();

        ArrayList<Date> getChosenDateWeek();

        ArrayList<Date> getChosenDateMonth();

        ArrayList<Date> getChosenDateYear();

        Date getChosenDateDay();

        void orgUnitProgress(boolean showProgress);
    }

    public interface Presenter {
        void init(View view);

        void onItemClick(ProgramViewModel programModel, Period currentPeriod);

        void onOrgUnitButtonClick();

        void onDateRangeButtonClick();

        void onTimeButtonClick();

        void showDescription(String description);

        Observable<Pair<Integer, String>> getNumberOfRecords(ProgramModel programModel);

        void getProgramsWithDates(ArrayList<Date> dates, Period period);

        void getProgramsOrgUnit(List<Date> dates, Period period, String orgUnitQuery);

        void getAllPrograms(String orgUnitQuery);

        List<OrganisationUnitModel> getOrgUnits();

        void dispose();
    }
}
