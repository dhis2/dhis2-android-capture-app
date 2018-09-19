package org.dhis2.usescases.dataset.dataSetPeriod;


import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.period.PeriodModel;

import java.util.List;

import io.reactivex.Observable;

public class DataSetPeriodRepositoryImpl implements DataSetPeriodRepository {

    private final BriteDatabase briteDatabase;

    public DataSetPeriodRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    private String DATASET_BY_ID = "SELECT * " +
            "FROM DataSet " +
            "WHERE DataSet.uid = ?";

    @Override
    public Observable<DataSet> getDataSet(String dataSetId) {
        return null;
        /*return briteDatabase.createQuery(DataSetModel.TABLE, DATASET_BY_ID, dataSetId)
                .mapToOne(DataSetModel::create);*/
    }

    @Override
    public Observable<List<PeriodModel>> getDataSetPeriods(String dataSetId) {
        return null;
    }
}
