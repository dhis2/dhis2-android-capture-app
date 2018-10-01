package org.dhis2.usescases.dataset.dataSetPeriod;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.dagger.PerActivity;

import dagger.Module;
import dagger.Provides;

/**
 * Created by frodriguez on 7/20/2018.
 */
@Module
public class DataSetPeriodModule {

    @Provides
    @PerActivity
    DataSetPeriodContract.View providesView(DataSetPeriodActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    DataSetPeriodContract.Presenter providesPresenter(DataSetPeriodRepository repository) {
        return new DataSetPeriodPresenter(repository);
    }

    @Provides
    @PerActivity
    DataSetPeriodRepository providesRepository(BriteDatabase briteDatabase) {
        return new DataSetPeriodRepositoryImpl(briteDatabase);
    }

}
