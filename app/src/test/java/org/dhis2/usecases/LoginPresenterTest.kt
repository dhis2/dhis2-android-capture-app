package org.dhis2.usecases

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.usescases.login.LoginPresenter
import org.dhis2.utils.scheduler.TrampolineSchedulerProvider
import org.junit.Before
import org.junit.Test

class LoginPresenterTest {

    private lateinit var loginPresenter: LoginPresenter
    private val preferenceProvider: PreferenceProvider = mock()
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()

    @Before
    fun setup() {
        loginPresenter = LoginPresenter(preferenceProvider, schedulers)
    }

    @Test
    fun `Should open information dialog when URL icon i is clicked`(){
      //  loginPresenter.onUrlInfoClick(View())
    }

    @Test
    fun `Should open account recovery when user does not remember it`(){
        loginPresenter.onAccountRecovery()
        //verify(view)
    }

}