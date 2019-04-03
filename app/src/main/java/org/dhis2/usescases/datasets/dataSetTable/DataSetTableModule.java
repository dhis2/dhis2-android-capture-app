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

    DataSetTableModule(String dataSetUid) {
        this.dataSetUid = dataSetUid;
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
    DataSetTableRepository DataSetTableRepository(BriteDatabase briteDatabase) {
        return new DataSetTableRepositoryImpl(briteDatabase, dataSetUid);
    }

    @Provides
    @PerActivity
    DataSetInitialRepository DataSetInitialRepository(BriteDatabase briteDatabase,D2 d2) {
        return new DataSetInitialRepositoryImpl(d2,briteDatabase, dataSetUid);
    }

}
