package com.dhis2.usescases.eventsWithoutRegistration.eventInfoSections;

import android.support.annotation.NonNull;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.usescases.programDetail.ProgramRepository;
import com.dhis2.usescases.programDetail.ProgramRepositoryImpl;
import com.squareup.sqlbrite2.BriteDatabase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Cristian on 13/02/2018.
 *
 */
@PerActivity
@Module
public class EventInfoSectionsModule {

    @Provides
    @PerActivity
    EventInfoSectionsContract.View provideView(EventInfoSectionsContract.View activity) {
        return activity;
    }

    @Provides
    @PerActivity
    EventInfoSectionsContract.Presenter providesPresenter(EventInfoSectionsContract.Interactor interactor) {
        return new EventInfoSectionsPresenter(interactor);
    }

    @Provides
    @PerActivity
    EventInfoSectionsContract.Interactor provideInteractor(@NonNull EventInfoSectionsRepository eventInfoSectionsRepository,
                                                      @NonNull MetadataRepository metadataRepository) {
        return new EventInfoSectionsInteractor(eventInfoSectionsRepository, metadataRepository);
    }

    @Provides
    @PerActivity
    EventInfoSectionsRepository eventInfoSectionsRepository(BriteDatabase briteDatabase) {
        return new EventInfoSectionsRepositoryImpl(briteDatabase);
    }

    @Provides
    @PerActivity
    ProgramRepository homeRepository(BriteDatabase briteDatabase) {
        return new ProgramRepositoryImpl(briteDatabase);
    }
}
