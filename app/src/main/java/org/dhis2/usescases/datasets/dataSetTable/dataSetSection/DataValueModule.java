package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerFragment;
import org.hisp.dhis.android.core.D2;

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
    DataValueContract.Presenter providesPresenter(DataValueRepository repository){
        return new DataValuePresenter(repository);
    }

    @Provides
    @PerFragment
    DataValueRepository DataValueRepository(D2 d2, BriteDatabase briteDatabase) {
        return new DataValueRepositoryImpl(d2,briteDatabase, dataSetUid);
    }
}
