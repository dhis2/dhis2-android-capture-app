package org.dhis2.usescases.settings;

import org.dhis2.data.dagger.PerFragment;
import org.dhis2.data.prefs.PreferenceProvider;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.service.workManager.WorkManagerController;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@Module
public final class SyncManagerModule {

    @Provides
    @PerFragment
    SyncManagerContracts.Presenter providePresenter(
            D2 d2,
            SchedulerProvider schedulerProvider,
            GatewayValidator gatewayValidator,
            PreferenceProvider preferenceProvider,
            WorkManagerController workManagerController,
            SettingsRepository settingsRepository) {
        return new SyncManagerPresenter(d2,
                schedulerProvider,
                gatewayValidator,
                preferenceProvider,
                workManagerController,
                settingsRepository);
    }

    @Provides
    @PerFragment
    SettingsRepository provideRepository(
            D2 d2,
            PreferenceProvider preferenceProvider
    ) {
        return new SettingsRepository(d2,
                preferenceProvider);
    }

    @Provides
    @PerFragment
    GatewayValidator providesGatewayValidator() {
        return new GatewayValidator();
    }
}
