package org.dhis2.usescases.datasets.dataSetTable;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.dataset.DataSetCompleteRegistration;
import org.hisp.dhis.android.core.dataset.DataSetElement;
import org.hisp.dhis.android.core.dataset.DataSetInstance;
import org.hisp.dhis.android.core.dataset.Section;
import org.hisp.dhis.android.core.datavalue.DataValue;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;

public class DataSetTableRepositoryImpl implements DataSetTableRepository {

    private final String dataSetUid;
    private final D2 d2;
    private final String periodId;
    private final String orgUnitUid;
    private final String catOptCombo;

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

    public Flowable<State> dataSetState(){
        return Flowable.defer(() ->{
            State state;
            DataSetInstance dataSetInstance = d2.dataSetModule().dataSetInstances()
                    .byDataSetUid().eq(dataSetUid)
                    .byAttributeOptionComboUid().eq(catOptCombo)
                    .byOrganisationUnitUid().eq(orgUnitUid)
                    .byPeriod().eq(periodId).one().blockingGet();

            state = dataSetInstance.state();
            return state != null ? Flowable.just(state) : Flowable.empty();
        });
    }

    @Override
    public Flowable<String> getCatComboName(String catcomboUid) {
        return Flowable.fromCallable(() -> d2.categoryModule().categoryOptionCombos().uid(catcomboUid).blockingGet().displayName());
    }

    @Override
    public String getCatOptComboFromOptionList(List<String> catOpts) {
        if(catOpts.isEmpty())
            return d2.categoryModule().categoryOptionCombos().byDisplayName().like("default").one().blockingGet().uid();
        else
            return d2.categoryModule().categoryOptionCombos().byCategoryOptions(catOpts).one().blockingGet().uid();
    }


}
