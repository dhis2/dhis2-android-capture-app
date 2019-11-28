package org.dhis2.data.services

import androidx.work.ExistingWorkPolicy.REPLACE
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import org.dhis2.data.prefs.Preference.Companion.TIME_DAILY
import org.dhis2.data.prefs.Preference.Companion.TIME_DATA
import org.dhis2.data.prefs.Preference.Companion.TIME_META
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.service.SyncPresenterImpl
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.data.service.workManager.WorkerItem
import org.dhis2.data.service.workManager.WorkerType
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.call.D2Progress
import org.junit.Before
import org.junit.Test

class SyncPresenterImplTest {

    private lateinit var presenter: SyncPresenterImpl

    private val d2: D2 = mock()
    private val preferences: PreferenceProvider = mock()
    private val workManagerController: WorkManagerController = mock()

    @Before
    fun setUp() {
        presenter = SyncPresenterImpl(d2, preferences, workManagerController)
    }

    @Test
    fun `Should upload file resources`() {
        val completable = Completable.fromObservable(Observable.just(D2Progress.empty(1))).test()
        whenever(d2.fileResourceModule()) doReturn mock()
        whenever(d2.fileResourceModule().fileResources()) doReturn mock()
        whenever(
            d2.fileResourceModule().fileResources().upload()
        ) doReturn Observable.just(D2Progress.empty(1))

        presenter.uploadResources()

        completable.hasSubscription()
    }

    @Test
    fun `Should start periodic data worker`() {
        val seconds = 2
        val workerItem = WorkerItem("DATA", WorkerType.DATA, seconds.toLong(), policy = REPLACE)
        whenever(preferences.getInt(TIME_DATA, TIME_DAILY)) doReturn seconds

        presenter.startPeriodicDataWork()

        verify(workManagerController).cancelUniqueWork(any())
        verify(workManagerController).syncDataForWorker(workerItem)
    }

    @Test
    fun `Should not a start periodic data worker if seconds are equal to zero`() {
        val seconds = 0
        whenever(preferences.getInt(TIME_DATA, TIME_DAILY)) doReturn seconds

        presenter.startPeriodicDataWork()

        verify(workManagerController).cancelUniqueWork(any())
        verifyNoMoreInteractions(workManagerController)
    }

    @Test
    fun `Should start periodic metadata worker`() {
        val seconds = 2
        val workerItem =
            WorkerItem("METADATA", WorkerType.METADATA, seconds.toLong(), policy = REPLACE)
        whenever(preferences.getInt(TIME_META, TIME_DAILY)) doReturn seconds

        presenter.startPeriodicMetaWork()

        verify(workManagerController).cancelUniqueWork(any())
        verify(workManagerController).syncDataForWorker(workerItem)
    }

    @Test
    fun `Should not a start periodic metadata worker if seconds are equal to zero`() {
        val seconds = 0
        whenever(preferences.getInt(TIME_DATA, TIME_DAILY)) doReturn seconds

        presenter.startPeriodicDataWork()

        verify(workManagerController).cancelUniqueWork(any())
        verifyNoMoreInteractions(workManagerController)
    }
}
