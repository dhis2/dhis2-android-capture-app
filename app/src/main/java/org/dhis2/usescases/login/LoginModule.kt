package org.dhis2.usescases.login

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.reporting.CrashReportController
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.fingerprint.FingerPrintController
import org.dhis2.data.server.UserManager
import org.dhis2.usescases.login.auth.OpenIdProviders
import org.dhis2.utils.analytics.AnalyticsHelper

@Module
class LoginModule(
    private val view: LoginContracts.View,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val userManager: UserManager?,
) {

    @Provides
    @PerActivity
    fun provideResourceManager(
        colorUtils: ColorUtils,
    ) = ResourceManager(view.context, colorUtils)

    @Provides
    @PerActivity
    fun providePresenter(
        preferenceProvider: PreferenceProvider,
        resourceManager: ResourceManager,
        schedulerProvider: SchedulerProvider,
        dispatcherProvider: DispatcherProvider,
        fingerPrintController: FingerPrintController,
        analyticsHelper: AnalyticsHelper,
        crashReportController: CrashReportController,
        networkUtils: NetworkUtils,
    ): LoginViewModel {
        return ViewModelProvider(
            viewModelStoreOwner,
            LoginViewModelFactory(
                view,
                preferenceProvider,
                resourceManager,
                schedulerProvider,
                dispatcherProvider,
                fingerPrintController,
                analyticsHelper,
                crashReportController,
                networkUtils,
                userManager,
            ),
        )[LoginViewModel::class.java]
    }

    @Provides
    @PerActivity
    fun openIdProviders(context: Context): OpenIdProviders {
        return OpenIdProviders(context)
    }
}
