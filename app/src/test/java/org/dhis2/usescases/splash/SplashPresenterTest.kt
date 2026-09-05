package org.dhis2.usescases.splash

import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.server.UserManager
import org.dhis2.mobile.commons.reporting.CrashReportController
import org.dhis2.utils.analytics.DATA_STORE_ANALYTICS_PERMISSION_KEY
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.datastore.KeyValuePair
import org.hisp.dhis.android.core.systeminfo.SystemInfo
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SplashPresenterTest {
    private val view: SplashView = mock()
    private val userManager: UserManager = mock(defaultAnswer = RETURNS_DEEP_STUBS)
    private val d2: D2 = Mockito.mock(D2::class.java, RETURNS_DEEP_STUBS)
    private val schedulerProvider = TrampolineSchedulerProvider()
    private val preferenceProvider: PreferenceProvider = mock()
    private val crashReportController: CrashReportController = mock()

    private lateinit var presenter: SplashPresenter

    @Before
    fun setup() {
        whenever(userManager.d2) doReturn d2
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
    fun `Should go to next screen when user is not logged in without touching the SDK`() {
        whenever(userManager.isUserLoggedIn) doReturn Observable.just(false)

        presenter.init()

        verify(view).goToNextScreen(
            false,
            preferenceProvider.getBoolean(Preference.SESSION_LOCKED, false),
            preferenceProvider.getBoolean(Preference.INITIAL_METADATA_SYNC_DONE, false),
            preferenceProvider.getBoolean(Preference.INITIAL_DATA_SYNC_DONE, false),
        )
        verify(d2, never()).systemInfoModule()
        verify(crashReportController, never()).trackServer(any(), any())
    }

    @Test
    fun `Should track user info and go to next screen when user is logged in and tracking permission is granted`() {
        whenever(userManager.isUserLoggedIn) doReturn Observable.just(true)
        val permissionGranted =
            KeyValuePair
                .builder()
                .key(DATA_STORE_ANALYTICS_PERMISSION_KEY)
                .value(true.toString())
                .build()
        whenever(
            d2
                .dataStoreModule()
                .localDataStore()
                .value(DATA_STORE_ANALYTICS_PERMISSION_KEY)
                .blockingGet(),
        ) doReturn permissionGranted
        val systemInfo =
            SystemInfo
                .builder()
                .contextPath("https://play.dhis2.org")
                .version("2.40")
                .build()
        whenever(d2.systemInfoModule().systemInfo().blockingGet()) doReturn systemInfo

        presenter.init()

        verify(crashReportController).trackServer("https://play.dhis2.org", "2.40")
        verify(view).goToNextScreen(
            true,
            preferenceProvider.getBoolean(Preference.SESSION_LOCKED, false),
            preferenceProvider.getBoolean(Preference.INITIAL_METADATA_SYNC_DONE, false),
            preferenceProvider.getBoolean(Preference.INITIAL_DATA_SYNC_DONE, false),
        )
    }

    @Test
    fun `Should not track user info when user is logged in but tracking permission is not granted`() {
        whenever(userManager.isUserLoggedIn) doReturn Observable.just(true)
        val permissionDenied =
            KeyValuePair
                .builder()
                .key(DATA_STORE_ANALYTICS_PERMISSION_KEY)
                .value(false.toString())
                .build()
        whenever(
            d2
                .dataStoreModule()
                .localDataStore()
                .value(DATA_STORE_ANALYTICS_PERMISSION_KEY)
                .blockingGet(),
        ) doReturn permissionDenied

        presenter.init()

        verify(crashReportController, never()).trackServer(any(), any())
        verify(view).goToNextScreen(
            true,
            preferenceProvider.getBoolean(Preference.SESSION_LOCKED, false),
            preferenceProvider.getBoolean(Preference.INITIAL_METADATA_SYNC_DONE, false),
            preferenceProvider.getBoolean(Preference.INITIAL_DATA_SYNC_DONE, false),
        )
    }

    @Test
    fun `Should go to next screen when there is no user manager`() {
        val presenterWithoutUserManager =
            SplashPresenter(
                view,
                null,
                schedulerProvider,
                preferenceProvider,
                crashReportController,
            )

        presenterWithoutUserManager.init()

        verify(view).goToNextScreen(
            false,
            sessionLocked = false,
            initialSyncDone = false,
            initialDataSyncDone = false,
        )
    }

    @Test
    fun `Should clear disposable on destroy`() {
        presenter.compositeDisposable = CompositeDisposable()
        presenter.destroy()

        assert(presenter.compositeDisposable.size() == 0)
    }
}
