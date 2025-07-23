package org.dhis2.usescases.login

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import org.dhis2.commons.di.dagger.PerActivity
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.biometric.BiometricAuthenticator
import org.dhis2.data.biometric.CryptographyManager
import org.dhis2.data.server.UserManager
import org.dhis2.mobile.commons.reporting.CrashReportController
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.login.auth.OpenIdProviders
import org.dhis2.utils.analytics.AnalyticsHelper

@Module
class LoginModule(
    private val view: LoginContracts.View,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val userManager: UserManager?,
) {

    @Provides
    fun provideActivity(): ActivityGlobalAbstract = view.abstractActivity

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
        biometricAuthenticator: BiometricAuthenticator,
        cryptographyManager: CryptographyManager,
        analyticsHelper: AnalyticsHelper,
        crashReportController: CrashReportController,
        networkUtils: NetworkUtils,
        repository: LoginRepository,
    ): LoginViewModel {
        return ViewModelProvider(
            viewModelStoreOwner,
            LoginViewModelFactory(
                view,
                preferenceProvider,
                resourceManager,
                schedulerProvider,
                dispatcherProvider,
                biometricAuthenticator,
                cryptographyManager,
                analyticsHelper,
                crashReportController,
                networkUtils,
                userManager,
                repository,
            ),
        )[LoginViewModel::class.java]
    }

    @Provides
    @PerActivity
    fun provideCryptographyManager(): CryptographyManager {
        return CryptographyManager()
    }

    @Provides
    @PerActivity
    fun openIdProviders(context: Context): OpenIdProviders {
        return OpenIdProviders(context)
    }

    @Provides
    @PerActivity
    fun provideLoginRepository(
        context: Context,
        dispatcherProvider: DispatcherProvider,
    ): LoginRepository {
        return LoginRepository(
            context.resources,
            Gson(),
            dispatcherProvider,
        )
    }
}
