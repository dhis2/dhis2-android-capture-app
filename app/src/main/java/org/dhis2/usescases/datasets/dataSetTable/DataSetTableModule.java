package org.dhis2.usescases.datasets.dataSetTable;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.prefs.PreferenceProvider;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialRepository;
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialRepositoryImpl;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@PerActivity
@Module
public class DataSetTableModule {

    private DataSetTableContract.View view;
    private final String dataSetUid;
    private final String periodId;
    private final String orgUnitUid;
    private final String catOptCombo;

    DataSetTableModule(DataSetTableActivity view, String dataSetUid, String periodId, String orgUnitUid, String catOptCombo) {
        this.view = view;
        this.dataSetUid = dataSetUid;
        this.periodId = periodId;
        this.orgUnitUid = orgUnitUid;
        this.catOptCombo = catOptCombo;
    }

    @Provides
    @PerActivity
    DataSetTableContract.Presenter providesPresenter(DataSetTableRepository DataSetTableRepository, SchedulerProvider schedulerProvider) {
        return new DataSetTablePresenter(view, DataSetTableRepository, schedulerProvider);
    }

    @Provides
    @PerActivity
    DataSetTableRepository DataSetTableRepository(D2 d2) {
        return new DataSetTableRepositoryImpl(d2, dataSetUid, periodId, orgUnitUid, catOptCombo);
    }

    @Provides
    @PerActivity
    DataSetInitialRepository DataSetInitialRepository(D2 d2) {
        return new DataSetInitialRepositoryImpl(d2, dataSetUid);
    }

}
