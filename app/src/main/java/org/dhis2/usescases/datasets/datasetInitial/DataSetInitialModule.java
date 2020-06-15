package org.dhis2.usescases.datasets.datasetInitial;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 24/09/2018.
 */
@PerActivity
@Module
public class DataSetInitialModule {
    private final String dataSetUid;
    private final DataSetInitialContract.View view;

    DataSetInitialModule(DataSetInitialContract.View view, String dataSetUid) {
        this.view = view;
        this.dataSetUid = dataSetUid;
    }

    @Provides
    @PerActivity
    DataSetInitialContract.View provideView(DataSetInitialActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    DataSetInitialContract.Presenter providesPresenter(DataSetInitialRepository dataSetInitialRepository, SchedulerProvider schedulerProvider) {
        return new DataSetInitialPresenter(view, dataSetInitialRepository, schedulerProvider);
    }

    @Provides
    @PerActivity
    DataSetInitialRepository dataSetInitialRepository(D2 d2) {
        return new DataSetInitialRepositoryImpl(d2, dataSetUid);
    }
}
