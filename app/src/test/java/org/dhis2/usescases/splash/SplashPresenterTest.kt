package org.dhis2.usescases.splash

import io.reactivex.Observable
import io.reactivex.schedulers.TestScheduler
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.schedulers.TestSchedulerProvider
import org.dhis2.data.server.UserManager
import org.dhis2.mobile.commons.reporting.CrashReportController
import org.dhis2.utils.analytics.DATA_STORE_ANALYTICS_PERMISSION_KEY
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.datastore.KeyValuePair
import org.hisp.dhis.android.core.systeminfo.SystemInfo
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit

class SplashPresenterTest {
    private val view: SplashView = mock()
    private val userManager: UserManager = mock()
    private val d2: D2 = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)
    private val scheduler = TestScheduler()
    private val schedulerProvider = TestSchedulerProvider(scheduler)
    private val preferenceProvider: PreferenceProvider = mock()
    private val crashReportController: CrashReportController = mock()

    private lateinit var presenter: SplashPresenter

    @Before
    fun setUp() {
        whenever(userManager.d2) doReturn d2
        whenever(preferenceProvider.getBoolean(Preference.SESSION_LOCKED, false)) doReturn true
        whenever(
            preferenceProvider.getBoolean(Preference.INITIAL_METADATA_SYNC_DONE, false),
        ) doReturn true
        whenever(
            preferenceProvider.getBoolean(Preference.INITIAL_DATA_SYNC_DONE, false),
        ) doReturn false
        presenter =
            SplashPresenter(
                view,
                userManager,
                schedulerProvider,
                preferenceProvider,
                crashReportController,
            )
    }

    @Test
    fun `Should go to next screen without reading the SDK when user is not logged in`() {
        whenever(userManager.isUserLoggedIn) doReturn Observable.just(false)

        presenter.init()
        scheduler.advanceTimeBy(SPLASH_DELAY_SECONDS, TimeUnit.SECONDS)

        verify(view).goToNextScreen(
            isUserLogged = false,
            sessionLocked = true,
            initialSyncDone = true,
            initialDataSyncDone = false,
        )
        verify(d2, never()).systemInfoModule()
        verify(crashReportController, never()).trackServer(anyOrNull(), anyOrNull())
    }

    @Test
    fun `Should track server and go to next screen when user is logged in and tracking is granted`() {
        whenever(userManager.isUserLoggedIn) doReturn Observable.just(true)
        mockTrackingPermission(granted = true)
        whenever(d2.systemInfoModule().systemInfo().blockingGet()) doReturn
            SystemInfo
                .builder()
                .contextPath("https://play.dhis2.org")
                .version("2.40")
                .build()

        presenter.init()
        scheduler.advanceTimeBy(SPLASH_DELAY_SECONDS, TimeUnit.SECONDS)

        verify(crashReportController).trackServer("https://play.dhis2.org", "2.40")
        verify(view).goToNextScreen(
            isUserLogged = true,
            sessionLocked = true,
            initialSyncDone = true,
            initialDataSyncDone = false,
        )
    }

    @Test
    fun `Should not track server when user is logged in but tracking is not granted`() {
        whenever(userManager.isUserLoggedIn) doReturn Observable.just(true)
        mockTrackingPermission(granted = false)

        presenter.init()
        scheduler.advanceTimeBy(SPLASH_DELAY_SECONDS, TimeUnit.SECONDS)

        verify(d2, never()).systemInfoModule()
        verify(crashReportController, never()).trackServer(anyOrNull(), anyOrNull())
        verify(view).goToNextScreen(
            isUserLogged = true,
            sessionLocked = true,
            initialSyncDone = true,
            initialDataSyncDone = false,
        )
    }

    @Test
    fun `Should go to next screen when there is no user manager`() {
        presenter =
            SplashPresenter(
                view,
                null,
                schedulerProvider,
                preferenceProvider,
                crashReportController,
            )

        presenter.init()

        verify(view).goToNextScreen(
            isUserLogged = false,
            sessionLocked = false,
            initialSyncDone = false,
            initialDataSyncDone = false,
        )
    }

    @Test
    fun `Should not go to next screen when destroyed before the splash delay ends`() {
        whenever(userManager.isUserLoggedIn) doReturn Observable.just(true)

        presenter.init()
        presenter.destroy()
        scheduler.advanceTimeBy(SPLASH_DELAY_SECONDS, TimeUnit.SECONDS)

        assertEquals(0, presenter.compositeDisposable.size())
        verify(view, never()).goToNextScreen(any(), any(), any(), any())
        verify(crashReportController, never()).trackServer(anyOrNull(), anyOrNull())
    }

    private fun mockTrackingPermission(granted: Boolean) {
        whenever(
            d2
                .dataStoreModule()
                .localDataStore()
                .value(DATA_STORE_ANALYTICS_PERMISSION_KEY)
                .blockingGet(),
        ) doReturn
            KeyValuePair
                .builder()
                .key(DATA_STORE_ANALYTICS_PERMISSION_KEY)
                .value(granted.toString())
                .build()
    }

    companion object {
        private const val SPLASH_DELAY_SECONDS = 2L
    }
}
