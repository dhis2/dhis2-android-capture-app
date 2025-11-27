package org.dhis2.usescases.settings.domain

import kotlinx.coroutines.test.runTest
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.data.service.VersionRepository
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CheckVersionUpdateTest {
    private lateinit var checkVersionUpdate: CheckVersionUpdate
    private val versionRepository: VersionRepository = mock()
    private val settingsMessages: SettingsMessages = mock()
    private val resourceManager: ResourceManager = mock()

    @Before
    fun setUp() {
        checkVersionUpdate =
            CheckVersionUpdate(
                versionRepository = versionRepository,
                settingsMessages = settingsMessages,
                resourceManager = resourceManager,
            )
    }

    @Test
    fun `should check version update`() =
        runTest {
            whenever(versionRepository.getLatestVersionInfo()) doReturn "new.version.name"
            checkVersionUpdate()
            verify(versionRepository, times(1)).checkVersionUpdates()
        }

    @Test
    fun `should send no new version message`() =
        runTest {
            val noUpdateMsg = "No updates"
            whenever(versionRepository.getLatestVersionInfo()) doReturn null
            whenever(resourceManager.getString(R.string.no_updates)) doReturn noUpdateMsg
            checkVersionUpdate()
            verify(settingsMessages, times(1)).sendMessage(noUpdateMsg)
            verify(versionRepository, times(0)).checkVersionUpdates()
        }
}
