package org.dhis2.usescases.datasets.dataSetTable;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.dataset.DataSetCompleteRegistration;
import org.hisp.dhis.android.core.dataset.DataSetInstance;
import org.hisp.dhis.android.core.dataset.Section;
import org.hisp.dhis.android.core.period.Period;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;

@Singleton
public class DataSetTableRepositoryImpl implements DataSetTableRepository {

    private final String dataSetUid;
    private final D2 d2;
    private final String periodId;
    private final String orgUnitUid;
    private final String catOptCombo;
    private FlowableProcessor<Unit> dataSetInstanceProcessor = PublishProcessor.create();

    public DataSetTableRepositoryImpl(D2 d2, String dataSetUid,
                                      String periodId, String orgUnitUid, String catOptCombo) {
        this.d2 = d2;
        this.dataSetUid = dataSetUid;
        this.periodId = periodId;
        this.orgUnitUid = orgUnitUid;
        this.catOptCombo = catOptCombo;
    }

    @Override
    public Flowable<DataSet> getDataSet() {
        return Flowable.fromCallable(() -> d2.dataSetModule().dataSets().byUid().eq(dataSetUid).one().blockingGet());
    }

    @Override
    public Flowable<Period> getPeriod() {
        return d2.periodModule().periodHelper().getPeriodForPeriodId(periodId).toFlowable();
    }

    @Override
    public Flowable<DataSetInstance> dataSetInstance() {
        return dataSetInstanceProcessor.startWith(new Unit())
                .switchMap(next -> d2.dataSetModule().dataSetInstances()
                        .byDataSetUid().eq(dataSetUid)
                        .byAttributeOptionComboUid().eq(catOptCombo)
                        .byOrganisationUnitUid().eq(orgUnitUid)
                        .byPeriod().eq(periodId).one().exists().toFlowable())
                .flatMap(exist -> {
                    if (exist) {
                        return d2.dataSetModule().dataSetInstances()
                                .byDataSetUid().eq(dataSetUid)
                                .byAttributeOptionComboUid().eq(catOptCombo)
                                .byOrganisationUnitUid().eq(orgUnitUid)
                                .byPeriod().eq(periodId).one().get().toFlowable();
                    } else {
                        return defaultDataSetInstance();
                    }
                });

    }

    @Override
    public Flowable<DataSetInstance> defaultDataSetInstance() {
        return Single.zip(
                d2.dataSetModule().dataSets().uid(dataSetUid).get(),
                d2.categoryModule().categoryOptionCombos().uid(catOptCombo).get(),
                d2.organisationUnitModule().organisationUnits().uid(orgUnitUid).get(),
                d2.periodModule().periodHelper().getPeriodForPeriodId(periodId),
                (dataSet, catOptComb, orgUnit, period) ->
                        DataSetInstance.builder()
                                .dataSetUid(dataSetUid)
                                .dataSetDisplayName(dataSet.displayName())
                                .attributeOptionComboUid(catOptComb.uid())
                                .attributeOptionComboDisplayName(catOptComb.displayName())
                                .organisationUnitUid(orgUnitUid)
                                .organisationUnitDisplayName(orgUnit.displayName())
                                .periodType(period.periodType())
                                .period(period.periodId())
                                .valueCount(0)
                                .completed(false)
                                .state(State.SYNCED)
                                .build()
        ).toFlowable();
    }

    @Override
    public Flowable<List<String>> getSections() {

        return Flowable.fromCallable(() -> d2.dataSetModule().sections().byDataSetUid().eq(dataSetUid).blockingGet())
                .switchMap(sections -> {
                    List<String> sectionUids = new ArrayList<>();
                    if (sections.isEmpty()) {
                        sectionUids.add("NO_SECTION");
                    } else {
                        for (Section section : sections) {
                            sectionUids.add(section.displayName());
                        }
                    }
                    return Flowable.just(sectionUids);
                });
    }


    @Override
    public Flowable<Boolean> dataSetStatus() {
        DataSetCompleteRegistration dscr = d2.dataSetModule().dataSetCompleteRegistrations()
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid().eq(catOptCombo)
                .byOrganisationUnitUid().eq(orgUnitUid)
                .byPeriod().eq(periodId).one().blockingGet();
        return Flowable.just(dscr != null && (dscr.deleted() == null || !dscr.deleted()));
    }

    public Flowable<State> dataSetState() {
        return Flowable.defer(() -> {
            State state;
            DataSetInstance dataSetInstance = d2.dataSetModule().dataSetInstances()
                    .byDataSetUid().eq(dataSetUid)
                    .byAttributeOptionComboUid().eq(catOptCombo)
                    .byOrganisationUnitUid().eq(orgUnitUid)
                    .byPeriod().eq(periodId).one().blockingGet();

            state = dataSetInstance.state();

            DataSetCompleteRegistration dscr = d2.dataSetModule().dataSetCompleteRegistrations()
                    .byDataSetUid().eq(dataSetUid)
                    .byAttributeOptionComboUid().eq(catOptCombo)
                    .byOrganisationUnitUid().eq(orgUnitUid)
                    .byPeriod().eq(periodId).one().blockingGet();

            if (state == State.SYNCED && dscr != null) {
                state = dscr.state();
            }

            return state != null ? Flowable.just(state) : Flowable.empty();
        });
    }

    @Override
    public Flowable<String> getCatComboName(String catcomboUid) {
        return Flowable.fromCallable(() -> d2.categoryModule().categoryOptionCombos().uid(catcomboUid).blockingGet().displayName());
    }

    @Override
    public String getCatOptComboFromOptionList(List<String> catOpts) {
        if (catOpts.isEmpty())
            return d2.categoryModule().categoryOptionCombos().byDisplayName().like("default").one().blockingGet().uid();
        else
            return d2.categoryModule().categoryOptionCombos().byCategoryOptions(catOpts).one().blockingGet().uid();
    }

    @Override
    public @NotNull Single<String> getDataSetCatComboName() {
        if (catOptCombo != null) {
            return d2.categoryModule().categoryOptionCombos().uid(catOptCombo).get()
                    .map(categoryOptionCombo -> categoryOptionCombo.categoryCombo().uid())
                    .flatMap(catComboUid -> d2.categoryModule().categoryCombos().uid(catComboUid).get())
                    .map(BaseIdentifiableObject::displayName);
        } else {
            return Single.just("");
        }
    }

    @Override
    public Flowable<Boolean> completeDataSetInstance() {
        return d2.dataSetModule().dataSetCompleteRegistrations()
                .value(periodId, orgUnitUid, dataSetUid, catOptCombo).exists()
                .map(alreadyCompleted -> {
                    if (!alreadyCompleted) {
                        d2.dataSetModule().dataSetCompleteRegistrations()
                                .value(periodId, orgUnitUid, dataSetUid, catOptCombo)
                                .blockingSet();
                        dataSetInstanceProcessor.onNext(new Unit());
                    }
                    return true;
                }).toFlowable();
    }
}
