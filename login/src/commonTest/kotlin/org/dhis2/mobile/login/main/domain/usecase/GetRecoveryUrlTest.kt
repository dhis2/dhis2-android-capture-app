package org.dhis2.mobile.login.main.domain.usecase

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.login.main.data.LoginRepository
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals

class GetRecoveryUrlTest {
    private val repository: LoginRepository = mock()
    private val getRecoveryUrl = GetRecoveryUrl(repository)

    private val serverUrl = "https://test.server.org"

    @Test
    fun `should use login app recovery path for 2_43 servers`() =
        runTest {
            assertRecoveryPath(apiVersion = "2.43", expectedPath = RECOVERY_PATH_V43)
            assertRecoveryPath(apiVersion = "2.43.1", expectedPath = RECOVERY_PATH_V43)
        }

    @Test
    fun `should use login app recovery path for newer and snapshot servers`() =
        runTest {
            assertRecoveryPath(apiVersion = "2.44-SNAPSHOT", expectedPath = RECOVERY_PATH_V43)
            assertRecoveryPath(apiVersion = "3.0", expectedPath = RECOVERY_PATH_V43)
        }

    @Test
    fun `should use legacy recovery path for servers older than 2_43`() =
        runTest {
            assertRecoveryPath(apiVersion = "2.42.3", expectedPath = RECOVERY_PATH)
            assertRecoveryPath(apiVersion = "2.40", expectedPath = RECOVERY_PATH)
        }

    @Test
    fun `should use legacy recovery path when version is unknown or invalid`() =
        runTest {
            assertRecoveryPath(apiVersion = null, expectedPath = RECOVERY_PATH)
            assertRecoveryPath(apiVersion = "", expectedPath = RECOVERY_PATH)
            assertRecoveryPath(apiVersion = "2", expectedPath = RECOVERY_PATH)
            assertRecoveryPath(apiVersion = "unknown", expectedPath = RECOVERY_PATH)
        }

    @Test
    fun `should use legacy recovery path when version cannot be retrieved`() =
        runTest {
            whenever(repository.getServerApiVersion(any())) doThrow RuntimeException()

            assertEquals("$serverUrl$RECOVERY_PATH", getRecoveryUrl(serverUrl).getOrThrow())
        }

    @Test
    fun `should not duplicate slash when server url ends with slash`() =
        runTest {
            whenever(repository.getServerApiVersion(any())) doReturn "2.43"

            assertEquals("$serverUrl$RECOVERY_PATH_V43", getRecoveryUrl("$serverUrl/").getOrThrow())
        }

    private suspend fun assertRecoveryPath(
        apiVersion: String?,
        expectedPath: String,
    ) {
        whenever(repository.getServerApiVersion(serverUrl)) doReturn apiVersion

        assertEquals("$serverUrl$expectedPath", getRecoveryUrl(serverUrl).getOrThrow())
    }
}
