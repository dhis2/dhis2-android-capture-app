package org.dhis2.usescases.login

import android.content.Intent
import androidx.annotation.VisibleForTesting
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
import org.dhis2.commons.data.tuples.Trio
import org.dhis2.commons.idlingresource.CountingIdlingResourceSingleton.decrement
import org.dhis2.commons.idlingresource.CountingIdlingResourceSingleton.increment
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.prefs.Preference.Companion.PIN
import org.dhis2.commons.prefs.Preference.Companion.SESSION_LOCKED
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.prefs.SECURE_PASS
import org.dhis2.commons.prefs.SECURE_SERVER_URL
import org.dhis2.commons.prefs.SECURE_USER_NAME
import org.dhis2.commons.reporting.CrashReportController
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.biometric.BiometricController
import org.dhis2.data.server.UserManager
import org.dhis2.usescases.main.MainActivity
import org.dhis2.utils.TestingCredential
import org.dhis2.utils.analytics.ACCOUNT_RECOVERY
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.DATA_STORE_ANALYTICS_PERMISSION_KEY
import org.dhis2.utils.analytics.LOGIN
import org.dhis2.utils.analytics.SERVER_QR_SCANNER
import org.dhis2.utils.analytics.USER_PROPERTY_SERVER
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.systeminfo.SystemInfo
import org.hisp.dhis.android.core.user.openid.OpenIDConnectConfig
import timber.log.Timber
import java.io.File

const val VERSION = "version"

