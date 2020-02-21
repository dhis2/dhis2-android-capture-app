package org.dhis2.utils.session

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertFalse
import org.dhis2.data.prefs.Preference
import org.dhis2.data.prefs.PreferenceProvider
import org.junit.Before
import org.junit.Test

class PinPresenterTest {

    lateinit var presenter: PinPresenter
    private var pinView: PinView = mock()
    private var preferenceProvider: PreferenceProvider = mock()

    @Before
    fun setUp() {
        presenter = PinPresenter(pinView, preferenceProvider)
    }

    @Test
    fun `Should return true if pin is correct`() {

        val testPin = "testPin"

        whenever(preferenceProvider.getString(Preference.PIN,"")) doReturn testPin
        val isUnlocked = presenter.unlockSession(testPin)

        verify(preferenceProvider, times(1)).setValue(Preference.SESSION_LOCKED, true)
        assert(isUnlocked)
    }

    @Test
    fun `Should return false if pin is wrong`() {
        val testPin = "testPin"
        val wrongPin = "wrongPin"

        whenever(preferenceProvider.getString(Preference.PIN,"")) doReturn testPin
        val isUnlocked = presenter.unlockSession(wrongPin)

        assertFalse(isUnlocked)
    }

    @Test
    fun `Should save pin and block session`() {
        val testPin = "testPin"

        presenter.savePin(testPin)
        verify(preferenceProvider,times(1)).setValue(Preference.PIN, testPin)
        verify(preferenceProvider,times(1)).setValue(Preference.SESSION_LOCKED, true)
    }
}