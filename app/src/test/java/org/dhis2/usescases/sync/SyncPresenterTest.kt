package org.dhis2.usescases.sync

import io.reactivex.Completable
import io.reactivex.Single
import org.dhis2.commons.Constants
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.server.UserManager
import org.dhis2.mobile.sync.data.SyncBackgroundJobAction
import org.dhis2.mobile.sync.model.SyncJobStatus
import org.dhis2.mobile.sync.model.SyncStatus
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SyncPresenterTest {
    lateinit var presenter: SyncPresenter
    private val view: SyncView = mock()
    private val userManager: UserManager =
        Mockito.mock(UserManager::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val schedulerProvider: SchedulerProvider = TrampolineSchedulerProvider()
    private val preferences: PreferenceProvider = mock()

    private val backgroundJobAction: SyncBackgroundJobAction = mock()

    @Before
    fun setUp() {
        presenter =
            SyncPresenter(
                view,
                userManager,
                schedulerProvider,
                backgroundJobAction,
                preferences,
            )
    }

    @Test
    fun `Should start initial sync`() {
        presenter.sync()
        verify(backgroundJobAction, times(1)).launchMetadataSync(0)
    }

    @Test
    fun `Should return work info live data`() {
        presenter.observeSyncProcess()
        verify(backgroundJobAction, times(1)).observeMetadataJob()
    }

    @Test
    fun `Should set metadata sync started`() {
        presenter.handleSyncInfo(arrayListOf(metaSyncJobStatus(SyncStatus.Running)))
        verify(view, times(1)).setMetadataSyncStarted()
    }

    @Test
    fun `Should set metadata sync succeeded`() {
        presenter.handleSyncInfo(arrayListOf(metaSyncJobStatus(SyncStatus.Succeed)))
        verify(view, times(1)).setMetadataSyncSucceed()
    }

    @Test
    fun `Should show metadata sync error message`() {
        val message = "Error message"
        presenter.handleSyncInfo(arrayListOf(metaSyncJobStatus(SyncStatus.Failed, message)))
        verify(view, times(1)).showMetadataFailedMessage(message)
    }

    @Test
    fun `Should get flag and theme after metadata sync`() {
        val flagAndTheme = Pair<String?, Int>("flag", -1)
        whenever(userManager.theme) doReturn Single.just(flagAndTheme)
        presenter.onMetadataSyncSuccess()
        verify(preferences, times(1)).setValue(Preference.FLAG, flagAndTheme.first)
        verify(preferences, times(1)).setValue(Preference.THEME, flagAndTheme.second)
        verify(view, times(1)).setFlag(flagAndTheme.first)
        verify(view, times(1)).setServerTheme(flagAndTheme.second)
        verify(view, times(1)).goToMain()
    }

    @Test
    fun `Should log out and go to login`() {
        whenever(userManager.logout()) doReturn Completable.complete()
        presenter.onLogout()
        verify(view, times(1)).goToLogin()
    }

    private fun metaSyncJobStatus(
        state: SyncStatus,
        message: String? = null,
    ) = SyncJobStatus(
        status = state,
        message = message,
        tags = listOf(Constants.META_NOW),
    )
}
