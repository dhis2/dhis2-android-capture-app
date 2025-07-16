package org.dhis2.usescases.settings

import dagger.Module
import dagger.Provides
import org.dhis2.R
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.service.VersionRepository
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.mobile.commons.files.FileHandlerImpl
import org.dhis2.usescases.settings.domain.GetSettingsState
import org.dhis2.usescases.settings.domain.UpdateSyncSettings
import org.dhis2.usescases.settings.models.ErrorModelMapper
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2

@Module
class SyncManagerModule {

    @Provides
    @PerFragment
    fun provideViewModelFactory(
        getSettingsState: GetSettingsState,
        updateSyncSettings: UpdateSyncSettings,
        gatewayValidator: GatewayValidator,
        preferenceProvider: PreferenceProvider,
        workManagerController: WorkManagerController,
        settingsRepository: SettingsRepository,
        analyticsHelper: AnalyticsHelper,
        resourceManager: ResourceManager,
        versionRepository: VersionRepository,
        dispatcherProvider: DispatcherProvider,
        networkUtils: NetworkUtils,
    ) = SettingsViewModelFactory(
        getSettingsState,
        updateSyncSettings,
        gatewayValidator,
        preferenceProvider,
        workManagerController,
        settingsRepository,
        analyticsHelper,
        ErrorModelMapper(resourceManager.getString(R.string.fk_message)),
        resourceManager,
        versionRepository,
        dispatcherProvider,
        networkUtils,
        FileHandlerImpl(),
    )

    @Provides
    @PerFragment
    fun provideGetSettingsState(
        settingsRepository: SettingsRepository,
        gatewayValidator: GatewayValidator,
    ) = GetSettingsState(
        settingsRepository,
        gatewayValidator,
    )

    @Provides
    @PerFragment
    fun provideUpdateSyncSettings(
        settingsRepository: SettingsRepository,
        analyticsHelper: AnalyticsHelper,
    ) = UpdateSyncSettings(settingsRepository, analyticsHelper)

    @Provides
    @PerFragment
    fun provideRepository(
        d2: D2,
        preferenceProvider: PreferenceProvider,
    ): SettingsRepository {
        return SettingsRepository(
            d2,
            preferenceProvider,
        )
    }

    @Provides
    @PerFragment
    fun providesGatewayValidator(): GatewayValidator {
        return GatewayValidator()
    }
}
