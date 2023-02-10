package org.dhis2.utils.session

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertFalse
import org.dhis2.commons.prefs.Preference
import org.dhis2.commons.prefs.PreferenceProvider
import org.hisp.dhis.android.core.D2
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class PinPresenterTest {

    lateinit var presenter: PinPresenter
    private var pinView: PinView = mock()
    private var preferenceProvider: PreferenceProvider = mock()
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

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
        val isUnlocked = presenter.unlockSession(testPin)

        verify(preferenceProvider, times(1)).setValue(Preference.SESSION_LOCKED, true)
        assert(isUnlocked)
    }

    @Test
    fun `Should return false if pin is wrong`() {
        val testPin = "testPin"
        val wrongPin = "wrongPin"

        whenever(preferenceProvider.getString(Preference.PIN, "")) doReturn testPin
        val isUnlocked = presenter.unlockSession(wrongPin)

        assertFalse(isUnlocked)
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
