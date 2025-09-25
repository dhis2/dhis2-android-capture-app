package org.dhis2.usescases.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.reactivex.Completable
import io.reactivex.Observable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.biometric.BiometricAuthenticator
import org.dhis2.data.biometric.CryptographyManager
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.server.UserManager
import org.dhis2.mobile.commons.biometrics.CiphertextWrapper
import org.dhis2.mobile.commons.reporting.CrashReportController
import org.dhis2.usescases.main.MainActivity
import org.dhis2.utils.MainCoroutineScopeRule
import org.dhis2.utils.TestingCredential
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.configuration.internal.DatabaseAccount
import org.hisp.dhis.android.core.systeminfo.SystemInfo
import org.hisp.dhis.android.core.user.openid.IntentWithRequestCode
import org.hisp.dhis.android.core.user.openid.OpenIDConnectConfig
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
import javax.crypto.Cipher

class LoginViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()

    private val preferenceProvider: PreferenceProvider = mock()
    private val biometricController: BiometricAuthenticator = mock()
    private val cryptographyManager: CryptographyManager = mock()
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
    private val dispatcherProvider =
        object : DispatcherProvider {
            override fun io(): CoroutineDispatcher = testingDispatcher

            override fun computation(): CoroutineDispatcher = testingDispatcher

            override fun ui(): CoroutineDispatcher = testingDispatcher
        }
    private val repository: LoginRepository = mock()

    private fun instantiateLoginViewModel() {
        loginViewModel =
            LoginViewModel(
                view,
                preferenceProvider,
                resourceManager,
                schedulers,
                userManager,
            )
    }

    private fun instantiateLoginViewModelWithNullUserManager() {
        loginViewModel =
            LoginViewModel(
                view,
                preferenceProvider,
                resourceManager,
                schedulers,
                null,
            )
    }

    val testingCredentials =
        listOf(
            TestingCredential("testing_server_1", "testing_user1", "psw", ""),
            TestingCredential("testing_server_2", "testing_user2", "psw", ""),
            TestingCredential("testing_server_3", "testing_user3", "psw", ""),
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
    }

    @Ignore("Implement in login module")
    @Test
    fun `Should go to MainActivity if user is already logged in`() {
    }

    @Ignore("Implement in login module")
    @Test
    fun `Should show unlock button when login is reached`() {
    }

    @Ignore("Implement in login module")
    @Test
    fun `Should set server url and username if they are saved and user is not loggedIn`() {
    }

    @Ignore("Implement in login module")
    @Test
    fun `Should set default protocol if server url and username is empty`() {
    }

    @Ignore("Implement in login module")
    @Test
    fun `Should set Url to default server protocol if userManager is null`() {
    }

    @Ignore("Implement in login module")
    @Test
    fun `Should log in successfully and show fabric dialog when  user has not been asked before`() {
    }

    @Ignore("Implement in login module")
    @Test
    fun `Should show progress dialog when user click on continue`() {
    }

    @Ignore("Implement in login module")
    @Test
    fun `Should navigate to QR Activity`() {
    }

    @Ignore("Implement in login module")
    @Test
    fun `Should log in with biometric successfully`() {
    }

    @Ignore("Implement in login module")
    @Test
    fun `Should display biometric button for logged server`() {
    }

    @Ignore("Implement in login module")
    @Test
    fun `Should not display biometric button for not logged server`() {
    }

    @Ignore("Implement in login module")
    @Test
    fun `Should not display biometric button for more than one account`() {
    }

    @Test
    fun `Should show empty credentials message when trying to log in with fingerprint`() {
        instantiateLoginViewModel()
    }

    @Ignore("Implement in login module")
    @Test
    fun `Should open account recovery when user does not remember it`() {
    }

    @Ignore("Implement in login module")
    @Test
    fun `Should show message when no connection and user tries to recover account`() {
    }

    @Ignore("Implement in login module")
    @Test
    fun `Should load testing servers and users`() =
        runTest {
        }

    @Test
    fun `Should handle log out when button is clicked`() {
        instantiateLoginViewModel()
        whenever(userManager.d2.userModule().logOut()) doReturn Completable.complete()
        loginViewModel.logOut()
        verify(view).handleLogout()
    }

    @Ignore("Implement in login module")
    @Test
    fun `Should handle successfull response`() {
    }

    @Ignore("Implement in login module")
    @Test
    fun `Should set server and username if user is logged and display biometric prompt`() {
    }

    @Ignore("Implement in login module")
    @Test(expected = Throwable::class)
    fun `Should show error dialog when login process goes wrong`() {
    }

    @Ignore("Implement in login module")
    @Test(expected = Throwable::class)
    fun `Should show error dialog if openIDLogin does not work`() {
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

    @Ignore("Implement in login module")
    @Test
    fun `Should import database`() =
        runTest {
        }

    private fun mockSystemInfo(isUserLoggedIn: Boolean = true) {
        whenever(userManager.isUserLoggedIn) doReturn Observable.just(isUserLoggedIn)
        if (isUserLoggedIn) {
            whenever(
                userManager.d2
                    .systemInfoModule()
                    .systemInfo()
                    .blockingGet(),
            ) doReturn
                SystemInfo
                    .builder()
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
            userManager.d2
                .userModule()
                .user()
                .blockingGet(),
        ) doReturn null
        whenever(
            userManager.d2.userModule().accountManager(),
        ) doReturn mock()

        whenever(
            userManager.d2
                .userModule()
                .accountManager()
                .getAccounts(),
        ) doReturn
            mutableListOf<DatabaseAccount>().apply {
                repeat(accounts) { this.add(dummyDatabaseAccount) }
            }
    }

    private val dummyDatabaseAccount =
        DatabaseAccount
            .builder()
            .username("userName")
            .serverUrl("serverUrl")
            .databaseName("database")
            .databaseCreationDate("")
            .encrypted(false)
            .build()

    private fun mockBiometrics() {
        val cipherTextWrapperMock: CiphertextWrapper =
            mock {
                on { initializationVector } doReturn byteArrayOf()
            }
        val cipherMock: Cipher = mock()
        whenever(preferenceProvider.getBiometricCredentials()) doReturn cipherTextWrapperMock
        whenever(cryptographyManager.getInitializedCipherForDecryption(any())) doReturn cipherMock
    }
}
