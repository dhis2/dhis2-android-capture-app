package org.dhis2.usescases.datasets.dataSetTable;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;

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
    DataSetTableContract.Presenter providesPresenter(DataSetTableRepository DataSetTableRepository) {
        return new DataSetTablePresenter(DataSetTableRepository);
    }

    @Provides
    @PerActivity
    DataSetTableRepository DataSetTableRepository(BriteDatabase briteDatabase) {
        return new DataSetTableRepositoryImpl(briteDatabase, dataSetUid);
    }

}
