package org.dhis2.usescases.login

import android.content.Context
import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.fingerprint.FingerPrintController
import org.dhis2.usescases.login.auth.OpenIdProviders
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.reporting.CrashReportController
/**
 * QUADRAM. Created by ppajuelo on 07/02/2018.
 */

@Module
class LoginModule(private val view: LoginContracts.View) {

    @Provides
    @PerActivity
    fun providePresenter(
        preferenceProvider: PreferenceProvider,
        schedulerProvider: SchedulerProvider,
        fingerPrintController: FingerPrintController,
        analyticsHelper: AnalyticsHelper,
        crashReportController: CrashReportController,
        networkUtils: NetworkUtils
    ): LoginPresenter {
        return LoginPresenter(
            view,
            preferenceProvider,
            schedulerProvider,
            fingerPrintController,
            analyticsHelper,
            crashReportController,
            networkUtils
        )
    }

    @Provides
    @PerActivity
    fun openIdProviders(context: Context): OpenIdProviders {
        return OpenIdProviders(context)
    }
}
