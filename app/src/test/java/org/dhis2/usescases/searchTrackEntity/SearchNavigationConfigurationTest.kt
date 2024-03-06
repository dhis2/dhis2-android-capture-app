package org.dhis2.usescases.searchTrackEntity

import java.util.GregorianCalendar
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SearchNavigationConfigurationTest {

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val configuration = SearchNavigationConfiguration(d2)

    private val defaultDate = GregorianCalendar(2019, 0, 11).time
    private val newDate = GregorianCalendar(2019, 0, 12).time

    @Test
    fun shouldRefreshDataIfTeiChanged() {
        mockTeiChanged("teiUid")
        configuration.openingTEI("teiUid")
        val result = configuration.refreshDataOnBackFromDashboard()
        assertTrue(result)
    }

    @Test
    fun shouldRefreshDataIfTeiDeleted() {
        mockTeiDeleted("teiUid")
        configuration.openingTEI("teiUid")
        val result = configuration.refreshDataOnBackFromDashboard()
        assertTrue(result)
    }

    @Test
    fun shouldNotRefreshDataIfTeiDeleted() {
        mockTeiNotChanged("teiUid")
        configuration.openingTEI("teiUid")
        val result = configuration.refreshDataOnBackFromDashboard()
        assertFalse(result)
    }

    @Test
    fun shouldRefreshDataIfEnrollmentWasCreated() {
        mockEnrollmentCreated("enrollmentUid")
        configuration.openingEnrollmentForm("enrollmentUid")
        val result = configuration.refreshDataOnBackFromEnrollment()
        assertTrue(result)
    }

    @Test
    fun shouldNotRefreshDataIfEnrollmentWasNotCreated() {
        mockEnrollmentNotCreated("enrollmentUid")
        configuration.openingEnrollmentForm("enrollmentUid")
        val result = configuration.refreshDataOnBackFromEnrollment()
        assertTrue(result)
    }

    @Test
    fun shouldNotRefreshDataIfEnrollmentDoesNotExist() {
        mockEnrollmentDoesNotExit("enrollmentUid")
        configuration.openingEnrollmentForm("enrollmentUid")
        val result = configuration.refreshDataOnBackFromEnrollment()
        assertFalse(result)
    }

    private fun mockTeiChanged(teiUId: String) {
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .uid(teiUId)
                .blockingGet()
        ) doReturnConsecutively listOf(
            defaultTei,
            teiWithChanges
        )
    }

    private fun mockTeiDeleted(teiUId: String) {
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .uid(teiUId)
                .blockingGet()
        ) doReturnConsecutively listOf(
            defaultTei,
            null
        )
    }

    private fun mockTeiNotChanged(teiUId: String) {
        whenever(
            d2.trackedEntityModule().trackedEntityInstances()
                .uid(teiUId)
                .blockingGet()
        ) doReturnConsecutively listOf(
            defaultTei,
            defaultTei
        )
    }

    private fun mockEnrollmentCreated(enrollmentUid: String) {
        whenever(
            d2.enrollmentModule().enrollments()
                .uid(enrollmentUid)
                .blockingGet()
        )doReturnConsecutively listOf(
            defaultEnrollment,
            defaultEnrollment
        )
    }

    private fun mockEnrollmentNotCreated(enrollmentUid: String) {
        whenever(
            d2.enrollmentModule().enrollments()
                .uid(enrollmentUid)
                .blockingGet()
        )doReturnConsecutively listOf(
            defaultEnrollment,
            null
        )
    }

    private fun mockEnrollmentDoesNotExit(enrollmentUid: String) {
        whenever(
            d2.enrollmentModule().enrollments()
                .uid(enrollmentUid)
                .blockingGet()
        )doReturn null
    }

    private val defaultTei: TrackedEntityInstance = mock {
        on { uid() } doReturn "teiUid"
        on { lastUpdated() } doReturn defaultDate
    }

    private val teiWithChanges: TrackedEntityInstance = mock {
        on { uid() } doReturn "teiUid"
        on { lastUpdated() } doReturn newDate
    }

    private val defaultEnrollment: Enrollment = mock {
        on { uid() } doReturn "enrollmentUId"
    }
}
