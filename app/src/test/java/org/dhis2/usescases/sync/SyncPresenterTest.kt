package org.dhis2.usescases.sync

import androidx.work.Data
import androidx.work.WorkInfo
import io.reactivex.Completable
import io.reactivex.Single
import java.util.UUID
import org.dhis2.commons.Constants
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.server.UserManager
import org.dhis2.data.service.METADATA_MESSAGE
import org.dhis2.data.service.workManager.WorkManagerController
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
    private val workManagerController: WorkManagerController = mock()
    private val preferences: PreferenceProvider = mock()

    @Before
    fun setUp() {
        presenter = SyncPresenter(
            view,
            userManager,
            schedulerProvider,
            workManagerController,
            preferences
        )
    }

    @Test
    fun `Should start initial sync`() {
        presenter.sync()
        verify(workManagerController, times(1)).syncMetaDataForWorker(
            Constants.META_NOW,
            Constants.INITIAL_SYNC
        )
    }

    @Test
    fun `Should return work info live data`() {
        presenter.observeSyncProcess()
        verify(workManagerController, times(1))
            .getWorkInfosForUniqueWorkLiveData(Constants.INITIAL_SYNC)
    }

    @Test
    fun `Should set metadata sync started`() {
        presenter.handleSyncInfo(arrayListOf(metaWorkInfo(WorkInfo.State.RUNNING)))
        verify(view, times(1)).setMetadataSyncStarted()
    }

    @Test
    fun `Should set metadata sync succeeded`() {
        presenter.handleSyncInfo(arrayListOf(metaWorkInfo(WorkInfo.State.SUCCEEDED)))
        verify(view, times(1)).setMetadataSyncSucceed()
    }

    @Test
    fun `Should show metadata sync error message`() {
        val message = "Error message"
        presenter.handleSyncInfo(arrayListOf(metaWorkInfo(WorkInfo.State.FAILED, message)))
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

    private fun metaWorkInfo(state: WorkInfo.State, message: String? = null): WorkInfo {
        return WorkInfo(
            UUID.randomUUID(),
            state,
            Data.Builder().apply { putString(METADATA_MESSAGE, message) }.build(),
            arrayListOf(Constants.META_NOW),
            Data.EMPTY,
            0
        )
    }
}
