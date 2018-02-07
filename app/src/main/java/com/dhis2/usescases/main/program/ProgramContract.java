package com.dhis2.usescases.main.program;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import com.dhis2.usescases.general.AbstractActivityContracts;
import com.dhis2.utils.Period;
import com.unnamed.b.atv.model.TreeNode;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.Date;
import java.util.List;

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

        Consumer<List<ProgramModel>> swapProgramData();

        @UiThread
        void renderError(String message);

        @NonNull
        @UiThread
        void addTree(TreeNode treeNode);

        void setOrgUnits(List<OrganisationUnitModel> orgUnits);

        void openDrawer();
    }

    public interface Presenter {
        void init(View view);

        void getPrograms(Date fromDate, Date toDate);

        void onItemClick(ProgramModel homeViewModel);

        void onOrgUnitButtonClick();

        void onDateRangeButtonClick();

        void onTimeButtonClick();

        void getOrgUnits();

        Observable<List<EventModel>> getEvents(ProgramModel programModel);

        void getProgramsWithDates(List<Date> dates, Period period);

        void getProgramsOrgUnit(String orgUnitQuery);


    }


}
