package org.dhis2.data.server

import android.content.Context
import dagger.Module
import dagger.Provides
import dhis2.org.analytics.charts.Charts
import dhis2.org.analytics.charts.DhisAnalyticCharts
import java.util.ArrayList
import okhttp3.Interceptor
import org.dhis2.Bindings.app
import org.dhis2.BuildConfig
import org.dhis2.R
import org.dhis2.commons.di.dagger.PerServer
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.dhislogic.DhisPeriodUtils
import org.dhis2.data.filter.GetFiltersApplyingWebAppConfig
import org.dhis2.utils.RulesUtilsProvider
import org.dhis2.utils.RulesUtilsProviderImpl
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.AnalyticsInterceptor
import org.dhis2.utils.reporting.SentryOkHttpInterceptor
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.D2Configuration
import org.hisp.dhis.android.core.D2Manager

@Module
@PerServer
class ServerModule {
    @Provides
    @PerServer
    fun sdk(): D2 {
        return D2Manager.getD2()
    }

    @Provides
    @PerServer
    fun configurationRepository(d2: D2?, repository: ServerSettingsRepository): UserManager {
        return UserManagerImpl(d2!!, repository)
    }

    @Provides
    @PerServer
    fun rulesUtilsProvider(d2: D2?): RulesUtilsProvider {
        return RulesUtilsProviderImpl(d2!!)
    }

    @Provides
    @PerServer
    fun openIdSession(d2: D2, schedulerProvider: SchedulerProvider): OpenIdSession {
        return OpenIdSession(d2, schedulerProvider)
    }

    @Provides
    @PerServer
    fun provideCharts(serverComponent: ServerComponent): Charts {
        return DhisAnalyticCharts.Provider.get(serverComponent)
    }

    @Provides
    @PerServer
    fun provideGetFiltersApplyingWebAppConfig(): GetFiltersApplyingWebAppConfig {
        return GetFiltersApplyingWebAppConfig()
    }

    @Provides
    @PerServer
    fun provideDhisPeriodUtils(d2: D2, context: Context): DhisPeriodUtils {
        return DhisPeriodUtils(
            d2,
            context.getString(R.string.period_span_default_label),
            context.getString(R.string.week_period_span_default_label),
            context.getString(R.string.biweek_period_span_default_label)
        )
    }

    @Provides
    @PerServer
    fun providesRepository(d2: D2, systemStyleMapper: SystemStyleMapper): ServerSettingsRepository {
        return ServerSettingsRepository(d2, systemStyleMapper)
    }

    companion object {
        @JvmStatic
        fun getD2Configuration(context: Context): D2Configuration {
            val interceptors: MutableList<Interceptor> =
                ArrayList()
            context.app().appInspector.flipperInterceptor?.let { flipperInterceptor ->
                interceptors.add(flipperInterceptor)
            }
            interceptors.add(
                AnalyticsInterceptor(
                    AnalyticsHelper(context.app().appComponent().matomoController())
                )
            )
            interceptors.add(
                SentryOkHttpInterceptor(
                    context.app().appComponent().preferenceProvider()
                )
            )
            return D2Configuration.builder()
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
}
