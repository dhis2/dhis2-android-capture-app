package org.dhis2.usecases

import co.infinum.goldfinger.rx.RxGoldfinger
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import junit.framework.Assert.assertTrue
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.server.UserManager
import org.dhis2.usescases.login.LoginContracts
import org.dhis2.usescases.login.LoginPresenter
import org.dhis2.utils.scheduler.TrampolineSchedulerProvider
import org.junit.Before
import org.junit.Test

class LoginPresenterTest {

    private lateinit var loginPresenter: LoginPresenter
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()

    private val preferenceProvider: PreferenceProvider = mock()
    private val goldfinger: RxGoldfinger = mock()
    private val view: LoginContracts.View = mock()
    private val userManager: UserManager = mock()

    @Before
    fun setup() {
        loginPresenter = LoginPresenter(preferenceProvider, schedulers, goldfinger)
    }

    @Test
    fun `Should go to MainActivity if user is already logged in`(){
        whenever(userManager.isUserLoggedIn) doReturn Observable.just(true)

        loginPresenter.init(view, userManager)
    }

    @Test
    fun `Should perform login successfully`(){

    }

    @Test
    fun `Should not perform login successfully`(){

    }

    @Test
    fun `Should show fabric dialog when user click continue and has not been asked`(){
      //  loginPresenter.onButtonClick()

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
    fun `Should clear disposable when activity is destroyed`(){
      //  loginPresenter.onDestroy()
      //  val disposableSize = loginPresenter.disposable.size()
      //  assertTrue(disposableSize == 0)
    }
}