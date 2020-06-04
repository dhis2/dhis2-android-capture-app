package org.dhis2.usescases.eventsWithoutRegistration.eventInitial

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.dhis2.data.prefs.Preference
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventSummary.EventSummaryRepository
import org.dhis2.utils.analytics.AnalyticsHelper
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EventInitialPresenterTest {
    lateinit var presenter: EventInitialPresenter
    val view: EventInitialContract.View = mock()
    private val eventRepository: EventSummaryRepository = mock()
    private val eventInitialRepository: EventInitialRepository = mock()
    val schedulers: SchedulerProvider = TrampolineSchedulerProvider()
    val preferences: PreferenceProvider = mock()
    val analyticsHelper: AnalyticsHelper = mock()

    @Before
    fun setUp() {
        presenter = EventInitialPresenter(
            view,
            eventRepository,
            eventInitialRepository,
            schedulers,
            preferences,
            analyticsHelper
        )
    }

    @Test
    fun `Should init previous org unit if exist and no one is selected`() {
        whenever(preferences.contains(Preference.CURRENT_ORG_UNIT)) doReturn true
        whenever(preferences.getString(Preference.CURRENT_ORG_UNIT, null)) doReturn "prevOrgUnit"
        assertTrue(presenter.getCurrentOrgUnit(null) == "prevOrgUnit")
    }

    @Test
    fun `Should init previous org unit if exist and one is selected`() {
        whenever(preferences.contains(Preference.CURRENT_ORG_UNIT)) doReturn true
        whenever(preferences.getString(Preference.CURRENT_ORG_UNIT, null)) doReturn "prevOrgUnit"
        assertTrue(presenter.getCurrentOrgUnit("orgUnit") == "prevOrgUnit")
    }

    @Test
    fun `Should init given org unit if previous does not exist`() {
        whenever(preferences.contains(Preference.CURRENT_ORG_UNIT)) doReturn false
        assertTrue(presenter.getCurrentOrgUnit("orgUnit") == "orgUnit")
    }

    @Test
    fun `Should init null org unit if previous does not exist`() {
        whenever(preferences.contains(Preference.CURRENT_ORG_UNIT)) doReturn false
        assertTrue(presenter.getCurrentOrgUnit(null).isNullOrEmpty())
    }
}
