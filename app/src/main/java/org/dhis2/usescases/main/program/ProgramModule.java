package org.dhis2.usescases.main.program;

import org.dhis2.data.dagger.PerFragment;

import com.squareup.sqlbrite2.BriteDatabase;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by ppajuelo on 07/02/2018.
 */
@Module
@PerFragment
public class ProgramModule {

    @Provides
    @PerFragment
    ProgramContract.ProgramPresenter programPresenter(HomeRepository homeRepository) {
        return new ProgramPresenterImpl(homeRepository);
    }

    @Provides
    @PerFragment
    HomeRepository homeRepository(BriteDatabase briteDatabase) {
        return new HomeRepositoryImpl(briteDatabase);
    }


}
