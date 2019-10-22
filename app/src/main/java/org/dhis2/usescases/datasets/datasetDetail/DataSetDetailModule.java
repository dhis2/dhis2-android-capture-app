package org.dhis2.usescases.datasets.datasetDetail;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@PerActivity
@Module
public class DataSetDetailModule {


    private final String datasetUid;

    public DataSetDetailModule(String datasetUid) {
        this.datasetUid = datasetUid;
    }

    @Provides
    @PerActivity
    DataSetDetailContract.View provideView(DataSetDetailActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    DataSetDetailContract.Presenter providesPresenter(DataSetDetailRepository dataSetDetailRepository, SchedulerProvider schedulerProvider) {
        return new DataSetDetailPresenter(dataSetDetailRepository, schedulerProvider);
    }

    @Provides
    @PerActivity
    DataSetDetailRepository eventDetailRepository(D2 d2) {
        return new DataSetDetailRepositoryImpl(datasetUid, d2);
    }
}
