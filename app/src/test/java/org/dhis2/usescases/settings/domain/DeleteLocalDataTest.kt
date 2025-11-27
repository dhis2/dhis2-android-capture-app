package org.dhis2.usescases.settings.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.settings.SettingsRepository
import org.dhis2.utils.analytics.AnalyticsHelper
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DeleteLocalDataTest {
    private lateinit var deleteLocalData: DeleteLocalData
    private val settingsRepository: SettingsRepository = mock()
    private val settingsMessages: SettingsMessages = mock()
    private val resourceManager: ResourceManager = mock()
    private val analyticsHelper: AnalyticsHelper = mock()

    @Before
    fun setUp() {
        deleteLocalData =
            DeleteLocalData(
                settingsRepository,
                settingsMessages,
                resourceManager,
                analyticsHelper,
            )
    }

    @Test
    fun shouldDeleteData() =
        runTest {
            val doneMessage = "done"
            whenever(resourceManager.getString(R.string.delete_local_data_done)) doReturn doneMessage
            deleteLocalData()
            verify(settingsRepository).deleteLocalData()
            verify(settingsMessages, times(1)).sendMessage(doneMessage)
        }

    @Test
    fun shouldSendErrorMessage() =
        runTest {
            val errorMessage = "error"
            whenever(resourceManager.getString(R.string.delete_local_data_error)) doReturn errorMessage
            whenever(settingsRepository.deleteLocalData()) doThrow RuntimeException()
            deleteLocalData()
            verify(settingsMessages, times(1)).sendMessage(errorMessage)
        }
}
