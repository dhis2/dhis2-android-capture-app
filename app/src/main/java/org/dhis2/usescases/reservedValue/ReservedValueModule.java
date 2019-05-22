package org.dhis2.usescases.reservedValue;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@Module
public class ReservedValueModule {

    @PerActivity
    @Provides
    ReservedValueContracts.ReservedValueView provideView(ReservedValueActivity activity) {
        return activity;
    }

    @PerActivity
    @Provides
    ReservedValueContracts.ReservedValuePresenter providePresenter(ReservedValueRepository repository, D2 d2) {
        return new ReservedValuePresenterImpl(repository, d2);
    }

    @PerActivity
    @Provides
    ReservedValueRepository provideRepository(BriteDatabase briteDatabase) {
        return new ReservedValueRepositoryImpl(briteDatabase);
    }
}
