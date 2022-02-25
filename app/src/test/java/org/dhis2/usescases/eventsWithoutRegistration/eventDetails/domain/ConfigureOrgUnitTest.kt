package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.prefs.Preference.Companion.CURRENT_ORG_UNIT
import org.dhis2.commons.prefs.PreferenceProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialRepository
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.junit.Before
import org.junit.Test
import java.util.Date

class ConfigureOrgUnitTest {

    private val eventInitialRepository: EventInitialRepository = mock()
    private val preferenceProvider: PreferenceProvider = mock()
    private val storedOrgUnit: OrganisationUnit = mock {
        on { uid() } doReturn STORED_ORG_UNIT_UID
    }

    private lateinit var configureOrgUnit: ConfigureOrgUnit

    @Before
    fun setUp() {
        whenever(
            eventInitialRepository.accessDataWrite(PROGRAM_UID)
        ) doReturn Observable.just(true)
        whenever(
            eventInitialRepository.orgUnits(PROGRAM_UID)
        ) doReturn Observable.just(listOf(storedOrgUnit))
    }

    @Test
    fun `Should not initialize orgUnit when there is not on the filtered list`() = runBlocking {
        //Given user is creating a new event
        configureOrgUnit = ConfigureOrgUnit(
            creationType = EventCreationType.ADDNEW,
            eventInitialRepository = eventInitialRepository,
            preferencesProvider = preferenceProvider,
            programUid = PROGRAM_UID,
            eventUid = null,
            initialOrgUnitUid = null
        )
        //And there is date selected
        val selectedDate = Date()
        val dateString = DateUtils.databaseDateFormat().format(selectedDate)

        whenever(
            preferenceProvider.getString(CURRENT_ORG_UNIT)
        ) doReturn STORED_ORG_UNIT_UID
        //And the stored org unit is in the filtered list
        whenever(
            eventInitialRepository.filteredOrgUnits(
                dateString,
                PROGRAM_UID,
                null
            )
        ) doReturn Observable.just(listOf())

        //When org unit is initialized
        val selectedOrgUnit = configureOrgUnit.invoke(selectedDate).first()

        //Then org unit should initialize with the stored
        assert(selectedOrgUnit.selectedOrgUnit == null)
    }

    @Test
    fun `Should initialize orgUnit when there is a stored orgUnit`() = runBlocking {
        //Given user is creating a new event
        configureOrgUnit = ConfigureOrgUnit(
            creationType = EventCreationType.ADDNEW,
            eventInitialRepository = eventInitialRepository,
            preferencesProvider = preferenceProvider,
            programUid = PROGRAM_UID,
            eventUid = null,
            initialOrgUnitUid = null
        )
        //And there is date selected
        val selectedDate = Date()
        val dateString = DateUtils.databaseDateFormat().format(selectedDate)
        //And there is a stored org unit
        whenever(
            preferenceProvider.contains(CURRENT_ORG_UNIT)
        ) doReturn true
        whenever(
            preferenceProvider.getString(CURRENT_ORG_UNIT)
        ) doReturn STORED_ORG_UNIT_UID
        //And the stored org unit is in the filtered list
        whenever(
            eventInitialRepository.filteredOrgUnits(
                dateString,
                PROGRAM_UID,
                null
            )
        ) doReturn Observable.just(
            listOf(
                storedOrgUnit,
                OrganisationUnit.builder()
                    .uid("orgUnitUid2")
                    .displayName("orgUnitUid2")
                    .build()
            )
        )

        //When org unit is initialized
        val selectedOrgUnit = configureOrgUnit.invoke(selectedDate).first()

        //Then org unit should initialize with the stored
        assert(selectedOrgUnit.selectedOrgUnit?.equals(storedOrgUnit) == true)
    }

    companion object {
        const val PROGRAM_UID = "programUid"
        const val STORED_ORG_UNIT_UID = "orgUnitUid"
    }
}