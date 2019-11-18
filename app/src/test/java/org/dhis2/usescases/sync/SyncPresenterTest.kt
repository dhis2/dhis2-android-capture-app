package org.dhis2.usescases.sync

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.data.service.workManager.WorkerType
import org.dhis2.utils.Constants
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.settings.SystemSetting
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.RETURNS_DEEP_STUBS

class SyncPresenterTest {

    private lateinit var presenter: SyncPresenter

    private val view: SyncView = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, RETURNS_DEEP_STUBS)
    private val schedulers = TrampolineSchedulerProvider()
    private val workManagerController: WorkManagerController = mock()

    @Before
    fun setUp() {
        presenter = SyncPresenter(view, d2, schedulers, workManagerController)
    }

    @Test
    fun `Should sync data and metadata values`() {
        presenter.sync()

        verify(workManagerController).syncDataForWorkers(any(), any(), any())
    }

    @Test
    fun `Should set app's theme`() {
        val flag =
            SystemSetting.builder().key(SystemSetting.SystemSettingKey.FLAG).value("flag").build()
        val style =
            SystemSetting.builder().key(SystemSetting.SystemSettingKey.STYLE).value("green").build()

        whenever(
            d2.systemSettingModule().systemSetting().get()
        ) doReturn Single.just(listOf(flag, style))

        presenter.getTheme()

        verify(view).saveFlag("flag")
        verify(view).saveTheme("green")
    }

    @Test
    fun `Should sync reserved values`() {
        val tag = Constants.RESERVED

        presenter.syncReservedValues()

        verify(workManagerController).cancelAllWorkByTag(any())
        verify(workManagerController).syncDataForWorker(WorkerType.RESERVED, tag)
    }

    @Test
    fun `Should dispose of the disposables`() {
        presenter.onDettach()

        assert(presenter.disposable.size() == 0)
    }

    @Test
    fun `Should display message`() {
        val message = "message"

        presenter.displayMessage(message)

        verify(view).displayMessage(message)
    }
}
