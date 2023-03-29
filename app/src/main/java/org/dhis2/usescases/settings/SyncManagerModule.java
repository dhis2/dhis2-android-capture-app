package org.dhis2.usescases.settings;


import org.dhis2.R;
import org.dhis2.commons.di.dagger.PerFragment;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.commons.viewmodel.DispatcherProvider;
import org.dhis2.data.server.ServerComponent;
import org.dhis2.data.server.UserManager;
import org.dhis2.data.service.VersionRepository;
import org.dhis2.data.service.workManager.WorkManagerController;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.usescases.settings.models.ErrorModelMapper;
import org.dhis2.commons.matomo.MatomoAnalyticsController;
import org.hisp.dhis.android.core.D2;

import dagger.Module;
import dagger.Provides;

@Module
public final class SyncManagerModule {

    private final SyncManagerContracts.View view;
    private final UserManager userManager;

    SyncManagerModule(SyncManagerContracts.View view, ServerComponent serverComponent){
        this.view = view;
        this.userManager = serverComponent.userManager();
    }

    @Provides
    @PerFragment
    SyncManagerPresenter providePresenter(
            D2 d2,
            SchedulerProvider schedulerProvider,
            GatewayValidator gatewayValidator,
            PreferenceProvider preferenceProvider,
            WorkManagerController workManagerController,
            SettingsRepository settingsRepository,
            AnalyticsHelper analyticsHelper,
            MatomoAnalyticsController matomoAnalyticsController,
            ResourceManager resourceManager,
            VersionRepository versionRepository,
            DispatcherProvider dispatcherProvider
    ) {
        return new SyncManagerPresenter(d2,
                schedulerProvider,
                gatewayValidator,
                preferenceProvider,
                workManagerController,
                settingsRepository,
                userManager,
                view,
                analyticsHelper,
                new ErrorModelMapper(view.getContext().getString(R.string.fk_message)),
                matomoAnalyticsController,
                resourceManager,
                versionRepository,
                dispatcherProvider
        );
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
