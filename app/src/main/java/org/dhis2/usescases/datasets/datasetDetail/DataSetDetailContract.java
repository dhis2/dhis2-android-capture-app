package org.dhis2.usescases.datasets.datasetDetail;

import com.unnamed.b.atv.model.TreeNode;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.Period;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;

public class DataSetDetailContract {

    public interface View extends AbstractActivityContracts.View {
        void setData(List<DataSetDetailModel> dataSetDetailModels);

        void addTree(TreeNode treeNode);

        void openDrawer();

        void showTimeUnitPicker();

        void showRageDatePicker();

        void renderError(String message);

        void setCatComboOptions(CategoryComboModel catCombo, List<CategoryOptionComboModel> catComboList);

        void setOrgUnitFilter(StringBuilder orgUnitFilter);

        void showHideFilter();

        void apply();

        void setWritePermission(Boolean aBoolean);

        Flowable<Integer> dataSetPage();

        String dataSetUid();
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(View view);

        void onTimeButtonClick();

        void onDateRangeButtonClick();

        void onOrgUnitButtonClick();

        void addDataSet();

        void onBackClick();

        void onCatComboSelected(CategoryOptionComboModel categoryOptionComboModel, String orgUnitQuery);

        void clearCatComboFilters(String orgUnitQuery);

        void onDataSetClick(String eventId, String orgUnit);

        List<OrganisationUnitModel> getOrgUnits();

        void showFilter();

        void getDataSets(Date fromDate, Date toDate, String orgUnitQuery);

        void getOrgUnits(Date date);

        void getDataSetWithDates(List<Date> dates, Period period, String orgUnitQuery);

    }
}
