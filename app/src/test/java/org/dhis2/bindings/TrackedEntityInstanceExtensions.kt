package org.dhis2.bindings

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertTrue
import org.dhis2.Bindings.filterDeletedEnrollment
import org.hisp.dhis.android.core.D2
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
}
