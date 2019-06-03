package org.dhis2.usescases.datasets.datasetInitial;

import androidx.annotation.NonNull;

import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

public interface DataSetInitialRepository {

    @NonNull
    Observable<DataSetInitialModel> dataSet();

    @NonNull
    Observable<List<OrganisationUnit>> orgUnits();

    @NonNull
    Observable<List<CategoryOption>> catCombo(String categoryUid);

    @NonNull
    Flowable<List<DateRangeInputPeriodModel>> getDataInputPeriod();

    @NonNull
    Flowable<String> getCategoryOptionCombo(List<String> catOptions, String catCombo);
}
