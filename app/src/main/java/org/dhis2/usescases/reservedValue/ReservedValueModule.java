package org.dhis2.usescases.reservedValue;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@Module
public class ReservedValueModule {

    private ReservedValueContracts.View view;

    ReservedValueModule(ReservedValueActivity view) {
        this.view = view;
    }

    @PerActivity
    @Provides
    ReservedValuePresenter providePresenter(ReservedValueRepository repository, D2 d2, SchedulerProvider schedulerProvider) {
        return new ReservedValuePresenter(repository, d2, schedulerProvider, view);
    }

    @PerActivity
    @Provides
    ReservedValueRepository provideRepository(D2 d2) {
        return new ReservedValueRepositoryImpl(d2);
    }
}
