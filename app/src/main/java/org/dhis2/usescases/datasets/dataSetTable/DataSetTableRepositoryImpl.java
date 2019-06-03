package org.dhis2.usescases.datasets.dataSetTable;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.dataset.DataSetCompleteRegistration;
import org.hisp.dhis.android.core.dataset.Section;

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
        return Flowable.fromCallable(() -> d2.dataSetModule().dataSets.byUid().eq(dataSetUid).one().get());
    }

    @Override
    public Flowable<List<String>> getSections() {

        return Flowable.fromCallable(() -> d2.dataSetModule().sections.byDataSetUid().eq(dataSetUid).get())
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
    public Flowable<State> dataSetStatus() {
        DataSetCompleteRegistration dscr = d2.dataSetModule().dataSetCompleteRegistrations
                .byDataSetUid().eq(dataSetUid)
                .byAttributeOptionComboUid().eq(catOptCombo)
                .byOrganisationUnitUid().eq(orgUnitUid)
                .byPeriod().eq(periodId).one().get();
        return Flowable.defer(() -> dscr != null ? Flowable.just(dscr.state()) : Flowable.empty());
    }

    @Override
    public Flowable<String> getCatComboName(String catcomboUid) {
        return Flowable.fromCallable(() -> d2.categoryModule().categoryOptionCombos.uid(catcomboUid).get().displayName());
    }


}
