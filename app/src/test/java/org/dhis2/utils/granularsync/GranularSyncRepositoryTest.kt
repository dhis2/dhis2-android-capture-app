package org.dhis2.utils.granularsync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.DhisPeriodUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.sync.SyncContext
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.dhislogic.DhisProgramUtils
import org.dhis2.utils.granularsync.data.GranularSyncRepository
import org.dhis2.utils.granularsync.domain.MissingSyncTargetException
import org.hisp.dhis.android.core.D2
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class GranularSyncRepositoryTest {
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val preferenceProvider: PreferenceProvider = mock()
    private val dhisProgramUtils: DhisProgramUtils = mock()
    private val periodUtils: DhisPeriodUtils = mock()
    private val resourceManager: ResourceManager = mock()
    private val testingDispatcher = UnconfinedTestDispatcher()
    private val dispatcherProvider: DispatcherProvider =
        mock {
            on { io() } doReturn testingDispatcher
        }

    @Before
    fun setUp() {
        Dispatchers.setMain(testingDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `should return error ui state when program is missing`() {
        runTest {
            whenever(d2.programModule().programs().uid("programUid").blockingGet()) doReturn null

            assertMissingSyncTarget(
                syncContext = SyncContext.TrackerProgram("programUid"),
                expectedRecordUid = "programUid",
            )
        }
    }

    @Test
    fun `should return error ui state when tei is missing`() {
        runTest {
            whenever(d2.enrollmentModule().enrollments().uid("enrollmentUid").blockingGet()) doReturn null
            whenever(d2.trackedEntityModule().trackedEntityInstances().uid("enrollmentUid").blockingGet()) doReturn null

            assertMissingSyncTarget(
                syncContext = SyncContext.TrackerProgramTei("enrollmentUid"),
                expectedRecordUid = "enrollmentUid",
            )
        }
    }

    @Test
    fun `should return error ui state when event is missing`() {
        runTest {
            whenever(d2.eventModule().events().uid("eventUid").blockingGet()) doReturn null

            assertMissingSyncTarget(
                syncContext = SyncContext.Event("eventUid"),
                expectedRecordUid = "eventUid",
            )
        }
    }

    @Test
    fun `should return error ui state when data set is missing`() {
        runTest {
            whenever(d2.dataSetModule().dataSets().uid("dataSetUid").blockingGet()) doReturn null

            assertMissingSyncTarget(
                syncContext = SyncContext.DataSet("dataSetUid"),
                expectedRecordUid = "dataSetUid",
            )
        }
    }

    private suspend fun assertMissingSyncTarget(
        syncContext: SyncContext,
        expectedRecordUid: String,
    ) {
        val repository = repositoryFor(syncContext)

        try {
            repository.getSyncStatus()
            fail("Expected MissingSyncTargetException")
        } catch (exception: MissingSyncTargetException) {
            assertEquals(expectedRecordUid, exception.recordUid)
        }
    }

    private fun repositoryFor(syncContext: SyncContext) =
        GranularSyncRepository(
            d2 = d2,
            syncContext = syncContext,
            preferenceProvider = preferenceProvider,
            dhisProgramUtils = dhisProgramUtils,
            periodUtils = periodUtils,
            resourceManager = resourceManager,
            dispatcher = dispatcherProvider,
        )
}
