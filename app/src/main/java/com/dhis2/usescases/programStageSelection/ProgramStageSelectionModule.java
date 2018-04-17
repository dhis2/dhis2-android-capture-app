package com.dhis2.usescases.programStageSelection;

import android.support.annotation.NonNull;

import com.dhis2.data.dagger.PerActivity;
import com.squareup.sqlbrite2.BriteDatabase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppajuelo on 31/10/2017.
 */
@PerActivity
@Module
public class ProgramStageSelectionModule {

    @Provides
    @PerActivity
    ProgramStageSelectionContract.View providesView(@NonNull ProgramStageSelectionActivity activity) {
        return activity;
    }

    @Provides
    @PerActivity
    ProgramStageSelectionContract.Presenter providesPresenter(@NonNull ProgramStageSelectionRepository programStageSelectionRepository) {
        return new ProgramStageSelectionPresenter(programStageSelectionRepository);
    }

    @Provides
    @PerActivity
    ProgramStageSelectionRepository providesProgramStageSelectionRepository(@NonNull BriteDatabase briteDatabase) {
        return new ProgramStageSelectionRepositoryImpl(briteDatabase);
    }
}
