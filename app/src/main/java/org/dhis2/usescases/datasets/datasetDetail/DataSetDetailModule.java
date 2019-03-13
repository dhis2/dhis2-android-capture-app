package org.dhis2.usescases.datasets.datasetDetail;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.metadata.MetadataRepository;

import dagger.Module;
import dagger.Provides;

@PerActivity
@Module
public class DataSetDetailModule {


    @Provides
    @PerActivity
    DataSetDetailContract.View provideView(DataSetDetailActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    DataSetDetailContract.Presenter providesPresenter(DataSetDetailRepository dataSetDetailRepository,
                                                      MetadataRepository metadataRepository) {
        return new DataSetDetailPresenter(dataSetDetailRepository, metadataRepository);
    }

    @Provides
    @PerActivity
    DataSetDetailRepository eventDetailRepository(BriteDatabase briteDatabase) {
        return new DataSetDetailRepositoryImpl(briteDatabase);
    }
}
