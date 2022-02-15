package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.domain

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Observable
import org.dhis2.commons.data.EventCreationType
import org.dhis2.data.dhislogic.DhisPeriodUtils
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.providers.EventDetailResourcesProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialRepository
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigureEventReportDateUnitTest {

    private val resourcesProvider: EventDetailResourcesProvider = mock {
        on { provideDueDate() } doReturn DUE_DATE
        on { provideEventDate() } doReturn EVENT_DATE
    }

    private val programStage: ProgramStage = mock()

    private val eventInitialRepository: EventInitialRepository = mock {
        on { programStageWithId(PROGRAM_STAGE_ID) } doReturn Observable.just(programStage)
    }

    private val periodUtils: DhisPeriodUtils = mock()

    private lateinit var configureEventReportDate: ConfigureEventReportDate

    @Test
    fun shouldReturnStoredEventInfoWhenExistingEvent() {
        //Given Existing event
        configureEventReportDate = ConfigureEventReportDate(
            eventId = EVENT_ID,
            programStageId = PROGRAM_STAGE_ID,
            resourceProvider = resourcesProvider,
            eventInitialRepository = eventInitialRepository,
            periodUtils = periodUtils
        )

        //And has a concrete date
        val expectedDate = "14/2/2022"

        val event: Event = mock {
            on { eventDate() } doReturn DateUtils.uiDateFormat().parse(expectedDate)
        }
        whenever(eventInitialRepository.event(EVENT_ID)) doReturn Observable.just(event)

        //When reportDate is invoked
        configureEventReportDate.invoke().apply {
            //Then report date should be active
            assert(active)
            //Then stored date should be displayed
            assert(dateValue == expectedDate)
            //Then default label should be displayed
            assert(label == EVENT_DATE)
        }
    }

    @Test
    fun shouldReturnCurrentDayWhenNewEvent() {
        //Given the creation of new event
        configureEventReportDate = ConfigureEventReportDate(
            programStageId = PROGRAM_STAGE_ID,
            resourceProvider = resourcesProvider,
            eventInitialRepository = eventInitialRepository,
            periodUtils = periodUtils
        )
        //When reportDate is invoked
        configureEventReportDate.invoke().apply {

            //Then report date should be active
            assert(active)
            //Then reportDate should be the current day
            assert(dateValue == getCurrentDay())
            //Then default label should be displayed
            assert(label == EVENT_DATE)
        }
    }

    @Test
    fun shouldReturnTomorrowWhenNewDailyEvent() {
        //Given the creation of new event
        //And periodType is daily
        val periodType = PeriodType.Daily
        configureEventReportDate = ConfigureEventReportDate(
            programStageId = PROGRAM_STAGE_ID,
            resourceProvider = resourcesProvider,
            eventInitialRepository = eventInitialRepository,
            periodType = periodType,
            periodUtils = periodUtils,
        )

        val tomorrow = "16/2/2022"

        whenever(
            periodUtils.getPeriodUIString(any(), any(), any())
        ) doReturn tomorrow

        //When reportDate is invoked
        configureEventReportDate.invoke().apply {
            //Then date should be tomorrow
            assert(dateValue == tomorrow)
        }
    }

    @Test
    fun getNextPeriodWhenCreatingScheduledEvent() {
        //Given the creation of new scheduled event
        configureEventReportDate = ConfigureEventReportDate(
            programStageId = PROGRAM_STAGE_ID,
            creationType = EventCreationType.SCHEDULE,
            resourceProvider = resourcesProvider,
            eventInitialRepository = eventInitialRepository,
            periodUtils = periodUtils,
            enrollmentId = ENROLLMENT_ID
        )

        val lastEventDate = "13/2/2022"
        val nextEventDate = "19/2/2022"
        whenever(
            eventInitialRepository.getStageLastDate(PROGRAM_STAGE_ID, ENROLLMENT_ID)
        ) doReturn DateUtils.uiDateFormat().parse(lastEventDate)
        whenever(
            eventInitialRepository.getMinDaysFromStartByProgramStage(PROGRAM_STAGE_ID)
        ) doReturn 6

        //When reportDate is invoked
        configureEventReportDate.invoke().apply {
            //Then date should be next period
            assert(dateValue == nextEventDate)
        }
    }

    @Test
    fun shouldHideFieldWhenScheduled() {
        //Given an scheduled event
        configureEventReportDate = ConfigureEventReportDate(
            programStageId = PROGRAM_STAGE_ID,
            creationType = EventCreationType.SCHEDULE,
            resourceProvider = resourcesProvider,
            eventInitialRepository = eventInitialRepository,
            periodUtils = periodUtils,
            enrollmentId = ENROLLMENT_ID
        )

        //And with hidden due date
        whenever(programStage.hideDueDate()) doReturn true
        whenever(programStage.dueDateLabel()) doReturn DUE_DATE

        whenever(
            eventInitialRepository.getStageLastDate(PROGRAM_STAGE_ID, ENROLLMENT_ID)
        ) doReturn DateUtils.getInstance().today
        whenever(
            eventInitialRepository.getMinDaysFromStartByProgramStage(PROGRAM_STAGE_ID)
        ) doReturn 6

        whenever(
            eventInitialRepository.programStageWithId(PROGRAM_STAGE_ID)
        ) doReturn Observable.just(programStage)

        //When report date is invoked
        configureEventReportDate.invoke().apply {
            //Then report date should be hidden
            assertFalse(active)
            assert(label == DUE_DATE)
        }
    }

    @Test
    fun `should allow future dates when event type is scheduled`() {
        //Given an schedule event
        configureEventReportDate = ConfigureEventReportDate(
            programStageId = PROGRAM_STAGE_ID,
            creationType = EventCreationType.SCHEDULE,
            resourceProvider = resourcesProvider,
            eventInitialRepository = eventInitialRepository,
            periodUtils = periodUtils,
            enrollmentId = ENROLLMENT_ID
        )

        whenever(
            eventInitialRepository.getStageLastDate(PROGRAM_STAGE_ID, ENROLLMENT_ID)
        ) doReturn DateUtils.getInstance().today
        whenever(
            eventInitialRepository.getMinDaysFromStartByProgramStage(PROGRAM_STAGE_ID)
        ) doReturn 6

        //When report date is invoked
        configureEventReportDate.invoke().apply {
            //Then future dates should be allowed
            assertTrue(allowFutureDates)
        }
    }

    private fun getCurrentDay() = DateUtils.getInstance().formatDate(DateUtils.getInstance().today)

    companion object {
        const val PROGRAM_STAGE_ID = "programStageId"
        const val ENROLLMENT_ID = "enrollmentId"
        const val EVENT_ID = "eventId"
        const val DUE_DATE = "Due date"
        const val EVENT_DATE = "Event date"
    }
}
