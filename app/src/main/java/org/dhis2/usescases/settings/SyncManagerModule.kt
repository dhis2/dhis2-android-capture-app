package org.dhis2.usescases.settings

import dagger.Module
import dagger.Provides
import org.dhis2.R
import org.dhis2.commons.di.dagger.PerFragment
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.service.VersionRepository
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.mobile.commons.files.FileHandlerImpl
import org.dhis2.usescases.settings.domain.CheckVersionUpdate
import org.dhis2.usescases.settings.domain.DeleteLocalData
import org.dhis2.usescases.settings.domain.ExportDatabase
import org.dhis2.usescases.settings.domain.GetSettingsState
import org.dhis2.usescases.settings.domain.GetSyncErrors
import org.dhis2.usescases.settings.domain.LaunchSync
import org.dhis2.usescases.settings.domain.SettingsMessages
import org.dhis2.usescases.settings.domain.UpdateSmsModule
import org.dhis2.usescases.settings.domain.UpdateSmsResponse
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
        updateSmsResponse: UpdateSmsResponse,
        getSyncErrors: GetSyncErrors,
        updateSmsModule: UpdateSmsModule,
        deleteLocalData: DeleteLocalData,
        exportDatabase: ExportDatabase,
        checkVersionUpdate: CheckVersionUpdate,
        launchSync: LaunchSync,
        dispatcherProvider: DispatcherProvider,
        networkUtils: NetworkUtils,
        settingsMessages: SettingsMessages,
    ) = SettingsViewModelFactory(
        getSettingsState,
        updateSyncSettings,
        updateSmsResponse,
        getSyncErrors,
        updateSmsModule,
        deleteLocalData,
        exportDatabase,
        checkVersionUpdate,
        launchSync,
        dispatcherProvider,
        networkUtils,
        settingsMessages,
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
    fun provideUpdateSmsResponse(
        settingsRepository: SettingsRepository,
        gatewayValidator: GatewayValidator,
    ) = UpdateSmsResponse(settingsRepository, gatewayValidator)

    @Provides
    @PerFragment
    fun provideGetSyncErrors(
        settingsRepository: SettingsRepository,
        resourceManager: ResourceManager,
    ) = GetSyncErrors(
        settingsRepository,
        ErrorModelMapper(
            resourceManager.getString(R.string.fk_message),
        ),
    )

    @Provides
    @PerFragment
    fun provideUpdateSmsModule(
        settingsRepository: SettingsRepository,
        gatewayValidator: GatewayValidator,
        settingsMessage: SettingsMessages,
        resourceManager: ResourceManager,
    ) = UpdateSmsModule(
        settingsRepository,
        gatewayValidator,
        settingsMessage,
        resourceManager,
    )

    @Provides
    @PerFragment
    fun provideDeleteLocalData(
        settingsRepository: SettingsRepository,
        settingsMessages: SettingsMessages,
        resourceManager: ResourceManager,
        analyticsHelper: AnalyticsHelper,
    ) = DeleteLocalData(
        settingsRepository,
        settingsMessages,
        resourceManager,
        analyticsHelper,
    )

    @Provides
    @PerFragment
    fun provideExportDatabase(
        settingsRepository: SettingsRepository,
        settingsMessages: SettingsMessages,
        resourceManager: ResourceManager,
    ) = ExportDatabase(
        settingsRepository = settingsRepository,
        fileHandler = FileHandlerImpl(),
        settingsMessages = settingsMessages,
        resourceManager = resourceManager,
    )

    @Provides
    @PerFragment
    fun provideCheckVersionUpdate(
        versionRepository: VersionRepository,
        settingsMessages: SettingsMessages,
        resourceManager: ResourceManager,
    ) = CheckVersionUpdate(
        versionRepository,
        settingsMessages,
        resourceManager,
    )

    @Provides
    @PerFragment
    fun provideLaunchSync(
        workManagerController: WorkManagerController,
        preferenceProvider: PreferenceProvider,
        analyticsHelper: AnalyticsHelper,
    ) = LaunchSync(
        workManagerController,
        preferenceProvider,
        analyticsHelper,
    )

    @Provides
    @PerFragment
    fun provideRepository(
        d2: D2,
        preferenceProvider: PreferenceProvider,
        featureConfigRepository: FeatureConfigRepository,
    ): SettingsRepository =
        SettingsRepository(
            d2,
            preferenceProvider,
            featureConfigRepository,
        )

    @Provides
    @PerFragment
    fun providesGatewayValidator(): GatewayValidator = GatewayValidator()

    @Provides
    @PerFragment
    fun providesSettingsMessage(): SettingsMessages = SettingsMessages()
}
