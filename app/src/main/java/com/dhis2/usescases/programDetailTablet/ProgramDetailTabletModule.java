package com.dhis2.usescases.programDetailTablet;

import android.support.annotation.NonNull;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.data.user.UserRepository;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppajuelo on 31/10/2017.
 */
@Module
public class ProgramDetailTabletModule {


    @Provides
    ProgramDetailContractModule.View provideView(ProgramDetailTabletActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    ProgramDetailContractModule.Presenter providesPresenter(ProgramDetailInteractor interactor) {
        return new ProgramDetailPresenter(interactor);
    }

    @Provides
    @PerActivity
    ProgramDetailContractModule.Interactor provideInteractor(D2 d2, @NonNull UserRepository userRepository,
                                                             @NonNull MetadataRepository metadataRepository) {
        return new ProgramDetailInteractor(d2, userRepository, metadataRepository);
    }

}
