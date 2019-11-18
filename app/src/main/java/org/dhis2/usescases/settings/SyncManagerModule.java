package org.dhis2.usescases.settings;

import org.dhis2.data.dagger.PerFragment;
import org.dhis2.data.prefs.PreferenceProvider;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

/**
 * QUADRAM. Created by frodriguez on 4/13/2018.
 */

@Module
public final class SyncManagerModule {

    @Provides
    @PerFragment
    SyncManagerContracts.Presenter providePresenter(D2 d2, SchedulerProvider schedulerProvider, PreferenceProvider preferenceProvider) {
        return new SyncManagerPresenter(d2, schedulerProvider,preferenceProvider);
    }
}
