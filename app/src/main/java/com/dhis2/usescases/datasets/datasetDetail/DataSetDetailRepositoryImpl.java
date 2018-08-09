package com.dhis2.usescases.datasets.datasetDetail;

import android.support.annotation.NonNull;

import com.dhis2.utils.Period;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

public class DataSetDetailRepositoryImpl implements DataSetDetailRepository {

    private final BriteDatabase briteDatabase;

    public DataSetDetailRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @NonNull
    @Override
    public Observable<List<DataSetModel>> filteredDataSet(String programUid, String fromDate, String toDate, CategoryOptionComboModel categoryOptionComboModel) {
        return null;
    }

    @NonNull
    @Override
    public Observable<List<DataSetModel>> filteredDataSet(String programUid, List<Date> dates, Period period, CategoryOptionComboModel categoryOptionComboModel) {
        return null;
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits() {
        return null;
    }

    @NonNull
    @Override
    public Observable<List<CategoryOptionComboModel>> catCombo(String programUid) {
        return null;
    }

    @NonNull
    @Override
    public Observable<List<TrackedEntityDataValueModel>> dataSetDataValues(DataSetModel eventModel) {
        return null;
    }

    @Override
    public Observable<List<String>> dataSetValuesNew(DataSetModel eventModel) {
        return null;
    }

    @Override
    public Observable<Boolean> writePermission(String programId) {
        return null;
    }
}
