package org.dhis2.usescases.main.program;

import android.content.Context;

import org.dhis2.R;
import org.dhis2.data.dagger.PerFragment;
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
    ProgramContract.Presenter programPresenter(HomeRepository homeRepository) {
        return new ProgramPresenter(homeRepository);
    }

    @Provides
    @PerFragment
    HomeRepository homeRepository(D2 d2, Context context) {
        String eventsLabel = context.getString(R.string.events);
        return new HomeRepositoryImpl(d2, eventsLabel);
    }


}
