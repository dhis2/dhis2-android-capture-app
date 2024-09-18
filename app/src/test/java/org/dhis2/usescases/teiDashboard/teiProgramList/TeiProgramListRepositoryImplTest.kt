package org.dhis2.usescases.teiDashboard.teiProgramList

import io.reactivex.Single
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.usescases.main.program.ProgramViewModelMapper
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentCreateProjection
import org.hisp.dhis.android.core.program.Program
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

class TeiProgramListRepositoryImplTest {

    private lateinit var teiProgramRepository: TeiProgramListRepository
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val programViewModelMapper: ProgramViewModelMapper = mock()
    private val metadataIconProvider: MetadataIconProvider = mock()

    @Before
    fun setUp() {
        teiProgramRepository =
            TeiProgramListRepositoryImpl(d2, programViewModelMapper, metadataIconProvider)
    }

    @Test
    fun `Should set incident date if program needs it`() {
        val testEnrollment = EnrollmentCreateProjection.builder()
            .organisationUnit("orgUnitUid")
            .program("programUid")
            .trackedEntityInstance("teiUid")
            .build()

        whenever(
            d2.enrollmentModule().enrollments().add(
                testEnrollment,
            ),
        ) doReturn Single.just("enrollmentUid")

        whenever(
            d2.enrollmentModule().enrollments().uid("enrollmentUid"),
        ) doReturn mock()

        whenever(
            d2.programModule().programs().uid("programUid").blockingGet(),
        ) doReturn Program.builder()
            .uid("programUid")
            .displayIncidentDate(true)
            .build()

        whenever(
            d2.enrollmentModule().enrollments().uid("enrollmentUid").blockingGet(),
        ) doReturn Enrollment.builder()
            .uid("enrollmentUid")
            .build()

        val testObservable = teiProgramRepository.saveToEnroll(
            "orgUnitUid",
            "programUid",
            "teiUid",
            Date(),
        ).test()

        testObservable
            .assertNoErrors()
            .assertValueCount(1)
            .assertValue { it == "enrollmentUid" }

        verify(d2.enrollmentModule().enrollments().uid("enrollmentUid"), times(1)).setIncidentDate(
            DateUtils.getInstance().today,
        )
    }

    @Test
    fun `Should not set incident date if program doesn't need it`() {
        val testEnrollment = EnrollmentCreateProjection.builder()
            .organisationUnit("orgUnitUid")
            .program("programUid")
            .trackedEntityInstance("teiUid")
            .build()

        whenever(
            d2.enrollmentModule().enrollments().add(
                testEnrollment,
            ),
        ) doReturn Single.just("enrollmentUid")

        whenever(
            d2.enrollmentModule().enrollments().uid("enrollmentUid"),
        ) doReturn mock()

        whenever(
            d2.programModule().programs().uid("programUid").blockingGet(),
        ) doReturn Program.builder()
            .uid("programUid")
            .displayIncidentDate(false)
            .build()

        whenever(
            d2.enrollmentModule().enrollments().uid("enrollmentUid").blockingGet(),
        ) doReturn Enrollment.builder()
            .uid("enrollmentUid")
            .build()

        val testObservable = teiProgramRepository.saveToEnroll(
            "orgUnitUid",
            "programUid",
            "teiUid",
            Date(),
        ).test()

        testObservable
            .assertNoErrors()
            .assertValueCount(1)
            .assertValue { it == "enrollmentUid" }

        verify(d2.enrollmentModule().enrollments().uid("enrollmentUid"), times(0)).setIncidentDate(
            DateUtils.getInstance().today,
        )
    }
}
