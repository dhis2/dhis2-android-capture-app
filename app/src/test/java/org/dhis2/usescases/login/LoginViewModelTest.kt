package org.dhis2.usescases.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.Constants.PREFS_URLS
import org.dhis2.commons.Constants.PREFS_USERS
import org.dhis2.commons.Constants.USER_ASKED_CRASHLYTICS
import org.dhis2.commons.Constants.USER_TEST_ANDROID
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.prefs.SECURE_PASS
import org.dhis2.commons.prefs.SECURE_SERVER_URL
import org.dhis2.commons.prefs.SECURE_USER_NAME
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.biometric.BiometricController
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.server.UserManager
import org.dhis2.mobile.commons.reporting.CrashReportController
import org.dhis2.usescases.main.MainActivity
import org.dhis2.utils.MainCoroutineScopeRule
import org.dhis2.utils.TestingCredential
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.LOGIN
import org.dhis2.utils.analytics.SERVER_QR_SCANNER
import org.hisp.dhis.android.core.arch.db.access.DatabaseExportMetadata
import org.hisp.dhis.android.core.configuration.internal.DatabaseAccount
import org.hisp.dhis.android.core.systeminfo.SystemInfo
import org.hisp.dhis.android.core.user.User
import org.hisp.dhis.android.core.user.openid.IntentWithRequestCode
import org.hisp.dhis.android.core.user.openid.OpenIDConnectConfig
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import java.io.File

class LoginViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()

    private val preferenceProvider: PreferenceProvider = mock()
    private val biometricController: BiometricController = mock()
    private val view: LoginContracts.View = mock()
    private val userManager: UserManager =
        Mockito.mock(UserManager::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val analyticsHelper: AnalyticsHelper = mock()
    private val crashReportController: CrashReportController = mock()
    private val network: NetworkUtils = mock()
    private lateinit var loginViewModel: LoginViewModel
    private val openidconfig: OpenIDConnectConfig = mock()
    private val resourceManager: ResourceManager = mock()
    private val testingDispatcher = StandardTestDispatcher()
    private val dispatcherProvider = object : DispatcherProvider {
        override fun io(): CoroutineDispatcher {
            return testingDispatcher
        }

        override fun computation(): CoroutineDispatcher {
            return testingDispatcher
        }

        override fun ui(): CoroutineDispatcher {
            return testingDispatcher
        }
    }
    private val repository: LoginRepository = mock()

    private fun instantiateLoginViewModel() {
        loginViewModel = LoginViewModel(
            view,
            preferenceProvider,
            resourceManager,
            schedulers,
            dispatcherProvider,
            biometricController,
            analyticsHelper,
            crashReportController,
            network,
            userManager,
            repository,
        )
    }

    private fun instantiateLoginViewModelWithNullUserManager() {
        loginViewModel = LoginViewModel(
            view,
            preferenceProvider,
            resourceManager,
            schedulers,
            dispatcherProvider,
            biometricController,
            analyticsHelper,
            crashReportController,
            network,
            null,
            repository,
        )
    }

    val testingCredentials = listOf(
        TestingCredential("testing_server_1", "testing_user1", "psw", ""),
        TestingCredential("testing_server_2", "testing_user2", "psw", ""),
        TestingCredential("testing_server_3", "testing_user3", "psw", ""),
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
    }

    @Test
    fun `Should go to MainActivity if user is already logged in`() {
        whenever(userManager.isUserLoggedIn) doReturn Observable.just(true)
        whenever(preferenceProvider.getBoolean("SessionLocked", false)) doReturn false
        instantiateLoginViewModel()
        verify(view).startActivity(MainActivity::class.java, null, true, true, null)
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should show unlock button when login is reached`() {
        whenever(userManager.isUserLoggedIn) doReturn Observable.just(true)
        whenever(preferenceProvider.getBoolean("SessionLocked", false)) doReturn true
        instantiateLoginViewModel()
        verify(view).showUnlockButton()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should set server url and username if they are saved and user is not loggedIn`() {
        val serverUrl = "https://test.com/"
        val userName = "user"
        val protocol = "https://"
        whenever(userManager.isUserLoggedIn) doReturn Observable.just(false)
        whenever(preferenceProvider.getBoolean("SessionLocked", false)) doReturn false
        whenever(view.getDefaultServerProtocol()) doReturn protocol
        whenever(
            preferenceProvider.getString(SECURE_SERVER_URL, protocol),
        ) doReturn serverUrl
        whenever(preferenceProvider.getString(SECURE_USER_NAME, "")) doReturn userName
        instantiateLoginViewModel()
        verify(view).setUrl(serverUrl)
        verify(view).setUser(userName)
        verify(view).getDefaultServerProtocol()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should set default protocol if server url and username is empty`() {
        val protocol = "https://"
        whenever(userManager.isUserLoggedIn) doReturn Observable.just(false)
        whenever(preferenceProvider.getBoolean("SessionLocked", false)) doReturn false
        whenever(view.getDefaultServerProtocol()) doReturn protocol
        whenever(
            preferenceProvider.getString(SECURE_SERVER_URL, protocol),
        ) doReturn null
        whenever(preferenceProvider.getString(SECURE_USER_NAME, "")) doReturn null
        instantiateLoginViewModel()
        verify(view).setUrl(protocol)
        verify(view, times(2)).getDefaultServerProtocol()
    }

    @Test
    fun `Should set Url to default server protocol if userManager is null`() {
        val defaultProtocol = "https://"
        whenever(view.getDefaultServerProtocol()) doReturn defaultProtocol
        instantiateLoginViewModelWithNullUserManager()
        verify(view).getDefaultServerProtocol()
        verify(view).setUrl(any())
    }

    @Test
    fun `Should log in successfully and show fabric dialog when  user has not been asked before`() {
        val mockedUser: User = mock()
        whenever(view.initLogin()) doReturn userManager
        whenever(userManager.logIn(any(), any(), any())) doReturn Observable.just(mockedUser)
        instantiateLoginViewModelWithNullUserManager()
        loginViewModel.onServerChanged(serverUrl = "serverUrl", 0, 0, 0)
        loginViewModel.onUserChanged(userName = "username", 0, 0, 0)
        loginViewModel.onPassChanged(password = "pass", 0, 0, 0)
        loginViewModel.onLoginButtonClick()
        verify(view).hideKeyboard()
        verify(analyticsHelper).setEvent(LOGIN, CLICK, LOGIN)
        verify(view).saveUsersData(true, false)
    }

    @Test
    fun `Should show progress dialog when user click on continue`() {
        whenever(
            preferenceProvider.getBoolean(
                USER_ASKED_CRASHLYTICS,
                false,
            ),
        ) doReturn true
        whenever(view.initLogin()) doReturn userManager
        instantiateLoginViewModelWithNullUserManager()
        loginViewModel.onLoginButtonClick()
        verify(view).hideKeyboard()
        verify(analyticsHelper).setEvent(LOGIN, CLICK, LOGIN)
        assertTrue(loginViewModel.loginProgressVisible.value == false)
    }

    @Test
    fun `Should navigate to QR Activity`() {
        instantiateLoginViewModel()
        loginViewModel.onQRClick()
        verify(analyticsHelper).setEvent(SERVER_QR_SCANNER, CLICK, SERVER_QR_SCANNER)
        verify(view).navigateToQRActivity()
    }

    @Test
    fun `Should log in with biometric successfully`() {
        instantiateLoginViewModel()
        loginViewModel.authenticateWithBiometric()
        verify(biometricController).authenticate(any())
    }

    @Test
    fun `Should display biometric button for logged server`() {
        instantiateLoginViewModel()
        mockAccounts()
        whenever(preferenceProvider.getString(SECURE_SERVER_URL)) doReturn "loggedServer"
        whenever(preferenceProvider.contains(SECURE_PASS)) doReturn true
        whenever(biometricController.hasBiometric()) doReturn true
        loginViewModel.onServerChanged("loggedServer", 0, 0, 0)
        assert(loginViewModel.canLoginWithBiometrics.value == true)
    }

    @Test
    fun `Should not display biometric button for not logged server`() {
        instantiateLoginViewModel()
        mockAccounts()
        whenever(preferenceProvider.getString(SECURE_SERVER_URL)) doReturn "loggedServer"
        whenever(biometricController.hasBiometric()) doReturn true
        loginViewModel.onServerChanged("notLoggedServer", 0, 0, 0)
        assert(loginViewModel.canLoginWithBiometrics.value == false)
    }

    @Test
    fun `Should not display biometric button for more than one account`() {
        instantiateLoginViewModel()
        mockAccounts(2)
        whenever(preferenceProvider.getString(SECURE_SERVER_URL)) doReturn "loggedServer"
        whenever(biometricController.hasBiometric()) doReturn true
        loginViewModel.onServerChanged("loggedServer", 0, 0, 0)
        assert(loginViewModel.canLoginWithBiometrics.value == false)
    }

    @Test
    fun `Should show empty credentials message when trying to log in with fingerprint`() {
        instantiateLoginViewModel()
    }

    @Test
    fun `Should open account recovery when user does not remember it`() {
        instantiateLoginViewModel()
        whenever(network.isOnline()) doReturn true
        loginViewModel.onAccountRecovery()
        verify(view).openAccountRecovery()
    }

    @Test
    fun `Should show message when no connection and user tries to recover account`() {
        instantiateLoginViewModel()
        whenever(network.isOnline()) doReturn false
        loginViewModel.onAccountRecovery()
        verify(view).showNoConnectionDialog()
    }

    @Test
    fun `Should load testing servers and users`() = runTest {
        whenever(repository.getTestingCredentials()) doReturn testingCredentials

        instantiateLoginViewModel()
        val urlSet = hashSetOf("url1", "url2", "url3")
        val userSet = hashSetOf("user1", "user2")

        whenever(preferenceProvider.getSet(PREFS_URLS, emptySet())) doReturn urlSet
        whenever(preferenceProvider.getSet(PREFS_USERS, emptySet())) doReturn userSet
        loginViewModel.autoCompleteData.observeForever { autocompleteData ->
            val (urls, users) = autocompleteData
            urlSet.forEach {
                assertTrue(urls.contains(it))
            }
            userSet.forEach {
                assertTrue(users.contains(it))
            }
            assertTrue(users.contains(USER_TEST_ANDROID))
            testingCredentials.forEach {
                assertTrue(urls.contains(it.server_url))
            }
        }
    }

    @Test
    fun `Should handle log out when button is clicked`() {
        instantiateLoginViewModel()
        whenever(userManager.d2.userModule().logOut()) doReturn Completable.complete()
        loginViewModel.logOut()
        verify(view).handleLogout()
    }

    @Test
    fun `Should handle successfull response`() {
        instantiateLoginViewModel()
        val response = Result.success(
            User.builder()
                .uid("userUid")
                .build(),
        )
        whenever(userManager.d2) doReturn mock()
        whenever(userManager.d2.systemInfoModule()) doReturn mock()
        whenever(userManager.d2.systemInfoModule().systemInfo()) doReturn mock()
        whenever(userManager.d2.systemInfoModule().systemInfo().blockingGet()) doReturn mock()
        whenever(
            userManager.d2.systemInfoModule().systemInfo().blockingGet()?.version(),
        ) doReturn "1234"
        whenever(userManager.d2.dataStoreModule()) doReturn mock()
        whenever(userManager.d2.dataStoreModule().localDataStore()) doReturn mock()
        whenever(
            userManager.d2.dataStoreModule().localDataStore().value("WasInitialSyncDone"),
        ) doReturn mock()
        whenever(
            userManager.d2.dataStoreModule().localDataStore().value("WasInitialSyncDone")
                .blockingExists(),
        ) doReturn false

        whenever(
            userManager.d2.userModule(),
        ) doReturn mock()
        whenever(
            userManager.d2.userModule().user(),
        ) doReturn mock()
        whenever(
            userManager.d2.userModule().user().blockingGet(),
        ) doReturn null
        whenever(
            userManager.d2.userModule().accountManager(),
        ) doReturn mock()

        whenever(
            userManager.d2.userModule().accountManager().getAccounts(),
        ) doReturn listOf()

        loginViewModel.handleResponse(response)
        verify(view).saveUsersData(true, false)
    }

    @Test
    fun `Should set server and username if user is logged and display biometric prompt`() {
        instantiateLoginViewModel()
        mockSystemInfo()
        mockAccounts()
        mockAccounts()
        whenever(userManager.userName()) doReturn Single.just("Username")
        whenever(biometricController.hasBiometric()) doReturn true
        whenever(preferenceProvider.getString(SECURE_SERVER_URL)) doReturn "contextPath"
        whenever(preferenceProvider.contains(SECURE_PASS)) doReturn true
        loginViewModel.serverUrl.value = "contextPath"
        loginViewModel.checkServerInfoAndShowBiometricButton()
        verify(view).setUrl("contextPath")
        verify(view).setUser("Username")
        assert(loginViewModel.canLoginWithBiometrics.value == true)
        loginViewModel.authenticateWithBiometric()
        verify(biometricController).authenticate(any())
    }

    @Test(expected = Throwable::class)
    fun `Should show error dialog when login process goes wrong`() {
        instantiateLoginViewModelWithNullUserManager()
        val throwable = Throwable()
        whenever(
            preferenceProvider.getBoolean(
                USER_ASKED_CRASHLYTICS,
                false,
            ),
        ) doReturn true
        given(loginViewModel.onLoginButtonClick()).willThrow(throwable)
        verify(view).renderError(throwable)
    }

    @Test(expected = Throwable::class)
    fun `Should show error dialog if openIDLogin does not work`() {
        instantiateLoginViewModelWithNullUserManager()
        val throwable = Throwable()
        userManager.logIn(openidconfig)
        loginViewModel.openIdLogin(openidconfig)
        verify(view).renderError(throwable)
    }

    @Test
    fun `Should invoke openIdLogin method successfully`() {
        instantiateLoginViewModelWithNullUserManager()
        val openidconfig: OpenIDConnectConfig = mock()
        val it: IntentWithRequestCode = mock()
        whenever(view.initLogin()) doReturn userManager
        whenever(userManager.logIn(openidconfig)) doReturn Observable.just(it)
        instantiateLoginViewModelWithNullUserManager()
        loginViewModel.openIdLogin(openidconfig)
        verify(view).openOpenIDActivity(it)
    }

    @Test
    fun `Should import database`() = runTest {
        val mockedDatabase: File = mock()
        whenever(repository.getTestingCredentials()) doReturn testingCredentials

        instantiateLoginViewModel()
        whenever(resourceManager.getString(any())) doReturn "Import successful"
        whenever(
            userManager.d2.maintenanceModule().databaseImportExport()
                .importDatabase(mockedDatabase),
        ) doReturn DatabaseExportMetadata(
            0,
            "2024-01-01",
            "serverUrl",
            "userName",
            false,
        )

        loginViewModel.onImportDataBase(mockedDatabase)
        testingDispatcher.scheduler.advanceUntilIdle()
        verify(view).setUrl("serverUrl")
        verify(view).setUser("userName")
        verify(view).displayMessage("Import successful")
        verify(view).onDbImportFinished(true)
    }

    private fun mockSystemInfo(isUserLoggedIn: Boolean = true) {
        whenever(userManager.isUserLoggedIn) doReturn Observable.just(isUserLoggedIn)
        if (isUserLoggedIn) {
            whenever(
                userManager.d2.systemInfoModule().systemInfo().blockingGet(),
            ) doReturn SystemInfo.builder()
                .contextPath("contextPath")
                .build()
        }
    }

    private fun mockAccounts(accounts: Int = 1) {
        whenever(
            userManager.d2.userModule(),
        ) doReturn mock()
        whenever(
            userManager.d2.userModule().user(),
        ) doReturn mock()
        whenever(
            userManager.d2.userModule().user().blockingGet(),
        ) doReturn null
        whenever(
            userManager.d2.userModule().accountManager(),
        ) doReturn mock()

        whenever(
            userManager.d2.userModule().accountManager().getAccounts(),
        ) doReturn mutableListOf<DatabaseAccount>().apply {
            repeat(accounts) { this.add(dummyDatabaseAccount) }
        }
    }

    private val dummyDatabaseAccount = DatabaseAccount.builder()
        .username("userName")
        .serverUrl("serverUrl")
        .databaseName("database")
        .databaseCreationDate("")
        .encrypted(false)
        .build()
}
