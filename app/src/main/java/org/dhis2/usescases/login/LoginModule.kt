package org.dhis2.usescases.login

import android.content.Context
import dagger.Module
import dagger.Provides
import org.dhis2.data.dagger.PerActivity
import org.dhis2.data.fingerprint.FingerPrintController
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.usescases.login.auth.OpenIdProviders
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.reporting.CrashReportController

/**
 * QUADRAM. Created by ppajuelo on 07/02/2018.
 */

@Module
@PerActivity
class LoginModule(private val view: LoginContracts.View) {

    @Provides
    @PerActivity
    fun providePresenter(
        preferenceProvider: PreferenceProvider,
        schedulerProvider: SchedulerProvider,
        fingerPrintController: FingerPrintController,
        analyticsHelper: AnalyticsHelper,
        crashReportController: CrashReportController
    ): LoginPresenter {
        return LoginPresenter(
            view,
            preferenceProvider,
            schedulerProvider,
            fingerPrintController,
            analyticsHelper,
            crashReportController
        )
    }

    @Provides
    @PerActivity
    fun openIdProviders(context: Context): OpenIdProviders {
        return OpenIdProviders(context)
    }
}
