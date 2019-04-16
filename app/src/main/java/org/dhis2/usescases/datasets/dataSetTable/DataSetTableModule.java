package org.dhis2.usescases.datasets.dataSetTable;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialRepository;
import org.dhis2.usescases.datasets.datasetInitial.DataSetInitialRepositoryImpl;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@PerActivity
@Module
public class DataSetTableModule {

    private final String dataSetUid;
    private final String periodId;
    private final String orgUnitUid;
    private final String catOptCombo;

    DataSetTableModule(String dataSetUid, String periodId, String orgUnitUid, String catOptCombo) {
        this.dataSetUid = dataSetUid;
        this.periodId = periodId;
        this.orgUnitUid = orgUnitUid;
        this.catOptCombo = catOptCombo;
    }

    @Provides
    @PerActivity
    DataSetTableContract.View provideView(DataSetTableActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    DataSetTableContract.Presenter providesPresenter(DataSetTableRepository DataSetTableRepository,
                                                     DataSetInitialRepository dataSetInitialRepository) {
        return new DataSetTablePresenter(DataSetTableRepository, dataSetInitialRepository);
    }

    @Provides
    @PerActivity
    DataSetTableRepository DataSetTableRepository(BriteDatabase briteDatabase, D2 d2) {
        return new DataSetTableRepositoryImpl(d2, briteDatabase, dataSetUid, periodId, orgUnitUid, catOptCombo);
    }

    @Provides
    @PerActivity
    DataSetInitialRepository DataSetInitialRepository(BriteDatabase briteDatabase, D2 d2) {
        return new DataSetInitialRepositoryImpl(d2, dataSetUid);
    }

}
