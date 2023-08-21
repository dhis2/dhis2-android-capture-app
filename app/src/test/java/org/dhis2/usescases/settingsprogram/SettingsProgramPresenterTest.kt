package org.dhis2.usescases.settingsprogram

import io.reactivex.Single
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.settings.ProgramSetting
import org.hisp.dhis.android.core.settings.ProgramSettings
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.Mockito.anyList
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SettingsProgramPresenterTest {

    lateinit var presenter: SettingsProgramPresenter
    private val view: ProgramSettingsView = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, RETURNS_DEEP_STUBS)
    private val scheduler = TrampolineSchedulerProvider()

    @Before
    fun setUp() {
        presenter = SettingsProgramPresenter(d2, view, scheduler)
    }

    @Test
    fun `Should initialize the settings program`() {
        val programSetting = ProgramSettings.builder()
            .specificSettings(hashMapOf("setting" to ProgramSetting.builder().uid("uid").build()))
            .build()
        val program = Program.builder()
            .uid("uid")
            .style(ObjectStyle.builder().color("color").icon("icon").build())
            .build()

        whenever(d2.settingModule()) doReturn mock()
        whenever(d2.settingModule().programSetting()) doReturn mock()
        whenever(d2.settingModule().programSetting().get()) doReturn Single.just(programSetting)
        whenever(d2.programModule()) doReturn mock()
        whenever(d2.programModule().programs()) doReturn mock()
        whenever(d2.programModule().programs().uid("uid")) doReturn mock()
        whenever(d2.programModule().programs().uid("uid").blockingGet()) doReturn program

        presenter.init()

        verify(view).setData(anyList())
    }

    @Test
    fun `Should clear the disposable`() {
        presenter.dispose()

        val disposableSize = presenter.disposable.size()

        assert(disposableSize == 0)
    }
}
