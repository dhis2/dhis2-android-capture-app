package org.dhis2.usescases.datasets.datasetDetail;


import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.dataset.DataSetCompleteRegistration;
import org.hisp.dhis.android.core.dataset.DataSetInstanceCollectionRepository;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.DatePeriod;
import org.hisp.dhis.android.core.period.Period;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Flowable;
import io.reactivex.Single;

public class DataSetDetailRepositoryImpl implements DataSetDetailRepository {

    private final D2 d2;
    private final String dataSetUid;

    public DataSetDetailRepositoryImpl(String dataSetUid, D2 d2) {
        this.d2 = d2;
        this.dataSetUid = dataSetUid;
    }

    @Override
    public Single<Pair<CategoryCombo, List<CategoryOptionCombo>>> catOptionCombos() {
        return d2.dataSetModule().dataSets().uid(dataSetUid).get()
                .filter(program -> program.categoryCombo() != null)
                .flatMapSingle(program -> d2.categoryModule().categoryCombos().uid(program.categoryCombo().uid()).get())
                .filter(categoryCombo -> !categoryCombo.isDefault())
                .flatMapSingle(categoryCombo -> Single.zip(
                        d2.categoryModule().categoryCombos()
                                .uid(categoryCombo.uid()).get(),
                        d2.categoryModule().categoryOptionCombos()
                                .byCategoryComboUid().eq(categoryCombo.uid()).get(),
                        Pair::create
                ));
    }

    @Override
    public Flowable<List<DataSetDetailModel>> dataSetGroups(List<String> orgUnits, List<DatePeriod> periodFilter, List<State> stateFilters, List<CategoryOptionCombo> catOptComboFilters) {
        DataSetInstanceCollectionRepository repo;
        repo = d2.dataSetModule().dataSetInstances().byDataSetUid().eq(dataSetUid);
        if (!orgUnits.isEmpty())
            repo = repo.byOrganisationUnitUid().in(orgUnits);
        if (!periodFilter.isEmpty())
            repo = repo.byPeriodStartDate().inDatePeriods(periodFilter);
        if (!catOptComboFilters.isEmpty())
            repo = repo.byAttributeOptionComboUid().in(UidsHelper.getUids(catOptComboFilters));

        DataSetInstanceCollectionRepository finalRepo = repo;
        return Flowable.fromIterable(finalRepo.blockingGet())
                .map(dataSetReport -> {
                    Period period = d2.periodModule().periods().byPeriodId().eq(dataSetReport.period()).one().blockingGet();
                    String periodName = DateUtils.getInstance().getPeriodUIString(period.periodType(), period.startDate(), Locale.getDefault());
                    DataSetCompleteRegistration dscr = d2.dataSetModule().dataSetCompleteRegistrations()
                            .byDataSetUid().eq(dataSetUid)
                            .byAttributeOptionComboUid().eq(dataSetReport.attributeOptionComboUid())
                            .byOrganisationUnitUid().eq(dataSetReport.organisationUnitUid())
                            .byPeriod().eq(dataSetReport.period()).one().blockingGet();

                    State state = dataSetReport.state();

                    if(state == State.SYNCED && dscr!=null){
                        state = dscr.state();
                    }


                    return DataSetDetailModel.create(
                            dataSetReport.organisationUnitUid(),
                            dataSetReport.attributeOptionComboUid(),
                            dataSetReport.period(),
                            dataSetReport.organisationUnitDisplayName(),
                            dataSetReport.attributeOptionComboDisplayName(),
                            periodName,
                            state,
                            dataSetReport.periodType().name());
                })
                .filter(dataSetDetailModel -> stateFilters.isEmpty() || stateFilters.contains(dataSetDetailModel.state()))
                .toSortedList((dataSet1, dataSet2) -> {
                    Date startDate1 = d2.periodModule().periods()
                            .byPeriodId().eq(dataSet1.periodId())
                            .byPeriodType().eq(PeriodType.valueOf(dataSet1.periodType())).one().blockingGet().startDate();
                    Date startDate2 = d2.periodModule().periods()
                            .byPeriodId().eq(dataSet2.periodId())
                            .byPeriodType().eq(PeriodType.valueOf(dataSet2.periodType())).one().blockingGet().startDate();
                    return startDate2.compareTo(startDate1);
                })
                .toFlowable();
    }

    @Override
    public Flowable<Boolean> canWriteAny() {
        return d2.dataSetModule().dataSets().uid(dataSetUid).get().toFlowable()
                .flatMap(dataSet -> {
                    if (dataSet.access().data().write())
                        return d2.categoryModule().categoryOptionCombos().withCategoryOptions()
                                .byCategoryComboUid().eq(dataSet.categoryCombo().uid()).get().toFlowable()
                                .map(categoryOptionCombos -> {
                                    boolean canWriteCatOption = false;
                                    for (CategoryOptionCombo categoryOptionCombo : categoryOptionCombos) {
                                        for (CategoryOption categoryOption : categoryOptionCombo.categoryOptions())
                                            if (categoryOption.access().data().write()) {
                                                canWriteCatOption = true;
                                                break;
                                            }
                                    }
                                    boolean canWriteOrgUnit = false;

                                    if (canWriteCatOption) {

                                        int countOrgUnits = d2.organisationUnitModule().organisationUnits().byDataSetUids(Collections.singletonList(dataSetUid))
                                                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).blockingCount();

                                        canWriteOrgUnit = countOrgUnits != 0;

                                    }

                                    return canWriteCatOption && canWriteOrgUnit;

                                });
                    else
                        return Flowable.just(false);
                });

    }
}