package org.dhis2.usescases.datasets.datasetDetail;

import static org.dhis2.data.dhislogic.AuthoritiesKt.AUTH_DATAVALUE_ADD;
import org.dhis2.data.dhislogic.DhisPeriodUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.dataset.DataSetCompleteRegistration;
import org.hisp.dhis.android.core.dataset.DataSetEditableStatus;
import org.hisp.dhis.android.core.dataset.DataSetInstanceCollectionRepository;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.DatePeriod;
import org.hisp.dhis.android.core.period.Period;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.settings.AnalyticsDhisVisualizationsSetting;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import dhis2.org.analytics.charts.Charts;
import io.reactivex.Flowable;
import timber.log.Timber;

public class DataSetDetailRepositoryImpl implements DataSetDetailRepository {

    private final D2 d2;
    private final String dataSetUid;
    private final DhisPeriodUtils periodUtils;
    private final Charts charts;

    public DataSetDetailRepositoryImpl(String dataSetUid, D2 d2, DhisPeriodUtils periodUtils, Charts charts) {
        this.d2 = d2;
        this.dataSetUid = dataSetUid;
        this.periodUtils = periodUtils;
        this.charts = charts;
    }

    @Override
    public String getDataSetUid() {
        return dataSetUid;
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
        d2.dataSetModule().dataSets().uid(dataSetUid).blockingGet();
        int dataSetOrgUnitNumber = d2.organisationUnitModule().organisationUnits()
                .byDataSetUids(Collections.singletonList(dataSetUid))
                .blockingGet().size();

        DataSetInstanceCollectionRepository finalRepo = repo;
        return Flowable.fromIterable(finalRepo.blockingGet())
                .map(dataSetReport -> {
                    Period period = d2.periodModule().periods().byPeriodId().eq(dataSetReport.period()).one().blockingGet();
                    String periodName = periodUtils.getPeriodUIString(period.periodType(), period.startDate(), Locale.getDefault());
                    DataSetCompleteRegistration dscr = d2.dataSetModule().dataSetCompleteRegistrations()
                            .byDataSetUid().eq(dataSetUid)
                            .byAttributeOptionComboUid().eq(dataSetReport.attributeOptionComboUid())
                            .byOrganisationUnitUid().eq(dataSetReport.organisationUnitUid())
                            .byPeriod().eq(dataSetReport.period()).one().blockingGet();

                    State state = dataSetReport.state();

                    if (state == State.SYNCED && dscr != null) {
                        state = dscr.state();
                    }

                    boolean isCompleted = dscr != null && Boolean.FALSE.equals(dscr.deleted());

                    //"Category Combination Name" + "Category option selected"
                    return DataSetDetailModel.create(
                            dataSetReport.dataSetUid(),
                            dataSetReport.organisationUnitUid(),
                            dataSetReport.attributeOptionComboUid(), //catComboUid
                            dataSetReport.period(),
                            dataSetReport.organisationUnitDisplayName(),
                            dataSetReport.attributeOptionComboDisplayName(), //nameCatCombo Unicef.... basic educc
                            periodName,
                            state,
                            dataSetReport.periodType().name(),
                            dataSetOrgUnitNumber > 1,
                            isCompleted,
                            dataSetReport.lastUpdated(),
                            getCategoryComboFromOptionCombo(dataSetReport.attributeOptionComboUid()).displayName()
                    );
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
                    if (dataSet.access().data().write() && hasDataValueAuthority())
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

    private boolean hasDataValueAuthority() {
        return !d2.userModule().authorities().byName().eq(AUTH_DATAVALUE_ADD).blockingIsEmpty();
    }

    @Override
    public CategoryOptionCombo getCatOptCombo(String selectedCatOptionCombo) {
        return d2.categoryModule().categoryOptionCombos().uid(selectedCatOptionCombo).blockingGet();
    }

    @Override
    public boolean dataSetHasAnalytics() {
        AnalyticsDhisVisualizationsSetting visualizationSettings = d2.settingModule().analyticsSetting()
                .visualizationsSettings()
                .blockingGet();

        return visualizationSettings.dataSet().get(dataSetUid) != null &&
                !Objects.requireNonNull(visualizationSettings.dataSet().get(dataSetUid)).isEmpty();

    }

    @Override
    public boolean dataSetIsEditable(
            String datasetUid,
            String periodId,
            String organisationUnitUid,
            String attributeOptionComboUid) {
        return d2.dataSetModule()
                .dataSetInstanceService()
                .blockingGetEditableStatus(
                        datasetUid,
                        periodId,
                        organisationUnitUid,
                        attributeOptionComboUid
                ) instanceof DataSetEditableStatus.Editable;
    }

    private CategoryCombo getCategoryComboFromOptionCombo(String categoryOptionComboUid) {
        CategoryOptionCombo catOptionCombo = d2.categoryModule()
                .categoryOptionCombos()
                .uid(categoryOptionComboUid)
                .blockingGet();
        return d2.categoryModule()
                .categoryCombos()
                .uid(catOptionCombo.categoryCombo().uid())
                .blockingGet();
    }
}