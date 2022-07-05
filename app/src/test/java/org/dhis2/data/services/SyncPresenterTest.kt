package org.dhis2.data.services

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Observable
import junit.framework.Assert.assertTrue
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.service.SyncPresenterImpl
import org.dhis2.data.service.SyncStatusController
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.utils.analytics.AnalyticsHelper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.call.BaseD2Progress
import org.hisp.dhis.android.core.settings.GeneralSettings
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
    private val syncStatusController: SyncStatusController = mock()

    @Before
    fun setUp() {
        presenter = SyncPresenterImpl(
            d2,
            preferences,
            workManagerController,
            analyticsHelper,
            syncStatusController
        )
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
        val completable = Completable.fromObservable(
            Observable.just(BaseD2Progress.empty(1))
        ).test()
        whenever(d2.fileResourceModule()) doReturn mock()
        whenever(d2.fileResourceModule().fileResources()) doReturn mock()
        whenever(
            d2.fileResourceModule().fileResources().upload()
        ) doReturn Observable.just(BaseD2Progress.empty(1))

        presenter.uploadResources()

        completable.hasSubscription()
    }

    @Test
    fun `Should configure secondary tracker if configuration exists`() {
        whenever(
            d2.metadataModule().download()
        ) doReturn Observable.fromArray(
            BaseD2Progress.empty(2)
        )
        whenever(
            d2.settingModule().generalSetting().blockingGet()
        ) doReturn GeneralSettings.builder()
            .encryptDB(false)
            .matomoID(11111)
            .matomoURL("MatomoURL")
            .build()
        presenter.syncMetadata { }

        verify(analyticsHelper, times(1)).updateMatomoSecondaryTracker(any(), any(), any())
    }

    @Test
    fun `Should not configure secondary tracker if matomo settings is missing`() {
        whenever(
            d2.metadataModule().download()
        ) doReturn Observable.fromArray(
            BaseD2Progress.empty(2)
        )
        whenever(
            d2.settingModule().generalSetting().blockingGet()
        ) doReturn GeneralSettings.builder()
            .encryptDB(false)
            .build()
        presenter.syncMetadata { }

        verifyZeroInteractions(analyticsHelper)
    }

    @Test
    fun `Should not configure secondary tracker if no configuration exists`() {
        whenever(
            d2.metadataModule().download()
        ) doReturn Observable.fromArray(
            BaseD2Progress.empty(2)
        )
        whenever(
            d2.settingModule().generalSetting().blockingGet()
        ) doReturn null
        presenter.syncMetadata { }

        verify(analyticsHelper, times(0)).updateMatomoSecondaryTracker(any(), any(), any())
    }

    @Test
    fun `Should clear secondary tracker`() {
        whenever(
            d2.metadataModule().download()
        ) doReturn Observable.fromArray(
            BaseD2Progress.empty(2)
        )
        whenever(
            d2.settingModule().generalSetting().blockingGet()
        ) doReturn null
        presenter.syncMetadata { }

        verify(analyticsHelper, times(0)).updateMatomoSecondaryTracker(any(), any(), any())
        verify(analyticsHelper).clearMatomoSecondaryTracker()
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
