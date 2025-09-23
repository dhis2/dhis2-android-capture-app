package org.dhis2.usescases.login

import android.content.Intent
import androidx.annotation.VisibleForTesting
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.dhis2.R
import org.dhis2.commons.Constants.PREFS_URLS
import org.dhis2.commons.Constants.PREFS_USERS
import org.dhis2.commons.Constants.USER_TEST_ANDROID
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.prefs.Preference.Companion.PIN
import org.dhis2.commons.prefs.Preference.Companion.SESSION_LOCKED
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.biometric.BiometricAuthenticator
import org.dhis2.data.biometric.CryptographyManager
import org.dhis2.data.server.UserManager
import org.dhis2.mobile.commons.providers.SECURE_PASS
import org.dhis2.mobile.commons.providers.SECURE_SERVER_URL
import org.dhis2.mobile.commons.providers.SECURE_USER_NAME
import org.dhis2.mobile.commons.reporting.CrashReportController
import org.dhis2.mobile.login.main.domain.model.LoginScreenState
import org.dhis2.mobile.login.main.ui.navigation.Navigator
import org.dhis2.usescases.main.MainActivity
import org.dhis2.utils.TestingCredential
import org.dhis2.utils.analytics.ACCOUNT_RECOVERY
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.DATA_STORE_ANALYTICS_PERMISSION_KEY
import org.dhis2.utils.analytics.LOGIN
import org.dhis2.utils.analytics.USER_PROPERTY_SERVER
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.systeminfo.SystemInfo
import org.hisp.dhis.android.core.user.openid.OpenIDConnectConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import timber.log.Timber
import java.io.File

const val VERSION = "version"

class LoginViewModel(
    private val view: LoginContracts.View,
    private val preferenceProvider: PreferenceProvider,
    private val resourceManager: ResourceManager,
    private val schedulers: SchedulerProvider,
    private val dispatchers: DispatcherProvider,
    private val biometricAuthenticator: BiometricAuthenticator,
    private val cryptographyManager: CryptographyManager,
    private val analyticsHelper: AnalyticsHelper,
    private val crashReportController: CrashReportController,
    private val network: NetworkUtils,
    private var userManager: UserManager?,
    private val repository: LoginRepository,
) : ViewModel() {

    var disposable: CompositeDisposable = CompositeDisposable()
    private val _canLoginWithBiometrics = MutableLiveData<Boolean>()
    val canLoginWithBiometrics: LiveData<Boolean> = _canLoginWithBiometrics

    fun openIdLogin(config: OpenIDConnectConfig) {
        try {
            disposable.add(
                Observable
                    .just(view.initLogin())
                    .flatMap { userManager ->
                        this.userManager = userManager
                        userManager.logIn(config)
                    }.subscribeOn(schedulers.io())
                    .observeOn(schedulers.ui())
                    .subscribe(
                        {
                            view.openOpenIDActivity(it)
                        },
                        {
                            Timber.e(it)
                        },
                    ),
            )
        } catch (throwable: Throwable) {
            Timber.e(throwable)
        }
    }

    fun handleAuthResponseData(
        serverUrl: String,
        data: Intent,
        requestCode: Int,
    ) {
        userManager?.let { userManager ->
            disposable.add(
                userManager
                    .handleAuthData(serverUrl, data, requestCode)
                    .map {
                        run {
                            with(preferenceProvider) {
                                setValue(SESSION_LOCKED, false)
                            }
                            deletePin()
                            Result.success(null)
                        }
                    }.subscribeOn(schedulers.io())
                    .observeOn(schedulers.ui())
                    .subscribe(
                        {},
                        {t->},
                    ),
            )
        }
    }

    fun onDestroy() {
        disposable.clear()
    }

    fun logOut() {
        userManager?.let {
            disposable.add(
                it.d2
                    .userModule()
                    .logOut()
                    .subscribeOn(schedulers.io())
                    .observeOn(schedulers.ui())
                    .subscribe(
                        {
                            preferenceProvider.setValue(SESSION_LOCKED, false)
                            view.handleLogout()
                        },
                        { view.handleLogout() },
                    ),
            )
        }
    }

    fun canEnableBiometric(): Boolean =
        biometricAuthenticator.hasBiometric() && !cryptographyManager.isKeyReady()

    fun saveUserCredentials(
        fragmentActivity: FragmentActivity,
        userPass: String? = null,
        onDone: () -> Unit,
    ) {
        val serverUrl = ""
        val userName = ""
        if (serverUrl.isEmpty() or userName.isEmpty()) return

        if (!preferenceProvider.areSameCredentials(serverUrl, userName)) {
            val pass = userPass ?: serverUrl
            if (canEnableBiometric()) {
                val cryptoObject =
                    cryptographyManager.getInitializedCipherForEncryption()?.let { cipher ->
                        BiometricPrompt.CryptoObject(cipher)
                    }

                biometricAuthenticator.authenticate(
                    fragmentActivity,
                    {
                        val ciphertextWrapper =
                            cryptographyManager.encryptData(pass, it.cryptoObject?.cipher!!)
                        preferenceProvider.saveUserCredentialsAndCipher(
                            serverUrl,
                            userName,
                            ciphertextWrapper,
                        )
                        onDone()
                    }, cryptoObject
                )
            }
            preferenceProvider.saveUserCredentials(
                serverUrl,
                userName,
                pass,
            )
        } else {
            onDone()
        }
    }

    fun authenticateWithBiometric(
        fragmentActivity: FragmentActivity,
    ) {
        val ciphertextWrapper = preferenceProvider.getBiometricCredentials()
        if (ciphertextWrapper != null) {
            val cryptoObject =
                cryptographyManager
                    .getInitializedCipherForDecryption(ciphertextWrapper.initializationVector)
                    ?.let { cipher ->
                        BiometricPrompt.CryptoObject(cipher)
                    }
            biometricAuthenticator.authenticate(
                fragmentActivity,
                {
                   /* password.value =
                        cryptographyManager.decryptData(
                            ciphertextWrapper.ciphertext,
                            it.cryptoObject?.cipher!!,
                        )*/
//                    logIn()
                }, cryptoObject
            )
        }
    }

    fun onAccountRecovery() {
        if (network.isOnline()) {
            analyticsHelper.setEvent(ACCOUNT_RECOVERY, CLICK, ACCOUNT_RECOVERY)
            view.openAccountRecovery()
        } else {
            view.showNoConnectionDialog()
        }
    }

    private fun deletePin() {
        userManager
            ?.d2
            ?.dataStoreModule()
            ?.localDataStore()
            ?.value(PIN)
            ?.blockingDeleteIfExist()
    }

    companion object {
        const val EMPTY_CREDENTIALS = "Empty credentials"
        const val AUTH_ERROR = "AUTH ERROR"
    }

    fun onImportDataBase(file: File) {
        userManager?.let {
            viewModelScope.launch {
                val resultJob =
                    async {
                        try {
                            val importedMetadata =
                                it.d2
                                    .maintenanceModule()
                                    .databaseImportExport()
                                    .importDatabase(file)
                            Result.success(importedMetadata)
                        } catch (e: Exception) {
                            Result.failure(e)
                        }
                    }

                val result = resultJob.await()

                result.fold(
                    onSuccess = {
                        view.displayMessage(resourceManager.getString(R.string.importing_successful))
                    },
                    onFailure = {
                        view.displayMessage(resourceManager.parseD2Error(it))
                    },
                )
            }
        }
    }
}
