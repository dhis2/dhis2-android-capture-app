package org.dhis2.usescases.main.program;

import com.unnamed.b.atv.model.TreeNode;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.Period;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.DatePeriod;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.UiThread;
import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 18/10/2017.
 */
public class ProgramContract {

    interface View extends AbstractActivityContracts.View {

        void showRageDatePicker();

        void showTimeUnitPicker();

        Consumer<List<ProgramViewModel>> swapProgramModelData();

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

        Consumer<Pair<TreeNode, List<TreeNode>>> addNodeToTree();

        void openOrgUnitTreeSelector();

        void showHideFilter();

        void clearFilters();
    }

    public interface Presenter {
        void init(View view);

        void onItemClick(ProgramViewModel programModel, Period currentPeriod);

        void onOrgUnitButtonClick();

        void onDateRangeButtonClick();

        void onTimeButtonClick();

        void showDescription(String description);

        void onExpandOrgUnitNode(TreeNode treeNode, String parentUid);

        List<TreeNode> transformToNode(List<OrganisationUnit> orgUnits);

        List<OrganisationUnit> getOrgUnits();

        void dispose();

        void updateDateFilter(List<DatePeriod> datePeriodList);

        void updateOrgUnitFilter(List<String> orgUnitList);

        void onSyncStatusClick(ProgramViewModel program);

        boolean areFiltersApplied();

        void showHideFilterClick();

        void clearFilterClick();
    }
}
