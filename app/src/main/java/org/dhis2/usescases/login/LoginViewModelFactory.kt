package org.dhis2.usescases.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.biometric.BiometricAuthenticator
import org.dhis2.data.biometric.CryptographyManager
import org.dhis2.data.server.UserManager
import org.dhis2.mobile.commons.reporting.CrashReportController
import org.dhis2.utils.analytics.AnalyticsHelper

class LoginViewModelFactory(
    private val view: LoginContracts.View,
    private val preferenceProvider: PreferenceProvider,
    private val resourceManager: ResourceManager,
    private val schedulerProvider: SchedulerProvider,
    private val dispatcherProvider: DispatcherProvider,
    private val biometricAuthenticator: BiometricAuthenticator,
    private val cryptographyManager: CryptographyManager,
    private val analyticsHelper: AnalyticsHelper,
    private val crashReportController: CrashReportController,
    private val networkUtils: NetworkUtils,
    private val userManager: UserManager?,
    private val repository: LoginRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(
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
        ) as T
    }
}
