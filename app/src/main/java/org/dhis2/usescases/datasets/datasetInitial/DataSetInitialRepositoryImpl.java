package org.dhis2.usescases.datasets.datasetInitial;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
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
    public Observable<List<OrganisationUnitModel>> orgUnits() {
        return Observable.fromIterable(d2.organisationUnitModule().organisationUnits.withDataSets().get())
                .map(organisationUnit -> {
                    List<OrganisationUnit> dataSetOrgUnits = new ArrayList<>();
                    List<OrganisationUnit> orgUnits = new ArrayList<>();
                    for (DataSet dataSet : organisationUnit.dataSets()) {
                        if (dataSet.uid().equals(dataSetUid))
                            orgUnits.add(organisationUnit);
                        dataSetOrgUnits.addAll(orgUnits);
                    }
                    return dataSetOrgUnits;
                })
                .flatMapIterable(organisationUnits -> organisationUnits)
                .flatMap(organisationUnit-> {
                    OrganisationUnitModel.Builder orgUnitBuilder = OrganisationUnitModel.builder();
                    orgUnitBuilder.uid(organisationUnit.uid());
                    orgUnitBuilder.code(organisationUnit.code());
                    orgUnitBuilder.name(organisationUnit.name());
                    orgUnitBuilder.displayName(organisationUnit.displayName());
                    orgUnitBuilder.created(organisationUnit.created());
                    orgUnitBuilder.lastUpdated(organisationUnit.lastUpdated());
                    orgUnitBuilder.shortName(organisationUnit.shortName());
                    orgUnitBuilder.displayShortName(organisationUnit.displayShortName());
                    orgUnitBuilder.description(organisationUnit.description());
                    orgUnitBuilder.displayDescription(organisationUnit.displayDescription());
                    orgUnitBuilder.path(organisationUnit.path());
                    orgUnitBuilder.openingDate(organisationUnit.openingDate());
                    orgUnitBuilder.closedDate(organisationUnit.closedDate());
                    orgUnitBuilder.level(organisationUnit.level());
                    orgUnitBuilder.parent(organisationUnit.parent().uid());
                    orgUnitBuilder.displayNamePath(organisationUnit.displayNamePath());
                    OrganisationUnitModel orgUnit = orgUnitBuilder.build();
                    return Observable.just(orgUnit);
                })
                .toList().toObservable();
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
