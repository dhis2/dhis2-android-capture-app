package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.date.DateUtils
import org.dhis2.data.dhislogic.DhisPeriodUtils
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.data.EventDetailsRepository
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.EventDetailResourcesProvider
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigureEventReportDateTest {

    private val resourcesProvider: EventDetailResourcesProvider = mock {
        on { provideDueDate() } doReturn DUE_DATE
        on { provideEventDate() } doReturn EVENT_DATE
    }

    private val programStage: ProgramStage = mock()

    private val repository: EventDetailsRepository = mock {
        on { getProgramStage() } doReturn programStage
    }

    private val periodUtils: DhisPeriodUtils = mock()

    private lateinit var configureEventReportDate: ConfigureEventReportDate

    @Test
    fun `Should return stored event info when existing event`() = runBlocking {
        // Given Existing event
        configureEventReportDate = ConfigureEventReportDate(
            resourceProvider = resourcesProvider,
            repository = repository,
            periodUtils = periodUtils
        )

        // And has a concrete date
        val expectedDate = "14/2/2022"

        val event: Event = mock {
            on { eventDate() } doReturn DateUtils.uiDateFormat().parse(expectedDate)
        }
        whenever(repository.getEvent()) doReturn event

        // When reportDate is invoked
        val eventDate = configureEventReportDate.invoke().first()

        // Then report date should be active
        assert(eventDate.active)
        // Then stored date should be displayed
        assert(eventDate.dateValue == expectedDate)
        // Then default label should be displayed
        assert(eventDate.label == EVENT_DATE)
    }

    @Test
    fun `Should return current day when new event`() = runBlocking {
        // Given the creation of new event
        configureEventReportDate = ConfigureEventReportDate(
            resourceProvider = resourcesProvider,
            repository = repository,
            periodUtils = periodUtils
        )
        val currentDay =
            DateUtils.uiDateFormat().format(DateUtils.getInstance().today)

        // When reportDate is invoked
        val eventDate = configureEventReportDate.invoke().first()

        // Then report date should be active
        assert(eventDate.active)
        // Then reportDate should be the current day
        assert(eventDate.dateValue == currentDay)
        // Then default label should be displayed
        assert(eventDate.label == EVENT_DATE)
    }

    @Test
    fun `Should return tomorrow when new daily event`() = runBlocking {
        // Given the creation of new event
        // And periodType is daily
        val periodType = PeriodType.Daily
        configureEventReportDate = ConfigureEventReportDate(
            resourceProvider = resourcesProvider,
            repository = repository,
            periodType = periodType,
            periodUtils = periodUtils
        )

        val tomorrow = "16/2/2022"

        whenever(
            periodUtils.getPeriodUIString(any(), any(), any())
        ) doReturn tomorrow

        // When reportDate is invoked
        val eventDate = configureEventReportDate.invoke().first()

        // Then date should be tomorrow
        assert(eventDate.dateValue == tomorrow)
    }

    @Test
    fun `Get next period when creating scheduled event`() = runBlocking {
        // Given the creation of new scheduled event
        configureEventReportDate = ConfigureEventReportDate(
            creationType = EventCreationType.SCHEDULE,
            resourceProvider = resourcesProvider,
            repository = repository,
            periodUtils = periodUtils,
            enrollmentId = ENROLLMENT_ID
        )

        val lastEventDate = "13/2/2022"
        val nextEventDate = "19/2/2022"
        whenever(
            repository.getStageLastDate(ENROLLMENT_ID)
        ) doReturn DateUtils.uiDateFormat().parse(lastEventDate)
        whenever(
            repository.getMinDaysFromStartByProgramStage()
        ) doReturn 6

        // When reportDate is invoked
        val eventDate = configureEventReportDate.invoke().first()

        // Then date should be next period
        assert(eventDate.dateValue == nextEventDate)
    }

    @Test
    fun `Should hide field when scheduled`() = runBlocking {
        // Given an scheduled event
        configureEventReportDate = ConfigureEventReportDate(
            creationType = EventCreationType.SCHEDULE,
            resourceProvider = resourcesProvider,
            repository = repository,
            periodUtils = periodUtils,
            enrollmentId = ENROLLMENT_ID
        )

        // And with hidden due date
        whenever(programStage.hideDueDate()) doReturn true
        whenever(programStage.dueDateLabel()) doReturn DUE_DATE
        whenever(repository.getStageLastDate(ENROLLMENT_ID)) doReturn DateUtils.getInstance().today
        whenever(repository.getMinDaysFromStartByProgramStage()) doReturn 6
        whenever(repository.getProgramStage()) doReturn programStage

        // When report date is invoked
        val eventDate = configureEventReportDate.invoke().first()

        // Then report date should be hidden
        assertFalse(eventDate.active)
        assert(eventDate.label == DUE_DATE)
    }

    @Test
    fun `Should allow future dates when event type is scheduled`() = runBlocking {
        // Given an schedule event
        configureEventReportDate = ConfigureEventReportDate(
            creationType = EventCreationType.SCHEDULE,
            resourceProvider = resourcesProvider,
            repository = repository,
            periodUtils = periodUtils,
            enrollmentId = ENROLLMENT_ID
        )

        whenever(repository.getStageLastDate(ENROLLMENT_ID)) doReturn DateUtils.getInstance().today
        whenever(repository.getMinDaysFromStartByProgramStage()) doReturn 6

        // When report date is invoked
        val eventDate = configureEventReportDate.invoke().first()
        // Then future dates should be allowed
        assertTrue(eventDate.allowFutureDates)
    }

    companion object {
        const val PROGRAM_STAGE_ID = "programStageId"
        const val ENROLLMENT_ID = "enrollmentId"
        const val EVENT_ID = "eventId"
        const val DUE_DATE = "Due date"
        const val EVENT_DATE = "Event date"
    }
}
