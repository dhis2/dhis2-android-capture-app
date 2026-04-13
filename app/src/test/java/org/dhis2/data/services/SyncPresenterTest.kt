package org.dhis2.data.services

import io.reactivex.Observable
import org.dhis2.commons.bindings.event
import org.dhis2.commons.bindings.program
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.data.service.SyncPresenterImpl
import org.dhis2.data.service.SyncRepository
import org.dhis2.data.service.SyncResult
import org.dhis2.mobile.sync.domain.SyncStatusController
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.call.D2Progress
import org.hisp.dhis.android.core.arch.call.D2ProgressStatus
import org.hisp.dhis.android.core.arch.call.D2ProgressSyncStatus
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramType
import org.hisp.dhis.android.core.settings.LimitScope
import org.hisp.dhis.android.core.settings.ProgramSetting
import org.hisp.dhis.android.core.settings.ProgramSettings
import org.hisp.dhis.android.core.settings.SynchronizationSettings
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.tracker.exporter.TrackerD2Progress
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SyncPresenterTest {
    private lateinit var presenter: SyncPresenterImpl

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val preferences: PreferenceProvider = mock()
    private val syncStatusController: SyncStatusController = mock()
    private val syncRepository: SyncRepository = mock()

    @Before
    fun setUp() {
        presenter =
            SyncPresenterImpl(
                d2,
                preferences,
                syncRepository,
                syncStatusController,
            )
    }

    @Test
    fun `Should download events base on GLOBAL configuration`() {
        val mockedProgramSettings =
            mockedProgramSettings(
                100,
                200,
                LimitScope.GLOBAL,
            )
        val mockedSyncSettings =
            mock<SynchronizationSettings> {
                on { programSettings() } doReturn mockedProgramSettings
            }

        whenever(
            d2.settingModule().synchronizationSettings().blockingGet(),
        ) doReturn mockedSyncSettings

        val (eventLimit, limitByOU, limitByProgram) = presenter.getDownloadLimits()

        assertTrue(eventLimit == 200 && !limitByOU && !limitByProgram)
    }

    @Test
    fun `Should download events base on PER_OU_AND_PROGRAM configuration`() {
        val mockedProgramSettings =
            mockedProgramSettings(
                100,
                200,
                LimitScope.PER_OU_AND_PROGRAM,
            )

        val mockedSyncSettings =
            mock<SynchronizationSettings> {
                on { programSettings() } doReturn mockedProgramSettings
            }

        whenever(
            d2.settingModule().synchronizationSettings().blockingGet(),
        ) doReturn mockedSyncSettings
        val (eventLimit, limitByOU, limitByProgram) = presenter.getDownloadLimits()

        assertTrue(eventLimit == 200 && limitByOU && limitByProgram)
    }

    @Test
    fun `Should download events base on PER_PROGRAM configuration`() {
        val mockedProgramSettings =
            mockedProgramSettings(
                100,
                200,
                LimitScope.PER_PROGRAM,
            )

        val mockedSyncSettings =
            mock<SynchronizationSettings> {
                on { programSettings() } doReturn mockedProgramSettings
            }

        whenever(
            d2.settingModule().synchronizationSettings().blockingGet(),
        ) doReturn mockedSyncSettings
        val (eventLimit, limitByOU, limitByProgram) = presenter.getDownloadLimits()

        assertTrue(eventLimit == 200 && !limitByOU && limitByProgram)
    }

    @Test
    fun `Should download events base on PER_ORG_UNIT configuration`() {
        val mockedProgramSettings =
            mockedProgramSettings(
                100,
                200,
                LimitScope.PER_ORG_UNIT,
            )

        val mockedSyncSettings =
            mock<SynchronizationSettings> {
                on { programSettings() } doReturn mockedProgramSettings
            }

        whenever(
            d2.settingModule().synchronizationSettings().blockingGet(),
        ) doReturn mockedSyncSettings
        val (eventLimit, limitByOU, limitByProgram) = presenter.getDownloadLimits()

        assertTrue(eventLimit == 200 && limitByOU && !limitByProgram)
    }

    @Test
    fun `Should return successfully SYNC if tei enrollment and events are ok`() {
        whenever(
            syncRepository.getTeiByNotInStates("uid", listOf(State.SYNCED, State.RELATIONSHIP)),
        ) doReturn emptyList()
        whenever(
            syncRepository.getEventsFromEnrollmentByNotInSyncState(
                "uid",
                listOf(State.SYNCED),
            ),
        ) doReturn emptyList()

        val syncResult = presenter.checkSyncTEIStatus("uid")

        assert(syncResult == SyncResult.SYNC)
    }

    @Test
    fun `Should return an incomplete sync if there are tei with TO_POST or TO_UPDATE syncState`() {
        whenever(
            syncRepository.getTeiByNotInStates("uid", listOf(State.SYNCED, State.RELATIONSHIP)),
        ) doReturn listOf(TrackedEntityInstance.builder().uid("uid").build())
        whenever(
            syncRepository.getEventsFromEnrollmentByNotInSyncState(
                "uid",
                listOf(State.SYNCED),
            ),
        ) doReturn emptyList()
        whenever(
            syncRepository.getTeiByInStates("uid", listOf(State.TO_POST, State.TO_UPDATE)),
        ) doReturn listOf(TrackedEntityInstance.builder().uid("uid").build())

        val syncResult = presenter.checkSyncTEIStatus("uid")

        assert(syncResult == SyncResult.INCOMPLETE)
    }

    @Test
    fun `Should return an ERROR sync if there are events of a tei without the SYNC syncState`() {
        whenever(
            syncRepository.getTeiByNotInStates("uid", listOf(State.SYNCED, State.RELATIONSHIP)),
        ) doReturn emptyList()
        whenever(
            syncRepository.getEventsFromEnrollmentByNotInSyncState(
                "uid",
                listOf(State.SYNCED),
            ),
        ) doReturn
            listOf(
                Event
                    .builder()
                    .uid("event")
                    .enrollment("uid")
                    .build(),
            )
        whenever(
            syncRepository.getTeiByInStates("uid", listOf(State.TO_POST, State.TO_UPDATE)),
        ) doReturn emptyList()

        val syncResult = presenter.checkSyncTEIStatus("uid")

        assert(syncResult == SyncResult.ERROR)
    }

    @Test
    fun syncGranularEvent() {
        mockEventUpload()
        mockEventDownload()
        mockFileResourceByEventCall()

        val testSubscriber = presenter.syncGranularEvent("uid").test()

        with(testSubscriber) {
            assertValueCount(5)
        }
    }

    @Test
    fun syncGranularTrackerProgram() {
        val mockedProgram =
            mock<Program> {
                on { programType() } doReturn ProgramType.WITH_REGISTRATION
            }
        whenever(d2.program("programUid")) doReturn mockedProgram

        mockTeiUploadByProgram()
        mockTeiDownloadByProgram()
        mockFileResourceByProgramCall()

        val testSubscriber = presenter.syncGranularProgram("programUid").test()

        with(testSubscriber) {
            assertValueCount(4)
        }
    }

    @Test
    fun syncGranularEventProgram() {
        val mockedProgram =
            mock<Program> {
                on { programType() } doReturn ProgramType.WITHOUT_REGISTRATION
            }
        whenever(d2.program("programUid")) doReturn mockedProgram

        mockEventUploadByProgram()
        mockEventDownloadByProgram()
        mockFileResourceByProgramCall()

        presenter.syncGranularProgram("programUid").test().assertValueCount(4)
    }

    @Test
    fun syncGranularNullProgram() {
        whenever(d2.program("programUid")) doReturn null

        mockFileResourceByProgramCall()

        val testSubscriber = presenter.syncGranularProgram("programUid").test()

        with(testSubscriber) {
            assertValueCount(0)
        }
    }

    private fun mockTeiUploadByProgram(programUid: String = "programUid") {
        whenever(syncRepository.uploadTrackerProgram(programUid)) doReturn Observable.empty()
    }

    private fun mockTeiDownloadByProgram(programUid: String = "programUid") {
        whenever(
            syncRepository.downloadTrackerProgram(programUid),
        ) doReturn
            Observable.fromArray(
                TrackerD2Progress.builder().build(),
                TrackerD2Progress
                    .builder()
                    .programs(
                        mapOf(
                            "programUid" to
                                D2ProgressStatus(
                                    true,
                                    D2ProgressSyncStatus.SUCCESS,
                                ),
                        ),
                    ).build(),
            )
    }

    private fun mockEventUpload(eventUid: String = "uid") {
        whenever(syncRepository.uploadEvent(eventUid)) doReturn Observable.empty()
    }

    private fun mockEventUploadByProgram(programUid: String = "programUid") {
        whenever(syncRepository.uploadEventProgram(programUid)) doReturn Observable.empty()
    }

    private fun mockEventDownloadByProgram(programUid: String = "programUid") {
        whenever(
            syncRepository.downloadEventProgram(programUid),
        ) doReturn
            Observable.fromArray(
                TrackerD2Progress.builder().build(),
                TrackerD2Progress
                    .builder()
                    .programs(
                        mapOf(
                            "programUid" to
                                D2ProgressStatus(
                                    true,
                                    D2ProgressSyncStatus.SUCCESS,
                                ),
                        ),
                    ).build(),
            )
    }

    private fun mockEventDownload(
        eventUid: String = "uid",
        programUid: String = "programUid",
    ) {
        whenever(d2.event(eventUid)) doReturn
            Event
                .builder()
                .uid(eventUid)
                .program(programUid)
                .build()
        whenever(
            syncRepository.downLoadEvent(eventUid, programUid),
        ) doReturn
            Observable.fromArray(
                TrackerD2Progress.builder().build(),
                TrackerD2Progress
                    .builder()
                    .programs(mapOf(Pair(programUid, D2ProgressStatus(false, null))))
                    .build(),
                TrackerD2Progress
                    .builder()
                    .programs(
                        mapOf(
                            Pair(
                                programUid,
                                D2ProgressStatus(true, D2ProgressSyncStatus.SUCCESS),
                            ),
                        ),
                    ).build(),
            )
    }

    private fun mockFileResourceByEventCall(eventUid: String = "uid") {
        whenever(
            syncRepository.downloadEventFiles(eventUid),
        ) doReturn
            Observable.fromArray(
                D2Progress(false, null, emptyList()),
                D2Progress(true, null, emptyList()),
            )
    }

    private fun mockFileResourceByProgramCall(programUid: String = "programUid") {
        whenever(
            syncRepository.downloadProgramFiles(programUid),
        ) doReturn
            Observable.fromArray(
                D2Progress(false, null, emptyList()),
                D2Progress(true, null, emptyList()),
            )
    }

    private fun mockedProgramSettings(
        teiToDownload: Int,
        eventToDownload: Int,
        limitScope: LimitScope,
    ): ProgramSettings =
        ProgramSettings
            .builder()
            .globalSettings(
                ProgramSetting
                    .builder()
                    .eventsDownload(eventToDownload)
                    .teiDownload(teiToDownload)
                    .settingDownload(limitScope)
                    .build(),
            ).specificSettings(
                mutableMapOf(
                    Pair(
                        "programUid",
                        ProgramSetting
                            .builder()
                            .eventsDownload(200)
                            .teiDownload(300)
                            .build(),
                    ),
                ),
            ).build()
}
