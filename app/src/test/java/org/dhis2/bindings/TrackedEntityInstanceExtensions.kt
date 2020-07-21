package org.dhis2.bindings

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doReturnConsecutively
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertTrue
import org.dhis2.Bindings.filterDeletedEnrollment
import org.dhis2.Bindings.filterEvents
import org.dhis2.Bindings.toDate
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.period.DatePeriod
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.junit.Test
import org.mockito.Mockito

class TrackedEntityInstanceExtensions {

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    @Test
    fun `Should not filter teis if program is null`() {
        val testList = mutableListOf(
            TrackedEntityInstance.builder().uid("tei_A").build(),
            TrackedEntityInstance.builder().uid("tei_C").build(),
            TrackedEntityInstance.builder().uid("tei_D").build()
        )

        testList.filterDeletedEnrollment(d2, null)

        assertTrue(testList.size == 3)
    }

    @Test
    fun `Should filter tei with deleted enrollment`() {
        val testList = mutableListOf(
            TrackedEntityInstance.builder().uid("tei_A").build(),
            TrackedEntityInstance.builder().uid("tei_C").build(),
            TrackedEntityInstance.builder().uid("tei_D").build()
        )
        testList.forEachIndexed { index, tei ->
            handleEnrollmentCall(tei.uid(), "programUid", index == 0)
        }

        testList.filterDeletedEnrollment(d2, "programUid")

        assertTrue(testList.size == 2)
    }

    private fun handleEnrollmentCall(teiUid: String, programUid: String, returnValue: Boolean) {
        whenever(
            d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
                .byProgram()
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
                .byProgram().eq(programUid)
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
                .byProgram().eq(programUid)
                .byDeleted()
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
                .byProgram().eq(programUid)
                .byDeleted().isFalse
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
                .byProgram().eq(programUid)
                .byDeleted().isFalse
                .blockingIsEmpty()
        ) doReturn returnValue
    }

    @Test
    fun `Should filter TEIs by event period`() {
        val testList = mutableListOf(
            TrackedEntityInstance.builder().uid("tei_A").build(),
            TrackedEntityInstance.builder().uid("tei_C").build(),
            TrackedEntityInstance.builder().uid("tei_D").build()
        )

        val periods = arrayListOf(
            DatePeriod.builder()
                .startDate("2020-01-01".toDate())
                .endDate("2020-01-10".toDate())
                .build()
        )
        testList.forEachIndexed { index, tei ->
            handleEventCall(tei.uid(), periods, "programUid", index == 0)
        }

        testList.filterEvents(d2, periods, "programUid")

        assertTrue(testList.size == 2)
    }

    private fun handleEventCall(
        teiUid: String,
        periods: ArrayList<DatePeriod>,
        programUid: String,
        returnValue: Boolean
    ) {
        whenever(
            d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
                .byProgram()
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
                .byProgram().eq(programUid)
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byTrackedEntityInstance().eq(teiUid)
                .byProgram().eq(programUid)
                .blockingGet()
        ) doReturn arrayListOf(Enrollment.builder().uid("enrollmentUid").build())

        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().`in`(arrayListOf("enrollmentUid"))
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().`in`(arrayListOf("enrollmentUid"))
                .byStatus()
        ) doReturn mock()

        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().`in`(arrayListOf("enrollmentUid"))
                .byStatus().`in`(EventStatus.OVERDUE, EventStatus.SCHEDULE, EventStatus.SKIPPED)
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().`in`(arrayListOf("enrollmentUid"))
                .byStatus().`in`(EventStatus.OVERDUE, EventStatus.SCHEDULE, EventStatus.SKIPPED)
                .byDueDate()
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().`in`(arrayListOf("enrollmentUid"))
                .byStatus().`in`(EventStatus.OVERDUE, EventStatus.SCHEDULE, EventStatus.SKIPPED)
                .byDueDate().inDatePeriods(periods)
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().`in`(arrayListOf("enrollmentUid"))
                .byStatus().`in`(EventStatus.OVERDUE, EventStatus.SCHEDULE, EventStatus.SKIPPED)
                .byDueDate().inDatePeriods(periods)
                .blockingIsEmpty()
        ) doReturnConsecutively arrayListOf(true, false, false)

        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().`in`(arrayListOf("enrollmentUid"))
                .byStatus().`in`(EventStatus.ACTIVE, EventStatus.COMPLETED)
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().`in`(arrayListOf("enrollmentUid"))
                .byStatus().`in`(EventStatus.ACTIVE, EventStatus.COMPLETED)
                .byEventDate()
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().`in`(arrayListOf("enrollmentUid"))
                .byStatus().`in`(EventStatus.ACTIVE, EventStatus.COMPLETED)
                .byEventDate().inDatePeriods(periods)
        ) doReturn mock()
        whenever(
            d2.eventModule().events()
                .byEnrollmentUid().`in`(arrayListOf("enrollmentUid"))
                .byStatus().`in`(EventStatus.ACTIVE, EventStatus.COMPLETED)
                .byEventDate().inDatePeriods(periods)
                .blockingIsEmpty()
        ) doReturnConsecutively arrayListOf(true, false, false)
    }
}
