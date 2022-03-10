package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import java.util.Date
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.dhis2.commons.data.EventCreationType
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepository
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.EventDetailResourcesProvider
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.event.EventEditableStatus.Editable
import org.hisp.dhis.android.core.event.EventEditableStatus.NonEditable
import org.hisp.dhis.android.core.event.EventNonEditableReason.BLOCKED_BY_COMPLETION
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigureEventDetailsTest {

    private val programStage: ProgramStage = mock()
    private val event: Event = mock()
    private val program: Program = mock()
    private val repository: EventDetailsRepository = mock {
        on { getProgramStage() } doReturn programStage
        on { hasAccessDataWrite() } doReturn true
        on { getProgram() } doReturn program
    }
    private val resourcesProvider: EventDetailResourcesProvider = mock {
        on { provideButtonNext() } doReturn NEXT
        on { provideButtonUpdate() } doReturn UPDATE
        on { provideButtonCheck() } doReturn CHECK
    }

    private lateinit var configureEventDetails: ConfigureEventDetails

    @Test
    fun `action button should be visible on new event`() = runBlocking {
        // Given user creates a new event
        configureEventDetails = ConfigureEventDetails(
            repository = repository,
            resourcesProvider = resourcesProvider,
            creationType = EventCreationType.ADDNEW,
            enrollmentStatus = EnrollmentStatus.ACTIVE
        )
        // And event creation should be completed
        val selectedDate = Date()
        val selectedOrgUnit = ORG_UNIT_UID
        val isCatComboCompleted = true

        // When button is checked
        val eventDetails = configureEventDetails.invoke(
            selectedDate = selectedDate,
            selectedOrgUnit = selectedOrgUnit,
            catOptionComboUid = null,
            isCatComboCompleted = isCatComboCompleted,
            coordinates = null,
            tempCreate = null
        ).first()

        // Then action button should be visible
        assertTrue(eventDetails.isActionButtonVisible)
        assert(eventDetails.actionButtonText.equals(NEXT))
    }

    @Test
    fun `action button should not be visible on new event`() = runBlocking {
        // Given user creates a new event
        configureEventDetails = ConfigureEventDetails(
            repository = repository,
            resourcesProvider = resourcesProvider,
            creationType = EventCreationType.ADDNEW,
            enrollmentStatus = EnrollmentStatus.ACTIVE
        )
        // And event creation should be uncompleted
        val selectedDate = null
        val selectedOrgUnit = ORG_UNIT_UID
        val isCatComboCompleted = true

        // When button is checked
        val eventDetails = configureEventDetails.invoke(
            selectedDate = selectedDate,
            selectedOrgUnit = selectedOrgUnit,
            catOptionComboUid = null,
            isCatComboCompleted = isCatComboCompleted,
            coordinates = null,
            tempCreate = null
        ).first()

        // Then action button should be invisible
        assertFalse(eventDetails.isActionButtonVisible)
    }

    @Test
    fun `action button should be visible on existing event`() = runBlocking {
        // Given user is in an existing event
        whenever(repository.getEvent()) doReturn event

        configureEventDetails = ConfigureEventDetails(
            repository = repository,
            resourcesProvider = resourcesProvider,
            creationType = EventCreationType.ADDNEW,
            enrollmentStatus = EnrollmentStatus.ACTIVE
        )
        // And event status is active
        whenever(event.status()) doReturn EventStatus.ACTIVE
        whenever(repository.getEditableStatus()) doReturn Editable()

        // When button is checked
        val eventDetails = configureEventDetails.invoke(
            selectedDate = null,
            selectedOrgUnit = null,
            catOptionComboUid = null,
            isCatComboCompleted = false,
            coordinates = null,
            tempCreate = null
        ).first()

        // Then action button should be invisible
        assertTrue(eventDetails.isActionButtonVisible)
        assert(eventDetails.actionButtonText.equals(UPDATE))
    }

    @Test
    fun `action button should hide button on a non editable existing event`() = runBlocking {
        // Given user is in an existing event
        whenever(repository.getEvent()) doReturn event

        configureEventDetails = ConfigureEventDetails(
            repository = repository,
            resourcesProvider = resourcesProvider,
            creationType = EventCreationType.ADDNEW,
            enrollmentStatus = EnrollmentStatus.ACTIVE
        )
        // And event is not editable
        whenever(repository.getEditableStatus()) doReturn NonEditable(BLOCKED_BY_COMPLETION)

        // When button is checked
        val eventDetails = configureEventDetails.invoke(
            selectedDate = null,
            selectedOrgUnit = null,
            catOptionComboUid = null,
            isCatComboCompleted = false,
            coordinates = null,
            tempCreate = null
        ).first()

        // Then action button should be invisible
        assertFalse(eventDetails.isActionButtonVisible)
    }

    @Test
    fun `reopen button should be visible if event status is complete and user has authorities`() =
        runBlocking {
            // Given user is in an existing event
            whenever(repository.getEvent()) doReturn event

            configureEventDetails = ConfigureEventDetails(
                repository = repository,
                resourcesProvider = resourcesProvider,
                creationType = EventCreationType.DEFAULT,
                enrollmentStatus = EnrollmentStatus.COMPLETED
            )

            // And user has authorities to reopen
            whenever(repository.getCanReopen()) doReturn true

            // When button is checked
            val eventDetails = configureEventDetails.invoke(
                selectedDate = null,
                selectedOrgUnit = null,
                catOptionComboUid = null,
                isCatComboCompleted = false,
                coordinates = null,
                tempCreate = null
            ).first()

            // Then reopen button should be visible
            assertTrue(eventDetails.canReopen)
        }

    @Test
    fun `reopen button not be visible if status is not complete or does not have authorities`() =
        runBlocking {
            // Given user is in an existing event
            whenever(repository.getEvent()) doReturn event

            configureEventDetails = ConfigureEventDetails(
                repository = repository,
                resourcesProvider = resourcesProvider,
                creationType = EventCreationType.DEFAULT,
                enrollmentStatus = EnrollmentStatus.ACTIVE
            )

            // And user has authorities to reopen
            whenever(repository.getCanReopen()) doReturn false

            // When button is checked
            val eventDetails = configureEventDetails.invoke(
                selectedDate = null,
                selectedOrgUnit = null,
                catOptionComboUid = null,
                isCatComboCompleted = false,
                coordinates = null,
                tempCreate = null
            ).first()

            // Then reopen button should be visible
            assertTrue(!eventDetails.canReopen)
        }

    companion object {
        const val ORG_UNIT_UID = "orgUnitUid"
        const val NEXT = "Next"
        const val UPDATE = "Update"
        const val CHECK = "Check"
    }
}
