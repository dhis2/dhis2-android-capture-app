package org.dhis2.usecases

import co.infinum.goldfinger.rx.RxGoldfinger
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import junit.framework.Assert.assertTrue
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.server.UserManager
import org.dhis2.usescases.login.LoginContracts
import org.dhis2.usescases.login.LoginPresenter
import org.dhis2.usescases.main.MainActivity
import org.dhis2.utils.scheduler.TrampolineSchedulerProvider
import org.hisp.dhis.android.core.systeminfo.SystemInfo
import org.junit.Before
import org.junit.Test

class LoginPresenterTest {

    private lateinit var loginPresenter: LoginPresenter
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()

    private val preferenceProvider: PreferenceProvider = mock()
    private val goldfinger: RxGoldfinger = mock()
    private val view: LoginContracts.View = mock()
    private val userManager: UserManager = mock()

    /*  private val userManager: UserManager = mock {
          on { d2 } doReturn mock()
          on { d2.systemInfoModule().systemInfo } doReturn mock()
          on { d2.systemInfoModule().systemInfo.blockingGet() } doReturn SystemInfo.builder().contextPath("any").build()
      } */

    @Before
    fun setup() {
        loginPresenter = LoginPresenter(preferenceProvider, schedulers, goldfinger)
    }

    @Test
    fun `Should go to MainActivity if user is already logged in`(){
        whenever(userManager.isUserLoggedIn) doReturn Observable.just(true)
        whenever(preferenceProvider.getBoolean("SessionLocked", false)) doReturn false

        loginPresenter.init(view, userManager)

        verify(view).startActivity(MainActivity::class.java, null, true, true, null)
    }

    @Test
    fun `Should show unlock button`(){
        whenever(userManager.isUserLoggedIn) doReturn Observable.just(true)
        whenever(preferenceProvider.getBoolean("SessionLocked", false)) doReturn true

        loginPresenter.init(view, userManager)

        verify(view).showUnlockButton()
    }

    @Test
    fun `Should show loading progress when continue is clicked`(){
        loginPresenter.onButtonClick()

        verify(view).hideKeyboard()
    }

    @Test
    fun `Should show fabric dialog when continue is clicked and has not been asked before`(){
        //  loginPresenter.onButtonClick()

    }

    @Test
    fun `Should perform login successfully`(){

    }

    @Test
    fun `Should not perform login successfully`(){

    }

    @Test
    fun `Should open information dialog when URL icon i is clicked`(){
      //  loginPresenter.onUrlInfoClick(View())
    }

    @Test
    fun `Should open account recovery when user does not remember it`(){
    //    loginPresenter.onAccountRecovery()
        //verify(view)
    }

    @Test
    fun `Should stop reading fingerprint`(){
        loginPresenter.stopReadingFingerprint()
        verify(goldfinger).cancel()
    }

    @Test
    fun `Should clear disposable when activity is destroyed`(){
        loginPresenter.onDestroy()

        val disposableSize = loginPresenter.disposable.size()
        assertTrue(disposableSize == 0)
    }
}