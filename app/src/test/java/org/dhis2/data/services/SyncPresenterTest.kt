package org.dhis2.data.services

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import junit.framework.Assert.assertTrue
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.service.SyncPresenterImpl
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.call.D2Progress
import org.hisp.dhis.android.core.settings.LimitScope
import org.hisp.dhis.android.core.settings.ProgramSetting
import org.hisp.dhis.android.core.settings.ProgramSettings
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class SyncPresenterTest {

    private lateinit var presenter: SyncPresenterImpl

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val preferences: PreferenceProvider = mock()
    private val workManagerController: WorkManagerController = mock()
    private val analyticsHelper: AnalyticsHelper = mock()

    @Before
    fun setUp() {
        presenter = SyncPresenterImpl(d2, preferences, workManagerController, analyticsHelper)
    }

    @Test
    fun `Should download events base on GLOBAL configuration`() {
        val mockedProgramSettings = mockedProgramSettings(
            100,
            200,
            LimitScope.GLOBAL
        )

        whenever(d2.settingModule().programSetting().blockingGet()) doReturn mockedProgramSettings

        val (eventLimit, limitByOU, limitByProgram) = presenter.getDownloadLimits()

        assertTrue(eventLimit == 200 && !limitByOU && !limitByProgram)
    }

    @Test
    fun `Should download events base on PER_OU_AND_PROGRAM configuration`() {
        val mockedProgramSettings = mockedProgramSettings(
            100,
            200,
            LimitScope.PER_OU_AND_PROGRAM
        )

        whenever(d2.settingModule().programSetting().blockingGet()) doReturn mockedProgramSettings

        val (eventLimit, limitByOU, limitByProgram) = presenter.getDownloadLimits()

        assertTrue(eventLimit == 200 && limitByOU && limitByProgram)
    }

    @Test
    fun `Should download events base on PER_PROGRAM configuration`() {
        val mockedProgramSettings = mockedProgramSettings(
            100,
            200,
            LimitScope.PER_PROGRAM
        )

        whenever(d2.settingModule().programSetting().blockingGet()) doReturn mockedProgramSettings

        val (eventLimit, limitByOU, limitByProgram) = presenter.getDownloadLimits()

        assertTrue(eventLimit == 200 && !limitByOU && limitByProgram)
    }

    @Test
    fun `Should download events base on PER_ORG_UNIT configuration`() {
        val mockedProgramSettings = mockedProgramSettings(
            100,
            200,
            LimitScope.PER_ORG_UNIT
        )

        whenever(d2.settingModule().programSetting().blockingGet()) doReturn mockedProgramSettings

        val (eventLimit, limitByOU, limitByProgram) = presenter.getDownloadLimits()

        assertTrue(eventLimit == 200 && limitByOU && !limitByProgram)
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

    private fun mockedProgramSettings(
        teiToDownload: Int,
        eventToDownload: Int,
        limitScope: LimitScope
    ): ProgramSettings {
        return ProgramSettings.builder()
            .globalSettings(
                ProgramSetting.builder()
                    .eventsDownload(eventToDownload)
                    .teiDownload(teiToDownload)
                    .settingDownload(limitScope)
                    .build()
            )
            .specificSettings(
                mutableMapOf(
                    Pair(
                        "programUid",
                        ProgramSetting.builder()
                            .eventsDownload(200)
                            .teiDownload(300)
                            .build()
                    )
                )
            )
            .build()
    }
}
