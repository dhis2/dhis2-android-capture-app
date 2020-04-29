package org.dhis2.usescases.login

import co.infinum.goldfinger.Goldfinger
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import junit.framework.Assert.assertTrue
import org.dhis2.data.fingerprint.FingerPrintController
import org.dhis2.data.fingerprint.FingerPrintResult
import org.dhis2.data.fingerprint.Type
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.server.UserManager
import org.dhis2.usescases.main.MainActivity
import org.dhis2.utils.Constants
import org.dhis2.utils.Constants.SECURE_SERVER_URL
import org.dhis2.utils.Constants.SECURE_USER_NAME
import org.dhis2.utils.TestingCredential
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.LOGIN
import org.dhis2.utils.analytics.SERVER_QR_SCANNER
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class LoginPresenterTest {

    private lateinit var loginPresenter: LoginPresenter
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()

    private val preferenceProvider: PreferenceProvider = mock()
    private val goldfinger: FingerPrintController = mock()
    private val view: LoginContracts.View = mock()
    private val userManager: UserManager = Mockito.mock(UserManager::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val analyticsHelper: AnalyticsHelper = mock()

    @Before
    fun setup() {
        loginPresenter =
            LoginPresenter(view, preferenceProvider, schedulers, goldfinger, analyticsHelper)
    }

    @Test
    fun `Should go to MainActivity if user is already logged in`() {
        whenever(userManager.isUserLoggedIn) doReturn Observable.just(true)
        whenever(preferenceProvider.getBoolean("SessionLocked", false)) doReturn false

        loginPresenter.init(userManager)

        verify(view).startActivity(MainActivity::class.java, null, true, true, null)
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should show unlock button when login is reached`() {
        whenever(userManager.isUserLoggedIn) doReturn Observable.just(true)
        whenever(preferenceProvider.getBoolean("SessionLocked", false)) doReturn true

        loginPresenter.init(userManager)

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
            preferenceProvider.getString(SECURE_SERVER_URL, protocol)
        ) doReturn serverUrl
        whenever(preferenceProvider.getString(SECURE_USER_NAME, "")) doReturn userName

        loginPresenter.init(userManager)

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
            preferenceProvider.getString(SECURE_SERVER_URL, protocol)
        ) doReturn null
        whenever(preferenceProvider.getString(SECURE_USER_NAME, "")) doReturn null

        loginPresenter.init(userManager)

        verify(view).setUrl(protocol)
        verify(view, times(2)).getDefaultServerProtocol()
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should set Url to default server protocol if userManager is null`() {
        val defaultProtocol = "https://"
        whenever(view.getDefaultServerProtocol()) doReturn defaultProtocol

        loginPresenter.init(null)

        verify(view).getDefaultServerProtocol()
        verify(view).setUrl(any())
        verifyNoMoreInteractions(view)
    }

    @Test
    fun `Should show fabric dialog when continue is clicked and user has not been asked before`() {
        whenever(
            preferenceProvider.getBoolean(
                Constants.USER_ASKED_CRASHLYTICS,
                false
            )
        ) doReturn false
        loginPresenter.onButtonClick()

        verify(view).hideKeyboard()
        verify(analyticsHelper).setEvent(LOGIN, CLICK, LOGIN)
        verify(view).showCrashlyticsDialog()
    }

    @Test
    fun `Should show progress dialog when user click on continue`() {
        whenever(
            preferenceProvider.getBoolean(
                Constants.USER_ASKED_CRASHLYTICS,
                false
            )
        ) doReturn true
        loginPresenter.onButtonClick()

        verify(view).hideKeyboard()
        verify(analyticsHelper).setEvent(LOGIN, CLICK, LOGIN)
        verify(view).showLoginProgress(true)
    }

    @Test
    fun `Should navigate to QR Activity`() {
        loginPresenter.onQRClick()

        verify(analyticsHelper).setEvent(SERVER_QR_SCANNER, CLICK, SERVER_QR_SCANNER)
        verify(view).navigateToQRActivity()
    }

    @Test
    fun `Should show alert when URL info is clicked`() {
        loginPresenter.onUrlInfoClick()

        verify(view).displayAlertDialog()
    }

    @Test
    fun `Should log in with fingerprint successfully`() {
        whenever(goldfinger.authenticate(view.getPromptParams())) doReturn Observable.just(
            FingerPrintResult(
                Type.SUCCESS,
                "none"
            )
        )
        whenever(
            preferenceProvider.contains(
                Constants.SECURE_SERVER_URL,
                Constants.SECURE_USER_NAME, Constants.SECURE_PASS
            )
        ) doReturn true
        whenever(
            preferenceProvider.getString(Constants.SECURE_SERVER_URL)
        ) doReturn "http://dhis2.org"
        whenever(preferenceProvider.getString(Constants.SECURE_USER_NAME)) doReturn "James"
        whenever(preferenceProvider.getString(Constants.SECURE_PASS)) doReturn "1234"

        loginPresenter.onFingerprintClick()

        verify(view).showCredentialsData(
            Goldfinger.Type.SUCCESS,
            preferenceProvider.getString(Constants.SECURE_SERVER_URL)!!,
            preferenceProvider.getString(Constants.SECURE_USER_NAME)!!,
            preferenceProvider.getString(Constants.SECURE_PASS)!!
        )
    }

    @Test
    fun `Should show credentials data when logging in with fingerprint`() {
        whenever(goldfinger.authenticate(view.getPromptParams())) doReturn Observable.just(
            FingerPrintResult(
                Type.ERROR,
                "none"
            )
        )
        whenever(
            preferenceProvider.contains(
                Constants.SECURE_SERVER_URL,
                Constants.SECURE_USER_NAME, Constants.SECURE_PASS
            )
        ) doReturn true

        loginPresenter.onFingerprintClick()

        view.showCredentialsData(Goldfinger.Type.ERROR, "none")
    }

    @Test
    fun `Should show empty credentials message when trying to log in with fingerprint`() {
        whenever(goldfinger.authenticate(view.getPromptParams())) doReturn Observable.just(
            FingerPrintResult(
                Type.ERROR,
                "none"
            )
        )
        whenever(
            preferenceProvider.contains(
                Constants.SECURE_SERVER_URL,
                Constants.SECURE_USER_NAME, Constants.SECURE_PASS
            )
        ) doReturn false

        loginPresenter.onFingerprintClick()

        verify(view).showEmptyCredentialsMessage()
    }

    @Test
    fun `Should display message when authenticate throws an error`() {
        whenever(
            goldfinger.authenticate(view.getPromptParams())
        ) doReturn Observable.error(Exception(LoginPresenter.AUTH_ERROR))

        loginPresenter.onFingerprintClick()

        verify(view).displayMessage(LoginPresenter.AUTH_ERROR)
    }

    @Test
    fun `Should open account recovery when user does not remember it`() {
        loginPresenter.onAccountRecovery()

        verify(view).openAccountRecovery()
    }

    @Test
    fun `Should stop reading fingerprint`() {
        loginPresenter.stopReadingFingerprint()

        verify(goldfinger).cancel()
    }

    @Test
    fun `Should clear disposable when activity is destroyed`() {
        loginPresenter.onDestroy()

        val disposableSize = loginPresenter.disposable.size()
        assertTrue(disposableSize == 0)
    }

    @Test
    fun `Should load testing servers and users`() {
        val urlSet = hashSetOf("url1", "url2", "url3")
        val userSet = hashSetOf("user1", "user2")

        val testingCredentials = listOf(
            TestingCredential("testing_server_1", "testing_user1", "psw", ""),
            TestingCredential("testing_server_2", "testing_user2", "psw", ""),
            TestingCredential("testing_server_3", "testing_user3", "psw", "")
        )

        whenever(preferenceProvider.getSet(Constants.PREFS_URLS, emptySet())) doReturn urlSet
        whenever(preferenceProvider.getSet(Constants.PREFS_USERS, emptySet())) doReturn userSet

        val (urls, users) = loginPresenter.getAutocompleteData(testingCredentials)

        urlSet.forEach {
            assertTrue(urls.contains(it))
        }

        userSet.forEach {
            assertTrue(users.contains(it))
        }

        assertTrue(users.contains(Constants.USER_TEST_ANDROID))

        testingCredentials.forEach {
            assertTrue(urls.contains(it.server_url))
        }
    }

    @Test
    fun `Should handle log out when button is clicked`() {
        whenever(userManager.d2.userModule().logOut()) doReturn Completable.complete()
        loginPresenter.setUserManager(userManager)
        loginPresenter.logOut()

        verify(view).handleLogout()
    }
}
