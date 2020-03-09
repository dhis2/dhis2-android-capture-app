package org.dhis2.usescases.settings;

import org.dhis2.data.dagger.PerFragment;
import org.dhis2.data.prefs.PreferenceProvider;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@Module
public final class SyncManagerModule {

    @Provides
    @PerFragment
    SyncManagerContracts.Presenter providePresenter(D2 d2, SchedulerProvider schedulerProvider, GatewayValidator gatewayValidator ,PreferenceProvider preferenceProvider) {
        return new SyncManagerPresenter(d2, schedulerProvider,gatewayValidator ,preferenceProvider);
    }

    @Provides @PerFragment
    GatewayValidator providesGatewayValidator(){
        return new GatewayValidator();
    }
}
