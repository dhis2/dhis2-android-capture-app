package org.dhis2.utils.granularsync

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.dhis2.R
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.commons.resources.DhisPeriodUtils
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.sync.SyncContext
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.dhislogic.DhisProgramUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
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
    private val dispatcherProvider: DispatcherProvider =
        mock {
            on { io() } doReturn UnconfinedTestDispatcher()
        }

    @Before
    fun setUp() {
        whenever(resourceManager.getString(R.string.sync_dialog_title_synced)) doReturn "Synced"
        whenever(resourceManager.getString(R.string.sync_dialog_title_error)) doReturn "Sync error"
        whenever(resourceManager.getString(R.string.sync_dialog_action_refresh)) doReturn "Refresh"
        whenever(resourceManager.getString(R.string.sync_dialog_action_not_now)) doReturn "Not now"
        whenever(resourceManager.getString(R.string.resource_not_found, "programUid")) doReturn "programUid not found"
        whenever(resourceManager.getString(R.string.resource_not_found, "enrollmentUid")) doReturn "enrollmentUid not found"
        whenever(resourceManager.getString(R.string.resource_not_found, "eventUid")) doReturn "eventUid not found"
        whenever(resourceManager.getString(R.string.resource_not_found, "dataSetUid")) doReturn "dataSetUid not found"
        whenever(
            resourceManager.getString(
                R.string.sync_dialog_message_synced_program,
                "programUid",
            ),
        ) doReturn "Program synced"
        whenever(
            resourceManager.getString(
                R.string.sync_dialog_message_synced_program,
                "dataSetUid",
            ),
        ) doReturn "Dataset synced"
        whenever(
            resourceManager.getString(
                R.string.sync_dialog_message_synced_tei,
                "enrollmentUid",
            ),
        ) doReturn "Tei synced"
        whenever(
            resourceManager.getString(
                R.string.sync_dialog_message_synced_tei,
                "eventUid",
            ),
        ) doReturn "Event synced"
    }

    @Test
    fun `should return error ui state when program is missing`() =
        runTest {
            whenever(d2.programModule().programs().uid("programUid").blockingGet()) doReturn null

            assertMissingSyncTarget(
                syncContext = SyncContext.TrackerProgram("programUid"),
                expectedMessage = "programUid not found",
            )
        }

    @Test
    fun `should return error ui state when tei is missing`() =
        runTest {
            whenever(d2.enrollmentModule().enrollments().uid("enrollmentUid").blockingGet()) doReturn null
            whenever(d2.trackedEntityModule().trackedEntityInstances().uid("enrollmentUid").blockingGet()) doReturn null

            assertMissingSyncTarget(
                syncContext = SyncContext.TrackerProgramTei("enrollmentUid"),
                expectedMessage = "enrollmentUid not found",
            )
        }

    @Test
    fun `should return error ui state when event is missing`() =
        runTest {
            whenever(d2.eventModule().events().uid("eventUid").blockingGet()) doReturn null

            assertMissingSyncTarget(
                syncContext = SyncContext.Event("eventUid"),
                expectedMessage = "eventUid not found",
            )
        }

    @Test
    fun `should return error ui state when data set is missing`() =
        runTest {
            whenever(d2.dataSetModule().dataSets().uid("dataSetUid").blockingGet()) doReturn null

            assertMissingSyncTarget(
                syncContext = SyncContext.DataSet("dataSetUid"),
                expectedMessage = "dataSetUid not found",
            )
        }

    private suspend fun assertMissingSyncTarget(
        syncContext: SyncContext,
        expectedMessage: String,
    ) {
        val repository = repositoryFor(syncContext)

        try {
            repository.getUiState()
            fail("Expected MissingSyncTargetException")
        } catch (exception: MissingSyncTargetException) {
            assertEquals(State.ERROR, exception.uiState.syncState)
            assertEquals("Sync error", exception.uiState.title)
            assertEquals(expectedMessage, exception.uiState.message)
            assertEquals("Refresh", exception.uiState.mainActionLabel)
            assertEquals("Not now", exception.uiState.secondaryActionLabel)
            assertEquals(emptyList<Any>(), exception.uiState.content)
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
