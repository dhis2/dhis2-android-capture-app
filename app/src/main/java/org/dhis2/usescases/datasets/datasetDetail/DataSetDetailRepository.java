package org.dhis2.usescases.datasets.datasetDetail;

import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.period.DatePeriod;

import java.util.List;

import io.reactivex.Flowable;

public interface DataSetDetailRepository {

    String getDataSetUid();

    Flowable<List<DataSetDetailModel>> dataSetGroups(List<String> orgUnits, List<DatePeriod> periodFilter, List<State> stateFilters, List<CategoryOptionCombo> catOptComboFilters);

    Flowable<Boolean> canWriteAny();

    CategoryOptionCombo getCatOptCombo(String selectedCatOptionCombo);

    boolean dataSetHasAnalytics();

    boolean dataSetIsEditable(
            String datasetUid,
            String periodId,
            String organisationUnitUid,
            String attributeOptionComboUid
    );
}
