package org.dhis2.usescases.main.program;

import org.dhis2.data.dagger.PerFragment;
import org.dhis2.data.sharedPreferences.SharePreferencesProvider;
import org.hisp.dhis.android.core.D2;

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
    ProgramContract.Presenter programPresenter(SharePreferencesProvider provider, HomeRepository homeRepository) {
        return new ProgramPresenter(provider, homeRepository);
    }

    @Provides
    @PerFragment
    HomeRepository homeRepository(D2 d2) {
        return new HomeRepositoryImpl(d2);
    }


}
