package org.dhis2.usescases.datasets.datasetDetail;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;

import dagger.Module;
import dagger.Provides;

@PerActivity
@Module
public class DataSetDetailModule {


    @Provides
    @PerActivity
    DataSetDetailContract.DataSetDetailView provideView(DataSetDetailActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    DataSetDetailContract.DataSetDetailPresenter providesPresenter(DataSetDetailRepository dataSetDetailRepository) {
        return new DataSetDetailPresenterImpl(dataSetDetailRepository);
    }

    @Provides
    @PerActivity
    DataSetDetailRepository eventDetailRepository(BriteDatabase briteDatabase) {
        return new DataSetDetailRepositoryImpl(briteDatabase);
    }
}
