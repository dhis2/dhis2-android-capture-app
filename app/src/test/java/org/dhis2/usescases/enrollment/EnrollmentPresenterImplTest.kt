package org.dhis2.usescases.enrollment

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.dhis2.data.forms.dataentry.DataEntryRepository
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.schedulers.TrampolineSchedulerProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceObjectRepository
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class EnrollmentPresenterImplTest {

    private val formRepository: EnrollmentFormRepository = mock()
    private val programRepository: ReadOnlyOneObjectRepositoryFinalImpl<Program> = mock()
    private val teiRepository: TrackedEntityInstanceObjectRepository = mock()
    private val dataEntryRepository: DataEntryRepository = mock()
    lateinit var presenter: EnrollmentPresenterImpl
    private val enrollmentView: EnrollmentView = mock()
    private val d2: D2 = mock()
    private val enrollmentRepository: EnrollmentObjectRepository = mock()
    private val schedulers: SchedulerProvider = TrampolineSchedulerProvider()

    @Before
    fun setUp() {
        presenter = EnrollmentPresenterImpl(
            enrollmentView,
            d2,
            enrollmentRepository,
            dataEntryRepository,
            teiRepository,
            programRepository,
            schedulers,
            formRepository
        )
    }

    @Test
    fun `Missing and errors fields should show mandatory fields dialog`() {
        val checkWthErrors = presenter.dataIntegrityCheck(mandatoryOk = false, hasError = true)

        Assert.assertFalse(checkWthErrors)

        verify(enrollmentView, times(1)).showMissingMandatoryFieldsMessage()
    }

    @Test
    fun `Missing fields should show mandatory fields dialog`() {
        val checkWthErrors = presenter.dataIntegrityCheck(mandatoryOk = false, hasError = false)

        Assert.assertFalse(checkWthErrors)

        verify(enrollmentView, times(1)).showMissingMandatoryFieldsMessage()
    }

    @Test
    fun `Error fields should show mandatory fields dialog`() {
        val checkWthErrors = presenter.dataIntegrityCheck(mandatoryOk = true, hasError = true)

        Assert.assertFalse(checkWthErrors)

        verify(enrollmentView, times(1)).showErrorFieldsMessage()
    }
}
