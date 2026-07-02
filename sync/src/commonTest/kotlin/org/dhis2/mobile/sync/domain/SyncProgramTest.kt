package org.dhis2.mobile.sync.domain

import kotlinx.coroutines.runBlocking
import org.dhis2.mobile.sync.data.SyncProgramRepository
import org.dhis2.mobile.sync.model.ProgramType
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertTrue

class SyncProgramTest {
    private val repository: SyncProgramRepository = mock()
    private val syncStatusController = SyncStatusController()
    private val useCase = SyncProgram(repository, syncStatusController)

    @Test
    fun `should sync event program successfully when all events are synced`() =
        runBlocking {
            val programUid = "eventProgramUid"
            whenever(repository.getProgramType(programUid)).thenReturn(ProgramType.Event)
            whenever(repository.allEventsAreSynced(programUid)).thenReturn(true)

            val result = useCase(programUid)

            assertTrue(result.isSuccess)
            verify(repository).uploadEventProgram(programUid)
            verify(repository).downloadEventProgram(programUid)
            verify(repository).downloadFileResources(programUid)
        }

    @Test
    fun `should not download event program when events are not synced`() =
        runBlocking {
            val programUid = "eventProgramUid"
            whenever(repository.getProgramType(programUid)).thenReturn(ProgramType.Event)
            whenever(repository.allEventsAreSynced(programUid)).thenReturn(false)

            val result = useCase(programUid)

            assertTrue(result.isFailure)
            verify(repository).uploadEventProgram(programUid)
            verify(repository, never()).downloadEventProgram(programUid)
            verify(repository, never()).downloadFileResources(programUid)
        }

    @Test
    fun `should sync tracker program successfully when all teis are synced`() =
        runBlocking {
            val programUid = "trackerProgramUid"
            whenever(repository.getProgramType(programUid)).thenReturn(ProgramType.Tracker)
            whenever(repository.allTeisAreSynced(programUid)).thenReturn(true)

            val result = useCase(programUid)

            assertTrue(result.isSuccess)
            verify(repository).uploadTrackerProgram(programUid)
            verify(repository).downloadTrackerProgram(programUid)
            verify(repository).downloadFileResources(programUid)
        }

    @Test
    fun `should not download tracker program when teis are not synced`() =
        runBlocking {
            val programUid = "trackerProgramUid"
            whenever(repository.getProgramType(programUid)).thenReturn(ProgramType.Tracker)
            whenever(repository.allTeisAreSynced(programUid)).thenReturn(false)

            val result = useCase(programUid)

            assertTrue(result.isFailure)
            verify(repository).uploadTrackerProgram(programUid)
            verify(repository, never()).downloadTrackerProgram(programUid)
            verify(repository, never()).downloadFileResources(programUid)
        }

    @Test
    fun `should return failure for unknown program type`() =
        runBlocking {
            val programUid = "unknownProgramUid"
            whenever(repository.getProgramType(programUid)).thenReturn(ProgramType.None)

            val result = useCase(programUid)

            assertTrue(result.isFailure)
        }

    @Test
    fun `should return failure when getProgramType throws`() =
        runBlocking {
            val programUid = "programUid"
            whenever(repository.getProgramType(programUid)).thenThrow(RuntimeException("type error"))

            val result = useCase(programUid)

            assertTrue(result.isFailure)
        }
}
