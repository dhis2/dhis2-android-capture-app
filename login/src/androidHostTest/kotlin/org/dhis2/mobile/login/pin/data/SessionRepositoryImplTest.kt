package org.dhis2.mobile.login.pin.data

import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.login.pin.domain.model.PinVerification
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.datastore.KeyValuePair
import org.mockito.Mockito
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SessionRepositoryImplTest {
    private companion object {
        const val PIN = "4826"
    }

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val testDispatcher = UnconfinedTestDispatcher()
    private val pinHasher = PinHasher(fakePinKey())
    private val pinValue get() = d2.dataStoreModule().localDataStore().value("pin")

    private fun repository(pinKey: PinKey = fakePinKey()) =
        SessionRepositoryImpl(
            d2 = d2,
            preferenceProvider = mock(),
            domainErrorMapper = mock(),
            dispatcher = Dispatcher(testDispatcher, testDispatcher, testDispatcher),
            pinHasher = PinHasher(pinKey),
        )

    @Test
    fun `savePin stores a hash instead of the PIN`() =
        runTest(testDispatcher) {
            repository().savePin(PIN)

            assertStoredHashOf(PIN)
        }

    @Test
    fun `verifyPin matches a hashed PIN without rewriting it`() =
        runTest(testDispatcher) {
            givenStoredValue(pinHasher.hash(PIN))

            assertEquals(PinVerification.Match, repository().verifyPin(PIN))
            verify(pinValue, never()).blockingSet(Mockito.anyString())
        }

    @Test
    fun `verifyPin rejects a wrong PIN without rewriting the stored value`() =
        runTest(testDispatcher) {
            listOf(pinHasher.hash(PIN), PIN).forEach { storedValue ->
                givenStoredValue(storedValue)

                assertEquals(PinVerification.Mismatch, repository().verifyPin("0000"))
            }
            verify(pinValue, never()).blockingSet(Mockito.anyString())
        }

    @Test
    fun `verifyPin returns NoPinStored when there is no PIN or no device key`() =
        runTest(testDispatcher) {
            whenever(pinValue.blockingGet()) doReturn null
            assertEquals(PinVerification.NoPinStored, repository().verifyPin(PIN))

            givenStoredValue(pinHasher.hash(PIN))
            assertEquals(PinVerification.NoPinStored, repository(missingPinKey).verifyPin(PIN))
        }

    @Test
    fun `verifyPin migrates a legacy plaintext PIN to a hash`() =
        runTest(testDispatcher) {
            givenStoredValue(PIN)

            assertEquals(PinVerification.Match, repository().verifyPin(PIN))
            assertStoredHashOf(PIN)
        }

    private fun givenStoredValue(value: String) {
        val keyValuePair = mock<KeyValuePair> { on { value() } doReturn value }
        whenever(pinValue.blockingGet()) doReturn keyValuePair
    }

    private fun assertStoredHashOf(pin: String) {
        val stored = argumentCaptor<String>()
        verify(pinValue).blockingSet(stored.capture())
        assertFalse(stored.firstValue.contains(pin))
        assertEquals(PinHasher.Verification.Match(needsUpgrade = false), pinHasher.verify(pin, stored.firstValue))
    }
}
