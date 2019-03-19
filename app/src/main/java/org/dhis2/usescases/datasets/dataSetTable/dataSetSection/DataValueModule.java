package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerFragment;
import org.dhis2.data.metadata.MetadataRepository;

import dagger.Module;
import dagger.Provides;

@Module
public class DataValueModule {

    private String dataSetUid;

    DataValueModule(String dataSetUid) {
        this.dataSetUid = dataSetUid;
    }

    @Provides
    @PerFragment
    DataValueContract.View provideView(DataSetSectionFragment fragment){
        return fragment;
    }

    @Provides
    @PerFragment
    DataValueContract.Presenter providesPresenter(DataValueRepository repository, MetadataRepository metadataRepository){
        return new DataValuePresenter(repository, metadataRepository);
    }

    @Provides
    @PerFragment
    DataValueRepository DataValueRepository(BriteDatabase briteDatabase) {
        return new DataValueRepositoryImpl(briteDatabase, dataSetUid);
    }
}
