package org.dhis2.usecases

import co.infinum.goldfinger.Goldfinger
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import junit.framework.Assert.assertTrue
import org.dhis2.data.fingerprint.FingerPrintController
import org.dhis2.data.fingerprint.FingerPrintResult
import org.dhis2.data.fingerprint.Type
import org.dhis2.data.prefs.Preference.Companion.PIN
import org.dhis2.data.prefs.Preference.Companion.SESSION_LOCKED
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.server.UserManager
import org.dhis2.usescases.login.LoginContracts
import org.dhis2.usescases.login.LoginPresenter
import org.dhis2.usescases.main.MainActivity
import org.dhis2.utils.Constants
import org.dhis2.utils.TestingCredential
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.LOGIN
import org.dhis2.utils.analytics.SERVER_QR_SCANNER
import org.junit.Before
import org.junit.Test

class LoginPresenterTest {

    private lateinit var loginPresenter: LoginPresenter
    private val schedulers: SchedulerProvider = SchedulerProvider()

    private val preferenceProvider: PreferenceProvider = mock()
    private val goldfinger: FingerPrintController = mock()
    private val view: LoginContracts.View = mock()
    private val userManager: UserManager = mock()
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
    }

    @Test
    fun `Should show unlock button when login is reached`() {
        whenever(userManager.isUserLoggedIn) doReturn Observable.just(true)
        whenever(preferenceProvider.getBoolean("SessionLocked", false)) doReturn true

        loginPresenter.init(userManager)

        verify(view).showUnlockButton()
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
    fun `Should unlock session`() {
        whenever((preferenceProvider.getString(PIN, ""))) doReturn "123"

        loginPresenter.unlockSession("123")

        verify(preferenceProvider).setValue(SESSION_LOCKED, false)
        verify(view).startActivity(MainActivity::class.java, null, true, true, null)
    }

    @Test
    fun `Should not unlock session`() {
        whenever((preferenceProvider.getString(PIN, ""))) doReturn "333"

        loginPresenter.unlockSession("123")

        verify(preferenceProvider, times(0)).setValue(SESSION_LOCKED, false)
        verify(view, times(0)).startActivity(MainActivity::class.java, null, true, true, null)
    }

    @Test
    fun `Should show alert when URL info is clicked`() {
        loginPresenter.onUrlInfoClick()

        verify(view).displayAlertDialog()
    }

    @Test
    fun `Should log in with fingerprint successfully`() {
        whenever(goldfinger.authenticate()) doReturn Observable.just(
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
        whenever(goldfinger.authenticate()) doReturn Observable.just(
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
        whenever(goldfinger.authenticate()) doReturn Observable.just(
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
    fun `Should display message and hide fingerprint dialog when authenticate throws an error`() {
        whenever(
            goldfinger.authenticate()
        ) doReturn Observable.error(Exception(LoginPresenter.AUTH_ERROR))

        loginPresenter.onFingerprintClick()

        verify(view).displayMessage(LoginPresenter.AUTH_ERROR)
        verify(view).hideFingerprintDialog()
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
    fun `Should load testing servers and users`(){
        val urlSet = hashSetOf("url1","url2","url3")
        val userSet = hashSetOf("user1","user2")

        val testingCredentials = listOf(
                TestingCredential("testing_server_1","testing_user1","psw",""),
                TestingCredential("testing_server_2","testing_user2","psw",""),
                TestingCredential("testing_server_3","testing_user3","psw","")
        )

        whenever(preferenceProvider.getSet(Constants.PREFS_URLS, emptySet())) doReturn urlSet
        whenever(preferenceProvider.getSet(Constants.PREFS_USERS, emptySet())) doReturn userSet

        val (urls,users) = loginPresenter.getAutocompleteData(testingCredentials)

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
}
