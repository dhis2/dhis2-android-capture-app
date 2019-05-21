package org.dhis2.usescases.programEventDetail;

import com.unnamed.b.atv.model.TreeNode;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.Period;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.DatePeriod;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.List;
import java.util.Map;

import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;
import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by Cristian on 13/02/2017.
 */

public class ProgramEventDetailContract {

    public interface View extends AbstractActivityContracts.View {

        void addTree(TreeNode treeNode);

        void openDrawer();

        void showTimeUnitPicker();

        void showRageDatePicker();

        void setProgram(Program programModel);

        void renderError(String message);

        void setCatComboOptions( List<Category> categories);

        void showHideFilter();

        void apply();

        void setWritePermission(Boolean aBoolean);

        void orgUnitProgress(boolean showProgress);

        Consumer<Pair<TreeNode, List<TreeNode>>> addNodeToTree();

        void setLiveData(LiveData<PagedList<ProgramEventViewModel>> pagedListLiveData);
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(View view, Period period);

        void updateDateFilter(List<DatePeriod> datePeriodList);

        void updateOrgUnitFilter(List<String> orgUnitList);

        void updateCatOptCombFilter(List<CategoryOption> categoryOptionComboMap);

        void onTimeButtonClick();

        void onDateRangeButtonClick();

        void onOrgUnitButtonClick();

        void addEvent();

        void onBackClick();

        void setProgram(ProgramModel program);

        void onCatComboSelected(CategoryOptionComboModel categoryOptionComboModel);

        void clearCatComboFilters();

        void onEventClick(String eventId, String orgUnit);

        void showFilter();

        List<OrganisationUnitModel> getOrgUnits();

        void onExpandOrgUnitNode(TreeNode node, String uid);

        void onSyncIconClick(String uid);
    }
}
