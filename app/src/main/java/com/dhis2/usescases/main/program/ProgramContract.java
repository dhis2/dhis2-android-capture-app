package com.dhis2.usescases.main.program;

import android.support.annotation.UiThread;

import com.dhis2.data.tuples.Pair;
import com.dhis2.usescases.general.AbstractActivityContracts;
import com.dhis2.utils.Period;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

/**
 * Created by ppajuelo on 18/10/2017.
 */
public class ProgramContract {

    interface View extends AbstractActivityContracts.View {

        void showRageDatePicker();

        void showTimeUnitPicker();

        void setUpRecycler();

        void getSelectedPrograms(ArrayList<Date> dates, Period period, String orgUnitQuery);

        Consumer<List<ProgramModel>> swapProgramData();

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

    }

    public interface Presenter {
        void init(View view);

        void onItemClick(ProgramModel homeViewModel, Period currentPeriod);

        void onOrgUnitButtonClick();

        void onDateRangeButtonClick();

        void onTimeButtonClick();

        void showDescription(String description);

        Observable<List<EventModel>> getEvents(ProgramModel programModel);

        Observable<Pair<Integer, String>> getNumberOfRecords(ProgramModel programModel);

        void getProgramsWithDates(ArrayList<Date> dates, Period period);

        void getProgramsOrgUnit(List<Date> dates, Period period, String orgUnitQuery);

        Flowable<State> syncState(ProgramModel program);

        void getAllPrograms(String orgUnitQuery);

        List<OrganisationUnitModel> getOrgUnits();
    }


}
