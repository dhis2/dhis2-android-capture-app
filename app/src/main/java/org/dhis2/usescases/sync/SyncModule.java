package org.dhis2.usescases.sync;

import androidx.work.WorkManager;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@Module
@PerActivity
public class SyncModule {

    private SyncView view;

    SyncModule(SyncView view) {
        this.view = view;
    }

    @Provides
    @PerActivity
    SyncPresenter providePresenter(D2 d2, SchedulerProvider schedulerProvider, WorkManager workManager) {
        return new SyncPresenter(view, d2, schedulerProvider, workManager);
    }
}
