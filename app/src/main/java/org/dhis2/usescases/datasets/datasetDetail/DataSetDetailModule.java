package org.dhis2.usescases.datasets.datasetDetail;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@PerActivity
@Module
public class DataSetDetailModule {

    private DataSetDetailView view;

    private final String datasetUid;

    public DataSetDetailModule(DataSetDetailView view, String datasetUid) {
        this.view = view;
        this.datasetUid = datasetUid;
    }

    @Provides
    @PerActivity
    DataSetDetailPresenter providesPresenter(
            DataSetDetailRepository dataSetDetailRepository,
            SchedulerProvider schedulerProvider,
            FilterManager filterManager) {
        return new DataSetDetailPresenter(view, dataSetDetailRepository, schedulerProvider, filterManager);
    }

    @Provides
    @PerActivity
    DataSetDetailRepository eventDetailRepository(D2 d2) {
        return new DataSetDetailRepositoryImpl(datasetUid, d2);
    }
}
