package org.dhis2.data.dhislogic

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.event.Event
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

class DhisEnrollmentUtilsTest {

    private lateinit var dhisEnrollmentUtils: DhisEnrollmentUtils
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    @Before
    fun setUp() {
        dhisEnrollmentUtils = DhisEnrollmentUtils(d2)
    }

    @Test
    fun `Should return enrollmentOpen if event has no enrollment`() {
        val result = dhisEnrollmentUtils.isEventEnrollmentOpen(
            Event.builder()
                .uid("eventUid")
                .build(),
        )
        assertTrue(result)
    }

    @Test
    fun `Should return false if enrollment is not active`() {
        whenever(
            d2.enrollmentModule().enrollments()
                .uid(anyString())
                .blockingGet(),
        ) doReturn Enrollment.builder()
            .uid("enrollmentUid")
            .status(EnrollmentStatus.CANCELLED)
            .build()
        val result = dhisEnrollmentUtils.isEventEnrollmentOpen(
            Event.builder()
                .uid("eventUid")
                .enrollment("enrollmentUid")
                .build(),
        )
        assertFalse(result)
    }

    @Test
    fun `Should return true if enrollment is not found`() {
        whenever(
            d2.enrollmentModule().enrollments()
                .uid(anyString())
                .blockingGet(),
        ) doReturn null
        val result = dhisEnrollmentUtils.isEventEnrollmentOpen(
            Event.builder()
                .uid("eventUid")
                .enrollment("enrollmentUid")
                .build(),
        )
        assertTrue(result)
    }

    @Test
    fun `Should return true if enrollment is active`() {
        whenever(
            d2.enrollmentModule().enrollments()
                .uid("enrollmentUid")
                .blockingGet(),
        ) doReturn Enrollment.builder()
            .uid("enrollmentUid")
            .status(EnrollmentStatus.ACTIVE)
            .build()
        val result = dhisEnrollmentUtils.isEventEnrollmentOpen(
            Event.builder()
                .uid("eventUid")
                .enrollment("enrollmentUid")
                .build(),
        )
        assertTrue(result)
    }
}
