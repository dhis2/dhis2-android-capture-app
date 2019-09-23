package org.dhis2.usescases.datasets.datasetDetail;

import org.dhis2.data.tuples.Pair;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.period.DatePeriod;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;

public interface DataSetDetailRepository {

    Single<Pair<CategoryCombo, List<CategoryOptionCombo>>> catOptionCombos();

    Flowable<List<DataSetDetailModel>> dataSetGroups(List<String> orgUnits, List<DatePeriod> periodFilter, List<State> stateFilters, List<CategoryOptionCombo> catOptComboFilters);

    Flowable<Boolean> canWriteAny();
}
