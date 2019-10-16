package org.dhis2.usescases.datasets.datasetInitial;

import androidx.annotation.NonNull;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
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
        return Flowable.just(d2.dataSetModule().dataSets().withDataInputPeriods().byUid().eq(dataSetUid).one().blockingGet())
                .flatMapIterable(dataSet -> dataSet.dataInputPeriods())
                .flatMap(dataInputPeriod ->
                        Flowable.just(d2.periodModule().periods().byPeriodId().eq(dataInputPeriod.period().uid()).blockingGet())
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
        return Observable.just(d2.dataSetModule().dataSets().byUid().eq(dataSetUid).one().blockingGet())
                .map(dataSet -> {
                    String categoryComboDisplayName = d2.categoryModule().categoryCombos().byUid().eq(dataSet.categoryCombo().uid()).one().blockingGet().displayName();
                    CategoryCombo categoryCombos = d2.categoryModule().categoryCombos().withCategories().byUid().eq(dataSet.categoryCombo().uid()).one().blockingGet();

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
        return d2.organisationUnitModule().organisationUnits
                .byDataSetUids(Collections.singletonList(dataSetUid))
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .withDataSets()
                .get().toObservable();
    }

    @NonNull
    @Override
    public Observable<List<CategoryOption>> catCombo(String categoryUid) {
        return Observable.just(d2.categoryModule().categories().withCategoryOptions().byUid().eq(categoryUid).one().blockingGet())
                .map(Category::categoryOptions)
                .map(list -> {
                    Iterator<CategoryOption> iterator = list.iterator();
                    while (iterator.hasNext())
                        if (!iterator.next().access().data().write())
                            iterator.remove();

                    return list;
                });
    }

    @NonNull
    @Override
    public Flowable<String> getCategoryOptionCombo(List<String> catOptions, String catCombo) {
        return d2.categoryModule().categoryOptionCombos().withCategoryOptions().byCategoryOptions(catOptions).byCategoryComboUid().eq(catCombo).one().get()
                .map(BaseIdentifiableObject::uid)
                .toFlowable();
    }

    @NonNull
    @Override
    public Flowable<String> getPeriodId(PeriodType periodType, Date date) {
        return Flowable.fromCallable(() -> {
            if (d2.periodModule().periodHelper().getPeriod(periodType, date) == null)
                d2.periodModule().periodHelper().blockingGetPeriodsForDataSet(dataSetUid);

            return d2.periodModule().periodHelper().getPeriod(periodType, date).periodId();
        });
    }
}
