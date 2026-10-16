package org.dhis2.usescases.settings.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.bindings.toDate
import org.dhis2.mobile.commons.domain.invoke
import org.dhis2.usescases.settings.SettingsRepository
import org.dhis2.usescases.settings.models.ErrorModelMapper
import org.dhis2.usescases.settings.models.ErrorViewModel
import org.dhis2.usescases.settings.ui.ErrorLogType
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GetSyncErrorsTest {
    private val settingsRepository: SettingsRepository = mock()
    private val errorMapper: ErrorModelMapper = mock()

    private fun getSyncErrors(errorLogType: ErrorLogType) =
        GetSyncErrors(
            errorLogType = errorLogType,
            settingsRepository = settingsRepository,
            errorMapper = errorMapper,
        )

    @Test
    fun shouldLoadErrorList() =
        runTest {
            val getSyncErrors = getSyncErrors(ErrorLogType.Sync)
            whenever(settingsRepository.d2Errors()) doReturn org.mockito.kotlin.mock()
            whenever(settingsRepository.trackerImportConflicts()) doReturn org.mockito.kotlin.mock()
            whenever(errorMapper.mapD2Error(any())) doReturn
                listOf(
                    ErrorViewModel(
                        creationDate = "2025-03-02T00:00:00.00Z".toDate(),
                        creationDateLabel = "2025-03-02T00:00:00.00Z",
                        errorCode = "1",
                        errorDescription = "d2 error",
                        errorComponent = null,
                    ),
                )
            whenever(errorMapper.mapConflict(any())) doReturn
                listOf(
                    ErrorViewModel(
                        creationDate = "2025-03-05T00:00:00.00Z".toDate(),
                        creationDateLabel = "2025-03-05T00:00:00.00Z",
                        errorCode = "2",
                        errorDescription = "conflict",
                        errorComponent = null,
                    ),
                )
            val errorList = getSyncErrors()
            assertTrue(errorList.isSuccess)
            assertTrue(errorList.getOrNull()?.size == 2)
            assertTrue(errorList.getOrNull()?.get(0)?.errorCode == "2")
            assertTrue(errorList.getOrNull()?.get(1)?.errorCode == "1")
            verify(settingsRepository, never()).foreignKeyViolations()
        }

    @Test
    fun shouldLoadOnlyForeignKeyViolationsForMetadataIntegrity() =
        runTest {
            val getSyncErrors = getSyncErrors(ErrorLogType.MetadataIntegrity)
            whenever(settingsRepository.foreignKeyViolations()) doReturn org.mockito.kotlin.mock()
            whenever(errorMapper.mapFKViolation(any())) doReturn
                listOf(
                    ErrorViewModel(
                        creationDate = "2025-03-02T00:00:00.00Z".toDate(),
                        creationDateLabel = "2025-03-02T00:00:00.00Z",
                        errorCode = "Foreign key error",
                        errorDescription = "Missing a b from c d",
                        errorComponent = null,
                    ),
                )
            val errorList = getSyncErrors()
            assertTrue(errorList.isSuccess)
            assertTrue(errorList.getOrNull()?.size == 1)
            assertTrue(errorList.getOrNull()?.get(0)?.errorCode == "Foreign key error")
            verify(settingsRepository, never()).d2Errors()
            verify(settingsRepository, never()).trackerImportConflicts()
        }
}
