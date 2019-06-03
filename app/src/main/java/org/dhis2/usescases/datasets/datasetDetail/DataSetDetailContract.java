package org.dhis2.usescases.datasets.datasetDetail;

import com.unnamed.b.atv.model.TreeNode;

import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.Date;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;

public class DataSetDetailContract {

    public interface View extends AbstractActivityContracts.View {
        void setData(List<DataSetDetailModel> dataSetDetailModels);

        void addTree(TreeNode treeNode);

        void openDrawer();

        void showRageDatePicker();

        void renderError(String message);

        void showHideFilter();

        void apply();

        void setWritePermission(Boolean aBoolean);

        Flowable<Integer> dataSetPage();

        String dataSetUid();

        Boolean accessDataWrite();
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(View view);

        void onDateRangeButtonClick();

        void onOrgUnitButtonClick();

        void addDataSet();

        void onBackClick();

        void onDataSetClick(String orgUnit, String orgUnitName, String perdiodId, String periodType, String initPeriodType, String catOptionComb);

        List<OrganisationUnit> getOrgUnits();

        void showFilter();

        void getOrgUnits(Date date);

        void getDataSetWithDates(List<String> periodIds, List<String> orgUnitQuery);

        Map<String, String> getPeriodAvailableForFilter();

        String getFirstPeriodSelected();

        void onSyncIconClick(String orgUnit, String attributeCombo, String periodId);
    }
}
