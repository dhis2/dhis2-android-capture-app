package org.dhis2.utils.session

import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.PreferenceProvider
import org.hisp.dhis.android.core.D2
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PinPresenterTest {

    lateinit var presenter: PinPresenter
    private var pinView: PinView = mock()
    private var preferenceProvider: PreferenceProvider = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    private val onPinCorrect: () -> Unit = mock()
    private val onError: () -> Unit = mock()
    private val onTwoManyAttempts: () -> Unit = mock()

    @Before
    fun setUp() {
        presenter = PinPresenter(pinView, preferenceProvider, d2)
    }

    @Test
    fun `Should return true if pin is correct`() {
        val testPin = "testPin"

        whenever(d2.dataStoreModule()) doReturn mock()
        whenever(d2.dataStoreModule().localDataStore()) doReturn mock()
        whenever(
            d2.dataStoreModule().localDataStore().value(Preference.PIN)
        )doReturn mock()
        whenever(
            d2.dataStoreModule().localDataStore().value(Preference.PIN).blockingGet()
        ) doReturn mock()
        whenever(
            d2.dataStoreModule().localDataStore().value(Preference.PIN).blockingGet().value()
        ) doReturn testPin

        presenter.unlockSession(
            testPin,
            attempts = 0,
            onError = onError,
            onPinCorrect = onPinCorrect,
            onTwoManyAttempts = onTwoManyAttempts
        )

        verify(preferenceProvider, times(1)).setValue(Preference.SESSION_LOCKED, true)
        verify(onPinCorrect).invoke()
    }

    @Test
    fun `Should return false if pin is wrong`() {
        val testPin = "testPin"
        val wrongPin = "wrongPin"

        whenever(d2.dataStoreModule()) doReturn mock()
        whenever(d2.dataStoreModule().localDataStore()) doReturn mock()
        whenever(
            d2.dataStoreModule().localDataStore().value(Preference.PIN)
        )doReturn mock()
        whenever(
            d2.dataStoreModule().localDataStore().value(Preference.PIN).blockingGet()
        ) doReturn mock()
        whenever(
            d2.dataStoreModule().localDataStore().value(Preference.PIN).blockingGet().value()
        ) doReturn testPin

        presenter.unlockSession(
            wrongPin,
            attempts = 0,
            onError = onError,
            onPinCorrect = onPinCorrect,
            onTwoManyAttempts = onTwoManyAttempts
        )

        verify(onError).invoke()
    }

    @Test
    fun `Should call onTwoManyAttempts when try 3 times`() {
        val testPin = "testPin"
        val wrongPin = "wrongPin"

        whenever(d2.dataStoreModule()) doReturn mock()
        whenever(d2.dataStoreModule().localDataStore()) doReturn mock()
        whenever(
            d2.dataStoreModule().localDataStore().value(Preference.PIN)
        )doReturn mock()
        whenever(
            d2.dataStoreModule().localDataStore().value(Preference.PIN).blockingGet()
        ) doReturn mock()
        whenever(
            d2.dataStoreModule().localDataStore().value(Preference.PIN).blockingGet().value()
        ) doReturn testPin

        presenter.unlockSession(
            wrongPin,
            attempts = 3,
            onError = onError,
            onPinCorrect = onPinCorrect,
            onTwoManyAttempts = onTwoManyAttempts
        )

        verify(onTwoManyAttempts).invoke()
    }

    @Test
    fun `Should save pin and block session`() {
        val testPin = "testPin"

        whenever(d2.dataStoreModule()) doReturn mock()
        whenever(d2.dataStoreModule().localDataStore()) doReturn mock()
        whenever(d2.dataStoreModule().localDataStore().value(Preference.PIN)) doReturn mock()

        presenter.savePin(testPin)
        verify(d2.dataStoreModule().localDataStore().value(Preference.PIN)).blockingSet(testPin)
        verify(preferenceProvider, times(1)).setValue(Preference.SESSION_LOCKED, true)
    }

    @Test
    fun `Should clear pin and block session when logout`() {
        whenever(d2.dataStoreModule()) doReturn mock()
        whenever(d2.dataStoreModule().localDataStore()) doReturn mock()
        whenever(d2.dataStoreModule().localDataStore().value(Preference.PIN)) doReturn mock()

        presenter.logOut()

        verify(d2.dataStoreModule().localDataStore().value(Preference.PIN)).blockingDelete()
        verify(preferenceProvider, times(1)).setValue(Preference.SESSION_LOCKED, false)
    }
}
