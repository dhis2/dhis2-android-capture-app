package org.dhis2.usecases.main

import androidx.work.WorkManager
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.TestScheduler
import org.dhis2.data.prefs.Preference.Companion.PIN
import org.dhis2.data.prefs.Preference.Companion.SESSION_LOCKED
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TestSchedulerProvider
import org.dhis2.usescases.login.LoginActivity
import org.dhis2.usescases.main.MainPresenter
import org.dhis2.usescases.main.MainView
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.user.User
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Created by frodriguez on 10/22/2019.
 *
 */
class MainPresenterTest {

    private lateinit var presenter: MainPresenter
    private val schedulers: TestSchedulerProvider = TestSchedulerProvider(TestScheduler())
    private val view: MainView = mock()
    private val d2: D2 = mock()
    private val preferences: PreferenceProvider = mock()
    private val workManger: WorkManager = mock()


    @Before
    fun setUp() {
        presenter = MainPresenter(view, d2, schedulers, preferences, workManger)
    }

    @Test
    fun `Should show username on activity resumed`(){
        val user = Single.just(createUserMock())
        whenever(d2.userModule()) doReturn mock()
        whenever(d2.userModule().user()) doReturn mock()
        whenever(d2.userModule().user().get()) doReturn user
        schedulers.ui().triggerActions()

        presenter.init()


        verify(view).showHideFilter()
    }

    @Test
    fun `Should log out`() {
        whenever(d2.userModule()) doReturn mock()
        whenever(d2.userModule().logOut()) doReturn Completable.complete()

        presenter.logOut()

        verify(workManger).cancelAllWork()
        verify(view).startActivity(LoginActivity::class.java, null, true, true, null)
    }

    @Test
    fun `Should block session`() {
        val pin = "1234"

        presenter.blockSession(pin)

        verify(preferences).setValue(SESSION_LOCKED, true)
        verify(preferences).setValue(PIN, pin)
        verify(workManger).cancelAllWork()
        verify(view).back()
    }

    @Test
    fun `Should show filter screen when filter icon is clicked`() {
        presenter.showFilter()

        verify(view).showHideFilter()
    }

    @Test
    fun `Should clear disposable when activity is paused`() {
        presenter.onDetach()

        val disposableSize = presenter.disposable.size()

        assertTrue(disposableSize == 0)
    }

    @Test
    fun `Should open drawer when menu is clicked`() {
        presenter.onMenuClick()

        verify(view).openDrawer(any())
    }

    private fun createUserMock(): User {
        return User.builder()
            .uid("userUid")
            .firstName("test_name")
            .surname("test_surName")
            .build()
    }

}
