package org.dhis2.mobile.login.main.domain.usecase

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.login.main.data.LoginRepository
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import kotlin.test.Test

class ValidateServerTest {

    private val repository: LoginRepository = mock()
    private val validateServer = ValidateServer(repository)

    @Test
    fun `should append https if server url does not start with scheme`() = runTest {
        validateServer.invoke("testingsite.test", true)
        verify(repository).validateServer("https://testingsite.test", true)
    }
}