package com.dhis2.usescases.datasets.datasetDetail;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.usescases.programDetail.ProgramRepository;
import com.dhis2.usescases.programDetail.ProgramRepositoryImpl;
import com.dhis2.usescases.programEventDetail.ProgramEventDetailRepository;
import com.dhis2.usescases.programEventDetail.ProgramEventDetailRepositoryImpl;
import com.squareup.sqlbrite2.BriteDatabase;

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
    /*@Provides
    @PerActivity
    ProgramRepository homeRepository(BriteDatabase briteDatabase) {
        return new ProgramRepositoryImpl(briteDatabase);
    }*/
}
