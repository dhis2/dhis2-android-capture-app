package org.dhis2.usescases.sync

import androidx.work.Data
import androidx.work.WorkInfo
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Single
import java.util.UUID
import org.dhis2.data.prefs.Preference
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.server.UserManager
import org.dhis2.data.service.METADATA_MESSAGE
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.utils.Constants
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

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
        verify(workManagerController, times(1)).syncDataForWorkers(
            Constants.META_NOW,
            Constants.DATA_NOW,
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
    fun `Should set data sync started`() {
        presenter.handleSyncInfo(arrayListOf(dataWorkInfo(WorkInfo.State.RUNNING)))
        verify(view, times(1)).setDataSyncStarted()
    }

    @Test
    fun `Should set data sync succeeded`() {
        presenter.handleSyncInfo(arrayListOf(dataWorkInfo(WorkInfo.State.SUCCEEDED)))
        verify(view, times(1)).setDataSyncSucceed()
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
    }

    @Test
    fun `Should sync reserved values after data sync`() {
        presenter.onDataSyncSuccess()
        verify(preferences, times(1)).setValue(Preference.INITIAL_SYNC_DONE, true)
        verify(workManagerController, times(1)).cancelAllWorkByTag(Constants.RESERVED)
        verify(workManagerController, times(1)).syncDataForWorker(any())
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
            arrayListOf(Constants.META_NOW), 0
        )
    }

    private fun dataWorkInfo(state: WorkInfo.State): WorkInfo {
        return WorkInfo(
            UUID.randomUUID(),
            state,
            Data.Builder().build(),
            arrayListOf(Constants.DATA_NOW), 0
        )
    }
}
