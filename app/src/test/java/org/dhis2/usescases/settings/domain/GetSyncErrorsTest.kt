package org.dhis2.usescases.settings.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.bindings.toDate
import org.dhis2.usescases.settings.SettingsRepository
import org.dhis2.usescases.settings.models.ErrorModelMapper
import org.dhis2.usescases.settings.models.ErrorViewModel
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

class GetSyncErrorsTest {
    private lateinit var getSyncErrors: GetSyncErrors
    private val settingsRepository: SettingsRepository = mock()
    private val errorMapper: ErrorModelMapper = mock()

    @Before
    fun setUp() {
        getSyncErrors =
            GetSyncErrors(
                settingsRepository = settingsRepository,
                errorMapper = errorMapper,
            )
    }

    @Test
    fun shouldLoadErrorList() =
        runTest {
            whenever(settingsRepository.d2Errors()) doReturn org.mockito.kotlin.mock()
            whenever(settingsRepository.trackerImportConflicts()) doReturn org.mockito.kotlin.mock()
            whenever(settingsRepository.foreignKeyViolations()) doReturn org.mockito.kotlin.mock()
            whenever(errorMapper.mapD2Error(any())) doReturn
                listOf(
                    ErrorViewModel(
                        creationDate = "2025-03-02T00:00:00.00Z".toDate(),
                        errorCode = "1",
                        errorDescription = "d2 error",
                        errorComponent = null,
                    ),
                )
            whenever(errorMapper.mapConflict(any())) doReturn
                listOf(
                    ErrorViewModel(
                        creationDate = "2025-03-05T00:00:00.00Z".toDate(),
                        errorCode = "2",
                        errorDescription = "conflict",
                        errorComponent = null,
                    ),
                )
            whenever(errorMapper.mapFKViolation(any())) doReturn
                listOf(
                    ErrorViewModel(
                        creationDate = "2025-03-01T00:00:00.00Z".toDate(),
                        errorCode = "3",
                        errorDescription = "fk",
                        errorComponent = null,
                    ),
                )
            val errorList = getSyncErrors()
            assertTrue(errorList.size == 3)
            assertTrue(errorList[0].errorCode == "3")
            assertTrue(errorList[1].errorCode == "1")
            assertTrue(errorList[2].errorCode == "2")
        }
}
