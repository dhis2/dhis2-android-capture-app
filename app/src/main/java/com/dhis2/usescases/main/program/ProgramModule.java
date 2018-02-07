package com.dhis2.usescases.main.program;

import com.dhis2.data.dagger.PerFragment;
import com.squareup.sqlbrite2.BriteDatabase;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ppajuelo on 07/02/2018.
 */
@Module
@PerFragment
public class ProgramModule {

    @Provides
    @PerFragment
    ProgramContract.Presenter programPresenter(HomeRepository homeRepository) {
        return new ProgramPresenter(homeRepository);
    }

    @Provides
    @PerFragment
    HomeRepository homeRepository(BriteDatabase briteDatabase) {
        return new HomeRepositoryImpl(briteDatabase);
    }


}
