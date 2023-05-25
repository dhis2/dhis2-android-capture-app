package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import java.util.Date
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.prefs.Preference.Companion.CURRENT_ORG_UNIT
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepository
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class ConfigureOrgUnitTest {

    private val repository: EventDetailsRepository = mock()
    private val preferenceProvider: PreferenceProvider = mock()
    private val storedOrgUnit: OrganisationUnit = mock {
        on { uid() } doReturn STORED_ORG_UNIT_UID
    }

    private val storedOrgUnit2: OrganisationUnit = mock {
        on { uid() } doReturn STORED_ORG_UNIT_2_UID
    }

    private lateinit var configureOrgUnit: ConfigureOrgUnit

    @Before
    fun setUp() {
        whenever(repository.hasAccessDataWrite()) doReturn true
        whenever(repository.getOrganisationUnits()) doReturn listOf(storedOrgUnit, storedOrgUnit2)
    }

    @Test
    fun `Should not initialize orgUnit when there is not on the filtered list`() = runBlocking {
        // Given user is creating a new event
        configureOrgUnit = ConfigureOrgUnit(
            creationType = EventCreationType.ADDNEW,
            repository = repository,
            preferencesProvider = preferenceProvider,
            programUid = PROGRAM_UID,
            initialOrgUnitUid = null
        )
        // And there is date selected
        val selectedDate = Date()
        val dateString = DateUtils.databaseDateFormat().format(selectedDate)

        whenever(
            preferenceProvider.getString(CURRENT_ORG_UNIT)
        ) doReturn STORED_ORG_UNIT_UID
        // And the stored org unit is in the filtered list
        whenever(
            repository.getFilteredOrgUnits(
                dateString,
                null
            )
        ) doReturn listOf()

        // When org unit is initialized
        val selectedOrgUnit = configureOrgUnit.invoke(selectedDate).first()

        // Then org unit should initialize with the stored
        assert(selectedOrgUnit.selectedOrgUnit == null)
    }

    @Test
    fun `Should initialize orgUnit when there is only one`() = runBlocking {
        whenever(repository.getOrganisationUnits()) doReturn listOf(storedOrgUnit)

        // Given user is creating a new event
        configureOrgUnit = ConfigureOrgUnit(
            creationType = EventCreationType.ADDNEW,
            repository = repository,
            preferencesProvider = preferenceProvider,
            programUid = PROGRAM_UID,
            initialOrgUnitUid = null
        )
        // And there is date selected
        val selectedDate = Date()
        val dateString = DateUtils.databaseDateFormat().format(selectedDate)

        whenever(
            preferenceProvider.getString(CURRENT_ORG_UNIT)
        ) doReturn STORED_ORG_UNIT_UID
        // And the stored org unit is in the filtered list
        whenever(
            repository.getFilteredOrgUnits(
                dateString,
                null
            )
        ) doReturn listOf()

        // When org unit is initialized
        val selectedOrgUnit = configureOrgUnit.invoke(selectedDate).first()

        // Then org unit should initialize with the stored
        assert(selectedOrgUnit.selectedOrgUnit == storedOrgUnit)
    }

    @Test
    fun `Should initialize orgUnit when there is a stored orgUnit`() = runBlocking {
        // Given user is creating a new event
        configureOrgUnit = ConfigureOrgUnit(
            creationType = EventCreationType.ADDNEW,
            repository = repository,
            preferencesProvider = preferenceProvider,
            programUid = PROGRAM_UID,
            initialOrgUnitUid = null
        )
        // And there is date selected
        val selectedDate = Date()
        val dateString = DateUtils.databaseDateFormat().format(selectedDate)
        // And there is a stored org unit
        whenever(
            preferenceProvider.contains(CURRENT_ORG_UNIT)
        ) doReturn true
        whenever(
            preferenceProvider.getString(CURRENT_ORG_UNIT)
        ) doReturn STORED_ORG_UNIT_UID
        // And the stored org unit is in the filtered list
        whenever(
            repository.getFilteredOrgUnits(
                dateString,
                null
            )
        ) doReturn listOf(
            storedOrgUnit,
            OrganisationUnit.builder()
                .uid("orgUnitUid2")
                .displayName("orgUnitUid2")
                .build()
        )

        // When org unit is initialized
        val selectedOrgUnit = configureOrgUnit.invoke(selectedDate).first()

        // Then org unit should initialize with the stored
        assert(selectedOrgUnit.selectedOrgUnit?.equals(storedOrgUnit) == true)
    }

    companion object {
        const val PROGRAM_UID = "programUid"
        const val STORED_ORG_UNIT_UID = "orgUnitUid"
        const val STORED_ORG_UNIT_2_UID = "orgUnitUid2"
    }
}
