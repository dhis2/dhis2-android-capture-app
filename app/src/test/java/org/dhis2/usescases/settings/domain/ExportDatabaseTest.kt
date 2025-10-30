package org.dhis2.usescases.settings.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.mobile.commons.files.FileHandler
import org.dhis2.usescases.settings.SettingsRepository
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File

class ExportDatabaseTest {
    private lateinit var exportDatabase: ExportDatabase
    private val settingsRepository: SettingsRepository = mock()
    private val fileHandler: FileHandler = mock()
    private val settingsMessages: SettingsMessages = mock()
    private val resourceManager: ResourceManager = mock()

    @Before
    fun setUp() {
        exportDatabase =
            ExportDatabase(
                settingsRepository = settingsRepository,
                fileHandler = fileHandler,
                settingsMessages = settingsMessages,
                resourceManager = resourceManager,
            )
    }

    @Test
    fun `Should export database and send message`() =
        runTest {
            val mockedFile: File = org.mockito.kotlin.mock()
            val exportedMessage = "Database exported"
            whenever(settingsRepository.exportDatabase()) doReturn mockedFile
            whenever(resourceManager.getString(R.string.database_export_downloaded)) doReturn exportedMessage
            val result = exportDatabase()
            assertTrue(result == ExportDatabase.ExportResult.Success)
            verify(fileHandler, times(1)).copyAndOpen(any(), any())
            verify(settingsMessages, times(1)).sendMessage(exportedMessage)
        }

    @Test
    fun `Should display export database error`() =
        runTest {
            val errorMessage = "Database export failed!"
            val exceptionToThrow = RuntimeException("Simulated DB export error")
            whenever(settingsRepository.exportDatabase()) doThrow exceptionToThrow
            whenever(resourceManager.parseD2Error(exceptionToThrow)) doReturn errorMessage
            whenever(resourceManager.parseD2Error(any<Throwable>())) doAnswer { invocation ->
                val throwable = invocation.arguments[0] as Throwable
                if (throwable == exceptionToThrow) { // Check if it's the specific exception we threw
                    errorMessage
                } else {
                    "Some other error occurred" // Fallback for other potential errors
                }
            }

            val result = exportDatabase()
            assertTrue(result == ExportDatabase.ExportResult.Error)
            verify(settingsMessages, times(1)).sendMessage(errorMessage)
        }

    @Test
    fun `Should return share result`() =
        runTest {
            val mockedFile: File = org.mockito.kotlin.mock()
            whenever(settingsRepository.exportDatabase()) doReturn mockedFile
            val result = exportDatabase(ExportDatabase.ExportType.Share)
            assertTrue((result as? ExportDatabase.ExportResult.Share)?.db == mockedFile)
        }
}
