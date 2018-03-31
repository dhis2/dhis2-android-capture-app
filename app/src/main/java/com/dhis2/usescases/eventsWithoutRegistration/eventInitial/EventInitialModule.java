package com.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.support.annotation.NonNull;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.usescases.programDetail.ProgramRepository;
import com.dhis2.usescases.programDetail.ProgramRepositoryImpl;
import com.dhis2.utils.CodeGenerator;
import com.squareup.sqlbrite2.BriteDatabase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Cristian on 13/02/2018.
 *
 */
@PerActivity
@Module
public class EventInitialModule {

    @Provides
    @PerActivity
    EventInitialContract.View provideView(EventInitialContract.View activity) {
        return activity;
    }

    @Provides
    @PerActivity
    EventInitialContract.Presenter providesPresenter(EventInitialContract.Interactor interactor) {
        return new EventInitialPresenter(interactor);
    }


    @Provides
    @PerActivity
    EventInitialContract.Interactor provideInteractor(@NonNull EventInitialRepository eventInitialRepository,
                                                      @NonNull MetadataRepository metadataRepository) {
        return new EventInitialInteractor(eventInitialRepository, metadataRepository);
    }

    @Provides
    @PerActivity
    EventInitialRepository eventDetailRepository(@NonNull CodeGenerator codeGenerator, BriteDatabase briteDatabase) {
        return new EventInitialRepositoryImpl(codeGenerator, briteDatabase);
    }

    @Provides
    @PerActivity
    ProgramRepository homeRepository(BriteDatabase briteDatabase) {
        return new ProgramRepositoryImpl(briteDatabase);
    }
}
