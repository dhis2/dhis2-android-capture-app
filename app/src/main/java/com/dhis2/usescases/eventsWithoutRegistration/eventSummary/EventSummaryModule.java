package com.dhis2.usescases.eventsWithoutRegistration.eventSummary;

import android.support.annotation.NonNull;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialRepository;
import com.squareup.sqlbrite2.BriteDatabase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Cristian on 13/02/2018.
 *
 */
@PerActivity
@Module
public class EventSummaryModule {

    @Provides
    @PerActivity
    EventSummaryContract.View provideView(EventSummaryContract.View activity) {
        return activity;
    }

    @Provides
    @PerActivity
    EventSummaryContract.Presenter providesPresenter(EventSummaryContract.Interactor interactor) {
        return new EventSummaryPresenter(interactor);
    }

    @Provides
    @PerActivity
    EventSummaryContract.Interactor provideInteractor(@NonNull EventSummaryRepository eventSummaryRepository,
                                                      @NonNull MetadataRepository metadataRepository) {
        return new EventSummaryInteractor(eventSummaryRepository, metadataRepository);
    }

    @Provides
    @PerActivity
    EventSummaryRepository eventSummaryRepository(BriteDatabase briteDatabase) {
        return new EventSummaryRepositoryImpl(briteDatabase);
    }
}
