package com.dhis2.usescases.programDetail;

import android.support.annotation.NonNull;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.user.UserRepository;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppajuelo on 31/10/2017.
 */
@Module
public class ProgramDetailModule {


    @Provides
    ProgramDetailContractModule.View provideView(ProgramDetailActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    ProgramDetailContractModule.Presenter providesPresenter(ProgramDetailInteractor interactor) {
        return new ProgramDetailPresenter(interactor);
    }

    @Provides
    @PerActivity
    ProgramDetailContractModule.Interactor provideInteractor(D2 d2, @NonNull UserRepository userRepository, @NonNull ProgramRepository programRepository) {
        return new ProgramDetailInteractor(d2, userRepository, programRepository);
    }

    @Provides
    @PerActivity
    ProgramDetailAdapter provideProgramDetailAdapter(ProgramDetailPresenter presenter) {
        return new ProgramDetailAdapter(presenter);
    }

    @Provides
    ProgramRepository homeRepository(BriteDatabase briteDatabase) {
        return new ProgramRepositoryImpl(briteDatabase);
    }
/*
    interface View extends AbstractActivityContracts.View {

    }

    interface Presenter {
        void init();
    }

    interface Interactor {
        void getData();
    }

    interface Router {

    }*/

}
