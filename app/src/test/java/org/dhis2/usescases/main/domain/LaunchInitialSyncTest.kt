package org.dhis2.usescases.main.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.commons.Constants
import org.dhis2.data.service.VersionRepository
import org.dhis2.data.service.workManager.WorkManagerController
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.usescases.main.data.HomeRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.given
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class LaunchInitialSyncTest {
    private val homeRepository: HomeRepository = mock()
    private val versionRepository: VersionRepository = mock()
    private val workManagerController: WorkManagerController = mock()
    private lateinit var launchInitialSync: LaunchInitialSync

    @Test
    fun `should return Skip if skipSync is true`() =
        runTest {
            launchInitialSync =
                LaunchInitialSync(
                    skipSync = true,
                    homeRepository = homeRepository,
                    versionRepository = versionRepository,
                    workManagerController = workManagerController,
                )

            val result = launchInitialSync()

            assertTrue(result.isSuccess)
            assertEquals(InitialSyncAction.Skip, result.getOrNull())
            verifyNoInteractions(homeRepository)
            verifyNoInteractions(versionRepository)
            verifyNoInteractions(workManagerController)
        }

    @Test
    fun `should return Skip if database is imported`() =
        runTest {
            whenever(homeRepository.isImportedDb()) doReturn true
            launchInitialSync =
                LaunchInitialSync(
                    skipSync = false,
                    homeRepository = homeRepository,
                    versionRepository = versionRepository,
                    workManagerController = workManagerController,
                )

            val result = launchInitialSync()

            assertTrue(result.isSuccess)
            assertEquals(InitialSyncAction.Skip, result.getOrNull())
        }

    @Test
    fun `should return Skip if initial sync is done`() =
        runTest {
            whenever(homeRepository.isImportedDb()) doReturn false
            whenever(homeRepository.getInitialSyncDone()) doReturn true
            launchInitialSync =
                LaunchInitialSync(
                    skipSync = false,
                    homeRepository = homeRepository,
                    versionRepository = versionRepository,
                    workManagerController = workManagerController,
                )

            val result = launchInitialSync()

            assertTrue(result.isSuccess)
            assertEquals(InitialSyncAction.Skip, result.getOrNull())
        }

    @Test
    fun `should return Syncing and launch initial sync`() =
        runTest {
            whenever(homeRepository.isImportedDb()) doReturn false
            whenever(homeRepository.getInitialSyncDone()) doReturn false
            launchInitialSync =
                LaunchInitialSync(
                    skipSync = false,
                    homeRepository = homeRepository,
                    versionRepository = versionRepository,
                    workManagerController = workManagerController,
                )

            val result = launchInitialSync()

            assertTrue(result.isSuccess)
            assertEquals(InitialSyncAction.Syncing, result.getOrNull())
            verify(versionRepository).checkVersionUpdates()
            verify(workManagerController).syncDataForWorker(Constants.DATA_NOW, Constants.INITIAL_SYNC)
        }

    @Test
    fun `should return failure if check version update fails`() =
        runTest {
            val exception = DomainError.DataBaseError("Error")
            whenever(homeRepository.isImportedDb()) doReturn false
            whenever(homeRepository.getInitialSyncDone()) doReturn false
            given(versionRepository.checkVersionUpdates()).willAnswer {
                throw exception
            }
            launchInitialSync =
                LaunchInitialSync(
                    skipSync = false,
                    homeRepository = homeRepository,
                    versionRepository = versionRepository,
                    workManagerController = workManagerController,
                )

            val result = launchInitialSync()

            assertTrue(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
        }

    @Test
    fun `should return failure if home repository fails`() =
        runTest {
            val exception = DomainError.DataBaseError("Error")
            given(homeRepository.isImportedDb()).willAnswer {
                throw exception
            }
            launchInitialSync =
                LaunchInitialSync(
                    skipSync = false,
                    homeRepository = homeRepository,
                    versionRepository = versionRepository,
                    workManagerController = workManagerController,
                )

            val result = launchInitialSync()

            assertTrue(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
        }
}
