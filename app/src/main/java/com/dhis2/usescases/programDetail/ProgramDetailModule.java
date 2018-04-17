package com.dhis2.usescases.programDetail;

import android.support.annotation.NonNull;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.data.user.UserRepository;
import com.squareup.sqlbrite2.BriteDatabase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppajuelo on 31/10/2017.
 */
@PerActivity
@Module
public class ProgramDetailModule {


    @Provides
    @PerActivity
    ProgramDetailContractModule.View provideView(ProgramDetailActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    ProgramDetailContractModule.Presenter providesPresenter(ProgramDetailContractModule.Interactor interactor) {
        return new ProgramDetailPresenter(interactor);
    }

    @Provides
    @PerActivity
    ProgramDetailContractModule.Interactor provideInteractor(@NonNull UserRepository userRepository,
                                                             @NonNull ProgramRepository programRepository,
                                                             @NonNull MetadataRepository metadataRepository) {
        return new ProgramDetailInteractor(userRepository, programRepository, metadataRepository);
    }

    @Provides
    @PerActivity
    ProgramDetailAdapter provideProgramDetailAdapter(ProgramDetailContractModule.Presenter presenter, ProgramRepository programRepository) {
        return new ProgramDetailAdapter(presenter, programRepository);
    }

    @Provides
    @PerActivity
    ProgramRepository homeRepository(BriteDatabase briteDatabase) {
        return new ProgramRepositoryImpl(briteDatabase);
    }
}
