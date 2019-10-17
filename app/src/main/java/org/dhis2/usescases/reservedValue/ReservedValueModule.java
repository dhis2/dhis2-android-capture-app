package org.dhis2.usescases.reservedValue;

import android.content.Context;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@Module
public class ReservedValueModule {

    private Context context;

    public ReservedValueModule(Context context) {
        this.context = context;
    }

    @PerActivity
    @Provides
    ReservedValueContracts.View provideView(ReservedValueActivity activity) {
        return activity;
    }

    @PerActivity
    @Provides
    ReservedValueContracts.Presenter providePresenter(ReservedValueRepository repository, D2 d2, SchedulerProvider schedulerProvider) {
        return new ReservedValuePresenter(repository, d2, schedulerProvider);
    }

    @PerActivity
    @Provides
    ReservedValueRepository provideRepository(BriteDatabase briteDatabase, D2 d2) {
        return new ReservedValueRepositoryImpl(briteDatabase, d2);
    }
}
