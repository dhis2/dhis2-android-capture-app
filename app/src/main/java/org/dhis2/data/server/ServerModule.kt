package org.dhis2.data.server

import android.content.Context
import android.content.ContextWrapper
import dagger.Module
import dagger.Provides
import dhis2.org.analytics.charts.Charts
import dhis2.org.analytics.charts.DhisAnalyticCharts
import okhttp3.Interceptor
import org.dhis2.BuildConfig
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.commons.di.dagger.PerServer
import org.dhis2.commons.filters.data.GetFiltersApplyingWebAppConfig
import org.dhis2.commons.periods.data.EventPeriodRepository
import org.dhis2.commons.periods.domain.GetEventPeriods
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.DhisPeriodUtils
import org.dhis2.commons.resources.EventResourcesProvider
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.service.SyncStatusController
import org.dhis2.data.service.VersionRepository
import org.dhis2.form.data.OptionsRepository
import org.dhis2.form.data.RulesUtilsProvider
import org.dhis2.form.data.RulesUtilsProviderImpl
import org.dhis2.form.data.UniqueAttributeController
import org.dhis2.metadata.usecases.DataSetConfiguration
import org.dhis2.metadata.usecases.ProgramConfiguration
import org.dhis2.metadata.usecases.TrackedEntityTypeConfiguration
import org.dhis2.mobile.commons.files.FileController
import org.dhis2.mobile.commons.files.FileControllerImpl
import org.dhis2.mobile.commons.reporting.CrashReportController
import org.dhis2.ui.ThemeManager
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.AnalyticsInterceptor
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Configuration
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.android.core.D2Manager.blockingInstantiateD2

@Module
class ServerModule {
    @Provides
    @PerServer
    fun sdk(context: Context): D2 {
        if (!D2Manager.isD2Instantiated()) {
            blockingInstantiateD2(getD2Configuration(context))
                ?.userModule()
                ?.accountManager()
                ?.setMaxAccounts(null)
        }
        return D2Manager.getD2()
    }

    @Provides
    @PerServer
    fun sdkInstantiated(): ServerStatus = ServerStatus(D2Manager.isD2Instantiated())

    @Provides
    @PerServer
    fun configurationRepository(
        d2: D2?,
        repository: ServerSettingsRepository,
    ): UserManager = UserManagerImpl(d2!!, repository)

    @Provides
    @PerServer
    fun rulesUtilsProvider(
        d2: D2?,
        optionsRepository: OptionsRepository,
    ): RulesUtilsProvider = RulesUtilsProviderImpl(d2!!, optionsRepository)

    @Provides
    @PerServer
    fun openIdSession(
        d2: D2,
        schedulerProvider: SchedulerProvider,
    ): OpenIdSession = OpenIdSession(d2, schedulerProvider)

    @Provides
    @PerServer
    fun provideCharts(serverComponent: ServerComponent): Charts = DhisAnalyticCharts.Provider.get(serverComponent)

    @Provides
    @PerServer
    fun provideGetFiltersApplyingWebAppConfig(): GetFiltersApplyingWebAppConfig = GetFiltersApplyingWebAppConfig()

    @Provides
    @PerServer
    fun provideDhisPeriodUtils(
        d2: D2,
        context: Context,
    ): DhisPeriodUtils =
        DhisPeriodUtils(
            d2,
            context.getString(R.string.period_span_default_label),
            context.getString(R.string.week_period_span_default_label),
            context.getString(R.string.biweek_period_span_default_label),
        )

    @Provides
    @PerServer
    fun providesRepository(d2: D2): ServerSettingsRepository = ServerSettingsRepository(d2)

    @Provides
    @PerServer
    fun providesThemeManager(
        userManager: UserManager,
        d2: D2,
        preferenceProvider: PreferenceProvider,
        colorUtils: ColorUtils,
    ): ThemeManager =
        ThemeManager(
            userManager,
            ProgramConfiguration(d2),
            DataSetConfiguration(d2),
            TrackedEntityTypeConfiguration(d2),
            preferenceProvider,
            colorUtils,
        )

    @Provides
    @PerServer
    fun providesSyncStatusController(dispatcherProvider: DispatcherProvider): SyncStatusController =
        SyncStatusController(dispatcherProvider)

    @Provides
    @PerServer
    fun providesVersionStatusController(d2: D2): VersionRepository = VersionRepository(d2)

    @Provides
    @PerServer
    fun providesFileController(): FileController = FileControllerImpl()

    @Provides
    @PerServer
    fun providesUniqueAttributeController(
        d2: D2,
        crashReportController: CrashReportController,
    ): UniqueAttributeController =
        UniqueAttributeController(
            d2,
            crashReportController,
        )

    @Provides
    @PerServer
    fun metadataIconProvider(d2: D2): MetadataIconProvider = MetadataIconProvider(d2)

    @Provides
    @PerServer
    fun provideResourceManager(
        context: Context,
        themeManager: ThemeManager,
        colorUtils: ColorUtils,
    ): ResourceManager {
        val contextWrapper = ContextWrapper(context)
        contextWrapper.setTheme(themeManager.getAppTheme())
        return ResourceManager(contextWrapper, colorUtils)
    }

    @Provides
    @PerServer
    fun provideEventPeriodRepository(d2: D2): EventPeriodRepository = EventPeriodRepository(d2)

    @Provides
    @PerServer
    fun providePeriodUseCase(eventPeriodRepository: EventPeriodRepository) = GetEventPeriods(eventPeriodRepository)

    companion object {
        @JvmStatic
        fun getD2Configuration(context: Context): D2Configuration {
            val interceptors: MutableList<Interceptor> =
                ArrayList()
            interceptors.add(
                AnalyticsInterceptor(
                    AnalyticsHelper(context.app().appComponent().matomoController()),
                ),
            )
            return D2Configuration
                .builder()
                .appName(BuildConfig.APPLICATION_ID)
                .appVersion(BuildConfig.VERSION_NAME)
                .connectTimeoutInSeconds(10 * 60)
                .readTimeoutInSeconds(10 * 60)
                .networkInterceptors(interceptors)
                .writeTimeoutInSeconds(10 * 60)
                .context(context)
                .build()
        }
    }

    @Provides
    @PerServer
    fun provideOptionsRepository(d2: D2): OptionsRepository = OptionsRepository(d2)

    @Provides
    @PerServer
    fun provideEventResourceProvider(
        d2: D2,
        resourceManager: ResourceManager,
    ): EventResourcesProvider = EventResourcesProvider(d2, resourceManager)
}
