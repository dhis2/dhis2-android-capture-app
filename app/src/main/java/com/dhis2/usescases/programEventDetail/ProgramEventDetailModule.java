package com.dhis2.usescases.programEventDetail;

import android.support.annotation.NonNull;

import com.dhis2.data.dagger.PerActivity;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.data.user.UserRepository;
import com.dhis2.usescases.programDetail.ProgramRepository;
import com.dhis2.usescases.programDetail.ProgramRepositoryImpl;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Cristian on 13/02/2018.
 *
 */
@PerActivity
@Module
public class ProgramEventDetailModule {


    @Provides
    @PerActivity
    ProgramEventDetailContract.View provideView(ProgramEventDetailActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    ProgramEventDetailContract.Presenter providesPresenter(ProgramEventDetailContract.Interactor interactor) {
        return new ProgramEventDetailPresenter(interactor);
    }

    @Provides
    @PerActivity
    ProgramEventDetailContract.Interactor provideInteractor(D2 d2, @NonNull UserRepository userRepository,
                                                             @NonNull ProgramEventDetailRepository programEventDetailRepository,
                                                             @NonNull MetadataRepository metadataRepository) {
        return new ProgramEventDetailInteractor(d2, userRepository, programEventDetailRepository, metadataRepository);
    }

    @Provides
    @PerActivity
    ProgramEventDetailAdapter provideProgramEventDetailAdapter(ProgramEventDetailContract.Presenter presenter) {
        return new ProgramEventDetailAdapter(presenter);
    }

    @Provides
    @PerActivity
    ProgramEventDetailRepository eventDetailRepository(BriteDatabase briteDatabase) {
        return new ProgramEventDetailRepositoryImpl(briteDatabase);
    }

    @Provides
    @PerActivity
    ProgramRepository homeRepository(BriteDatabase briteDatabase) {
        return new ProgramRepositoryImpl(briteDatabase);
    }
}