class LoginViewModel(
    private val view: LoginContracts.View,
    private val preferenceProvider: PreferenceProvider,
    private val resourceManager: ResourceManager,
    private val schedulers: SchedulerProvider,
    private val dispatchers: DispatcherProvider,
    private val biometricController: BiometricController,
    private val analyticsHelper: AnalyticsHelper,
    private val crashReportController: CrashReportController,
    private val network: NetworkUtils,
    private var userManager: UserManager?,
) : ViewModel() {

    private val syncIsPerformedInteractor = SyncIsPerformedInteractor(userManager)
    var disposable: CompositeDisposable = CompositeDisposable()

    val serverUrl = MutableLiveData<String>()
    val userName = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val isDataComplete = MutableLiveData<Boolean>()
    val isTestingEnvironment = MutableLiveData<Trio<String, String, String>>()
    var testingCredentials: MutableMap<String, TestingCredential>? = null
    private val _loginProgressVisible = MutableLiveData(false)
    val loginProgressVisible: LiveData<Boolean> = _loginProgressVisible

    private val _hasAccounts = MutableLiveData<Boolean>()
    val hasAccounts: LiveData<Boolean> = _hasAccounts

    private val _canLoginWithBiometrics = MutableLiveData<Boolean>()
    val canLoginWithBiometrics: LiveData<Boolean> = _canLoginWithBiometrics

    private val _displayMoreActions = MutableLiveData<Boolean>(true)
    val displayMoreActions: LiveData<Boolean> = _displayMoreActions

    init {
        this.userManager?.let {
            disposable.add(
                it.isUserLoggedIn
                    .subscribeOn(schedulers.io())
                    .observeOn(schedulers.ui())
                    .subscribe(
                        { isUserLoggedIn ->
                            val isSessionLocked =
                                preferenceProvider.getBoolean(SESSION_LOCKED, false)
                            if (isUserLoggedIn && !isSessionLocked) {
                                view.startActivity(MainActivity::class.java, null, true, true, null)
                            } else if (isSessionLocked) {
                                view.showUnlockButton()
                            }
                            if (!isUserLoggedIn) {
                                val serverUrl =
                                    preferenceProvider.getString(
                                        SECURE_SERVER_URL,
                                        view.getDefaultServerProtocol(),
                                    )
                                val user = preferenceProvider.getString(SECURE_USER_NAME, "")
                                if (!serverUrl.isNullOrEmpty() && !user.isNullOrEmpty()) {
                                    setAccountInfo(serverUrl, user)
                                } else {
                                    setAccountInfo(view.getDefaultServerProtocol(), null)
                                }
                            }
                        },
                        { exception -> Timber.e(exception) },
                    ),
            )
        } ?: setAccountInfo(view.getDefaultServerProtocol(), null)
        displayManageAccount()
    }

    private fun trackServerVersion() {
        userManager?.d2?.systemInfoModule()?.systemInfo()?.blockingGet()?.version()
            ?.let { analyticsHelper.trackMatomoEvent(USER_PROPERTY_SERVER, VERSION, it) }
    }

    fun checkServerInfoAndShowBiometricButton() {
        userManager?.let { userManager ->
            disposable.add(
                Observable.just(getSystemInfoIfUserIsLogged(userManager))
                    .subscribeOn(schedulers.io())
                    .observeOn(schedulers.ui())
                    .subscribe(
                        { systemInfo ->
                            setServerAndUserInfo(systemInfo.contextPath())
                            checkBiometricVisibility()
                        },
                        { Timber.e(it) },
                    ),
            )
        } ?: setAccountInfo(view.getDefaultServerProtocol(), null)
    }

    private fun setServerAndUserInfo(contextPath: String?) {
        contextPath?.let {
            setAccountInfo(it, userManager?.userName()?.blockingGet())
        } ?: {
            val isSessionLocked =
                preferenceProvider.getBoolean(SESSION_LOCKED, false)
            val serverUrl =
                preferenceProvider.getString(
                    SECURE_SERVER_URL,
                    view.getDefaultServerProtocol(),
                )
            val user = preferenceProvider.getString(SECURE_USER_NAME, "")

            if (!isSessionLocked && !serverUrl.isNullOrEmpty() && !user.isNullOrEmpty()) {
                setAccountInfo(serverUrl, user)
            } else {
                setAccountInfo(view.getDefaultServerProtocol(), null)
            }
        }
    }

    private fun getSystemInfoIfUserIsLogged(userManager: UserManager): SystemInfo {
        return if (userManager.isUserLoggedIn.blockingFirst() &&
            userManager.d2.systemInfoModule().systemInfo().blockingGet() != null
        ) {
            userManager.d2.systemInfoModule().systemInfo().blockingGet() ?: SystemInfo.builder()
                .build()
        } else {
            SystemInfo.builder().build()
        }
    }

    fun checkBiometricVisibility() {
        _canLoginWithBiometrics.value =
            biometricController.hasBiometric() &&
            userManager?.d2?.userModule()?.accountManager()?.getAccounts()?.count() == 1 &&
            preferenceProvider.getString(SECURE_SERVER_URL)?.let { it == serverUrl.value } ?: false &&
            preferenceProvider.contains(SECURE_PASS)
    }

    fun onLoginButtonClick() {
        try {
            view.hideKeyboard()
            analyticsHelper.setEvent(LOGIN, CLICK, LOGIN)
            logIn()
        } catch (throwable: Throwable) {
            Timber.e(throwable)
            handleError(throwable)
        }
    }

    private fun logIn() {
        _loginProgressVisible.postValue(true)
        increment()
        disposable.add(
            Observable.just(view.initLogin())
                .flatMap { userManager ->
                    this.userManager = userManager
                    userManager.logIn(
                        userName.value!!.trim { it <= ' ' },
                        password.value!!,
                        serverUrl.value!!,
                    )
                        .map {
                            run {
                                with(preferenceProvider) {
                                    setValue(SESSION_LOCKED, false)
                                }
                                deletePin()
                                Result.success(null)
                            }
                        }
                }
                .doOnTerminate {
                    decrement()
                    _loginProgressVisible.postValue(false)
                }
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe(
                    this::handleResponse,
                    this::handleError,
                ),
        )
    }

    fun openIdLogin(config: OpenIDConnectConfig) {
        try {
            disposable.add(
                Observable.just(view.initLogin())
                    .flatMap { userManager ->
                        this.userManager = userManager
                        userManager.logIn(config)
                    }
                    .subscribeOn(schedulers.io())
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
            handleError(throwable)
        }
    }

    fun handleAuthResponseData(serverUrl: String, data: Intent, requestCode: Int) {
        userManager?.let { userManager ->
            disposable.add(
                userManager.handleAuthData(serverUrl, data, requestCode)
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
                        this::handleResponse,
                        this::handleError,
                    ),
            )
        }
    }

    private fun trackUserInfo() {
        val serverVersion =
            userManager?.d2?.systemInfoModule()?.systemInfo()?.blockingGet()?.version() ?: ""
        crashReportController.trackServer(serverUrl.value, serverVersion)
        crashReportController.trackUser(userName.value, serverUrl.value)
    }

    fun onQRClick() {
        analyticsHelper.setEvent(SERVER_QR_SCANNER, CLICK, SERVER_QR_SCANNER)
        view.navigateToQRActivity()
    }

    fun onDestroy() {
        disposable.clear()
    }

    fun logOut() {
        userManager?.let {
            disposable.add(
                it.d2.userModule().logOut()
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

    @VisibleForTesting
    fun handleResponse(userResponse: Result<*>) {
        if (userResponse.isSuccess) {
            updateServerUrls()
            updateLoginUsers()
            val displayTrackingMessage = hasToDisplayTrackingMessage()
            val isInitialSyncDone = syncIsPerformedInteractor.execute()
            view.saveUsersData(displayTrackingMessage, isInitialSyncDone)
        }
    }

    private fun updateServerUrls() {
        (preferenceProvider.getSet(PREFS_URLS, HashSet()) as HashSet).apply {
            serverUrl.value?.let {
                if (!contains(it)) {
                    add(it)
                }
                preferenceProvider.setValue(PREFS_URLS, this)
            }
        }
    }

    private fun updateLoginUsers() {
        (preferenceProvider.getSet(PREFS_USERS, HashSet()) as HashSet).apply {
            userName.value?.let {
                if (!contains(it)) {
                    add(it)
                }
                preferenceProvider.setValue(PREFS_USERS, this)
            }
        }
    }

    private fun handleError(throwable: Throwable) {
        Timber.e(throwable)
        if (throwable is D2Error && throwable.errorCode() == D2ErrorCode.ALREADY_AUTHENTICATED) {
            userManager?.d2?.userModule()?.blockingLogOut()
            logIn()
        } else {
            view.renderError(throwable)
        }
    }

    private fun hasToDisplayTrackingMessage(): Boolean {
        return userManager?.d2?.dataStoreModule()?.localDataStore()
            ?.value(DATA_STORE_ANALYTICS_PERMISSION_KEY)
            ?.blockingGet()?.value() == null
    }

    fun saveUserCredentials(userPass: String? = null) {
        if (!preferenceProvider.areSameCredentials(serverUrl.value!!, userName.value!!)) {
            preferenceProvider.saveUserCredentials(
                serverUrl.value!!,
                userName.value!!,
                userPass,
            )
        }
    }

    fun authenticateWithBiometric() {
        biometricController.authenticate {
            password.value = preferenceProvider.getString(SECURE_PASS)
            logIn()
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

    fun getAutocompleteData(
        testingCredentials: List<TestingCredential>,
    ): Pair<MutableList<String>, MutableList<String>> {
        val urls = preferenceProvider.getSet(PREFS_URLS, emptySet())!!.toMutableList()
        val users = preferenceProvider.getSet(PREFS_USERS, emptySet())!!.toMutableList()

        urls.let {
            for (testingCredential in testingCredentials) {
                if (!it.contains(testingCredential.server_url)) {
                    it.add(testingCredential.server_url)
                }
            }
        }

        preferenceProvider.setValue(PREFS_URLS, HashSet(urls))

        users.let {
            if (!it.contains(USER_TEST_ANDROID)) {
                it.add(USER_TEST_ANDROID)
            }
        }

        preferenceProvider.setValue(PREFS_USERS, HashSet(users))

        return Pair(urls, users)
    }

    fun displayManageAccount() {
        val users = userManager?.d2?.userModule()?.accountManager()?.getAccounts()?.count() ?: 0
        _hasAccounts.value = (users >= 1)
    }

    fun onManageAccountClicked() {
        view.openAccountsActivity()
    }

    fun grantTrackingPermissions(granted: Boolean) {
        userManager?.d2?.dataStoreModule()?.localDataStore()
            ?.value(DATA_STORE_ANALYTICS_PERMISSION_KEY)
            ?.blockingSet(granted.toString())
        if (granted) {
            trackServerVersion()
            trackUserInfo()
        }
    }

    private fun deletePin() {
        userManager?.d2?.dataStoreModule()
            ?.localDataStore()
            ?.value(PIN)
            ?.blockingDeleteIfExist()
    }

    companion object {
        const val EMPTY_CREDENTIALS = "Empty credentials"
        const val AUTH_ERROR = "AUTH ERROR"
    }

    fun onServerChanged(serverUrl: CharSequence, start: Int, before: Int, count: Int) {
        if (serverUrl.toString() != this.serverUrl.value) {
            this.serverUrl.value = serverUrl.toString()
            checkData()
            if (this.serverUrl.value != null) {
                checkTestingEnvironment(this.serverUrl.value!!)
            }
        }
    }

    fun onUserChanged(userName: CharSequence, start: Int, before: Int, count: Int) {
        if (userName.toString() != this.userName.value) {
            this.userName.value = userName.toString()
            checkData()
        }
    }

    fun onPassChanged(password: CharSequence, start: Int, before: Int, count: Int) {
        if (password.toString() != this.password.value) {
            this.password.value = password.toString()
            checkData()
        }
    }

    private fun checkData() {
        val newValue = !serverUrl.value.isNullOrEmpty() &&
            !userName.value.isNullOrEmpty() &&
            !password.value.isNullOrEmpty()
        if (isDataComplete.value == null || isDataComplete.value != newValue) {
            isDataComplete.value = newValue
        }
        checkBiometricVisibility()
    }

    private fun checkTestingEnvironment(serverUrl: String) {
        testingCredentials?.get(serverUrl)?.let { credentials ->
            isTestingEnvironment.value = Trio.create(
                serverUrl,
                credentials.user_name,
                credentials.user_pass,
            )
        }
    }

    fun setTestingCredentials(testingCredentials: List<TestingCredential>) {
        this.testingCredentials = HashMap()
        for (testingCredential in testingCredentials) {
            this.testingCredentials!![testingCredential.server_url] = testingCredential
        }
    }

    fun setAccountInfo(serverUrl: String?, userName: String?) {
        this.serverUrl.value = serverUrl
        this.userName.value = userName
        view.setUrl(serverUrl)
        view.setUser(userName)
    }

    fun onImportDataBase(file: File) {
        userManager?.let {
            viewModelScope.launch {
                val resultJob = async {
                    try {
                        val importedMetadata =
                            it.d2.maintenanceModule().databaseImportExport().importDatabase(file)
                        Result.success(importedMetadata)
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                }

                val result = resultJob.await()

                result.fold(
                    onSuccess = {
                        setAccountInfo(it.serverUrl, it.username)
                        displayManageAccount()
                        view.displayMessage(resourceManager.getString(R.string.importing_successful))
                    },
                    onFailure = {
                        view.displayMessage(resourceManager.parseD2Error(it))
                    },
                )

                view.onDbImportFinished(result.isSuccess)
            }
        }
    }

    fun displayMoreActions() = displayMoreActions
    fun setDisplayMoreActions(shouldDisplayMoreActions: Boolean) {
        _displayMoreActions.postValue(shouldDisplayMoreActions)
    }

    fun shouldAskForBiometrics(): Boolean =
        biometricController.hasBiometric() &&
            !preferenceProvider.areCredentialsSet() &&
            hasAccounts.value == false
}
