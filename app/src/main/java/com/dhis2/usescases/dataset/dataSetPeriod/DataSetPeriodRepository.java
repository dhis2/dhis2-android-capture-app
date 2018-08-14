package com.dhis2.usescases.dataset.dataSetPeriod;

import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.period.PeriodModel;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;

public interface DataSetPeriodRepository {

    @NonNull
    Observable<DataSet> getDataSet(String dataSetId);

    Observable<List<PeriodModel>> getDataSetPeriods(String dataSetId);


}
