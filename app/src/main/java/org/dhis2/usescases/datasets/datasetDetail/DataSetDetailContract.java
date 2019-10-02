package org.dhis2.usescases.datasets.datasetDetail;


import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;

import java.util.List;

public class DataSetDetailContract {

    public interface View extends AbstractActivityContracts.View {
        void setData(List<DataSetDetailModel> dataSetDetailModels);

        void renderError(String message);

        void showHideFilter();

        void clearFilters();

        void updateFilters(int totalFilters);

        void openOrgUnitTreeSelector();

        void showPeriodRequest(FilterManager.PeriodRequest periodRequest);

        void setCatOptionComboFilter(Pair<CategoryCombo, List<CategoryOptionCombo>> categoryOptionCombos);

        void setWritePermission(Boolean aBoolean);

        String dataSetUid();

        Boolean accessDataWrite();

        void startNewDataSet();
    }

    public interface Presenter extends AbstractActivityContracts.Presenter {
        void init(DataSetDetailContract.View view, String dataSetUid);

        void addDataSet();

        void onBackClick();

        void onDataSetClick(String orgUnit, String orgUnitName, String perdiodId, String periodType, String initPeriodType, String catOptionComb);

        void showFilter();

        void onSyncIconClick(String orgUnit, String attributeCombo, String periodId);

        void clearFilterClick();
    }
}
