package org.dhis2.usescases.datasets.datasetInitial;

import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Flowable;
import io.reactivex.Observable;

public interface DataSetInitialRepository {

    @NonNull
    Observable<DataSetInitialModel> dataSet();

    @NonNull
    Observable<List<OrganisationUnitModel>> orgUnits();

    @NonNull
    Observable<List<CategoryOption>> catCombo(String categoryUid);

    @NonNull
    Flowable<List<DateRangeInputPeriodModel>> getDataInputPeriod();

    @NonNull
    Flowable<String> getCategoryOptionCombo(String catOptions, String catCombo);
}
