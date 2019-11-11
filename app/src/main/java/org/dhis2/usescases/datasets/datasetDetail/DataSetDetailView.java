package org.dhis2.usescases.datasets.datasetDetail;


import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;

import java.util.List;

interface DataSetDetailView extends AbstractActivityContracts.View {

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
