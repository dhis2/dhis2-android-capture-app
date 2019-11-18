package org.dhis2.data.services

import androidx.work.WorkManager
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.service.SyncPresenterImpl
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.call.D2Progress
import org.junit.Before
import org.junit.Test

class SyncPresenterTest {

    private lateinit var presenter: SyncPresenterImpl

    private val d2: D2 = mock()
    private val preferences: PreferenceProvider = mock()
    private val workManager: WorkManager = mock()

    @Before
    fun setUp() {
        presenter = SyncPresenterImpl(d2, preferences, workManager)
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
}
