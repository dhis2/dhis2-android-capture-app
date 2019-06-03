package org.dhis2.usescases.datasets.datasetInitial;

import androidx.annotation.NonNull;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

public class DataSetInitialRepositoryImpl implements DataSetInitialRepository {

    private final String dataSetUid;
    private final D2 d2;

    public DataSetInitialRepositoryImpl(D2 d2, String dataSetUid) {
        this.d2 = d2;
        this.dataSetUid = dataSetUid;
    }

    @Override
    public Flowable<List<DateRangeInputPeriodModel>> getDataInputPeriod() {
        return Flowable.just(d2.dataSetModule().dataSets.withDataInputPeriods().byUid().eq(dataSetUid).one().get())
                .flatMapIterable(dataSet -> dataSet.dataInputPeriods())
                .flatMap(dataInputPeriod ->
                        Flowable.just(d2.periodModule().periods.byPeriodId().eq(dataInputPeriod.period().uid()).get())
                                .flatMapIterable(periods -> periods)
                                .map(period -> {
                                    Date periodStartDate = period.startDate();
                                    Date periodEndDate = period.endDate();

                                    return DateRangeInputPeriodModel.create(dataSetUid,
                                            dataInputPeriod.period().uid(), dataInputPeriod.openingDate(),
                                            dataInputPeriod.closingDate(), periodStartDate, periodEndDate);
                                })
                ).toList().toFlowable();
    }

    @NonNull
    @Override
    public Observable<DataSetInitialModel> dataSet() {
        return Observable.just(d2.dataSetModule().dataSets.byUid().eq(dataSetUid).one().get())
                .map(dataSet -> {
                    String categoryComboDisplayName = d2.categoryModule().categoryCombos.byUid().eq(dataSet.categoryCombo().uid()).one().get().displayName();
                    CategoryCombo categoryCombos = d2.categoryModule().categoryCombos.withCategories().byUid().eq(dataSet.categoryCombo().uid()).one().get();

                    return DataSetInitialModel.create(
                            dataSet.displayName(),
                            dataSet.description(),
                            dataSet.categoryCombo().uid(),
                            categoryComboDisplayName,
                            dataSet.periodType(),
                            categoryCombos.categories(),
                            dataSet.openFuturePeriods()
                    );
                });
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnit>> orgUnits() {
        return Observable.just(d2.organisationUnitModule().organisationUnits.withDataSets().get())
                .flatMapIterable(organisationUnits -> organisationUnits)
                .filter(organisationUnit -> {
                    boolean result = false;
                    for (DataSet dataSet : organisationUnit.dataSets())
                        if (dataSet.uid().equals(dataSetUid))
                            result = true;
                    return result;
                })
                .toList()
                .toObservable();

    }

    @NonNull
    @Override
    public Observable<List<CategoryOption>> catCombo(String categoryUid) {
        return Observable.just(d2.categoryModule().categories.withCategoryOptions().byUid().eq(categoryUid).one().get())
                .map(Category::categoryOptions);
    }

    @NonNull
    @Override
    public Flowable<String> getCategoryOptionCombo(List<String> catOptions, String catCombo) {
        return Flowable.just(d2.categoryModule().categoryOptionCombos.withCategoryOptions().byCategoryOptions(catOptions).byCategoryComboUid().eq(catCombo).one().get())
                .map(BaseIdentifiableObject::uid);
    }
}
