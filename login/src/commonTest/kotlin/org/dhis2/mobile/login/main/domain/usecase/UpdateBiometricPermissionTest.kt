package org.dhis2.mobile.login.main.domain.usecase

import coil3.PlatformContext
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.biometrics.BiometricActions
import org.dhis2.mobile.commons.providers.PreferenceProvider
import org.dhis2.mobile.login.main.data.LoginErrorMessageProvider
import org.dhis2.mobile.login.main.data.LoginRepository
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertTrue

class UpdateBiometricPermissionTest {
    private val loginRepository: LoginRepository = mock()
    private val preferences: PreferenceProvider = mock()
    private val biometrics: BiometricActions = mock()
    private val messages: LoginErrorMessageProvider = mock()
    private val platformContext: PlatformContext = mock()

    private val updateBiometricPermission =
        UpdateBiometricPermission(loginRepository, preferences, biometrics, messages)

    @Test
    fun `GIVEN the credentials are already saved WHEN invoked THEN it fails without saving`() =
        runTest {
            whenever(preferences.areSameCredentials(SERVER_URL, USERNAME)) doReturn true

            val result = invoke(granted = true)

            assertTrue(result.isFailure)
            verify(preferences, never()).saveUserCredentials(any(), any(), anyOrNull())
        }

    @Test
    fun `GIVEN biometrics without a protected secret WHEN granted THEN the password is protected by biometrics`() =
        runTest {
            givenBiometricsWithoutProtectedSecret()
            whenever(with(platformContext) { biometrics.protectSecret(PASSWORD) }) doReturn Result.success(Unit)

            val result = invoke(granted = true)

            assertTrue(result.isSuccess)
            with(platformContext) { verify(biometrics).protectSecret(PASSWORD) }
            verify(preferences).saveUserCredentials(SERVER_URL, USERNAME, null)
            verify(loginRepository).updateBiometricsPermissions(true)
        }

    @Test
    fun `GIVEN the biometric prompt fails WHEN granted THEN it fails without saving`() =
        runTest {
            givenBiometricsWithoutProtectedSecret()
            whenever(with(platformContext) { biometrics.protectSecret(PASSWORD) }) doReturn
                Result.failure(Exception("cancelled"))

            val result = invoke(granted = true)

            assertTrue(result.isFailure)
            verify(preferences, never()).saveUserCredentials(any(), any(), anyOrNull())
            verify(loginRepository, never()).updateBiometricsPermissions(any())
        }

    @Test
    fun `GIVEN biometrics are not granted WHEN invoked THEN the password is saved without biometrics`() =
        runTest {
            givenBiometricsWithoutProtectedSecret()

            val result = invoke(granted = false)

            assertTrue(result.isSuccess)
            with(platformContext) { verify(biometrics, never()).protectSecret(PASSWORD) }
            verify(preferences).saveUserCredentials(SERVER_URL, USERNAME, PASSWORD)
            verify(loginRepository).updateBiometricsPermissions(false)
        }

    private fun givenBiometricsWithoutProtectedSecret() {
        whenever(preferences.areSameCredentials(SERVER_URL, USERNAME)) doReturn false
        whenever(biometrics.hasBiometric()) doReturn true
        whenever(biometrics.hasProtectedSecret()) doReturn false
    }

    private suspend fun invoke(granted: Boolean) =
        with(platformContext) {
            updateBiometricPermission(SERVER_URL, USERNAME, PASSWORD, granted)
        }

    private companion object {
        const val SERVER_URL = "https://server"
        const val USERNAME = "user"
        const val PASSWORD = "password"
    }
}
