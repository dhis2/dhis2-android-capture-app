package org.dhis2.usescases.notes

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import java.util.UUID
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.note.Note
import org.hisp.dhis.android.core.program.Program
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class NotesRepositoryTest {

    private lateinit var repository: NotesRepository
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val programUid = UUID.randomUUID().toString()

    @Before
    fun setUp() {
        repository = NotesRepository(d2, programUid)
    }

    @Test
    fun `Should return notes for TEI enrollment`() {
        val notes = listOf(dummyNote(), dummyNote())
        val teiUid = UUID.randomUUID().toString()
        val enrollmentUid = UUID.randomUUID().toString()

        mockEnrollment(teiUid, enrollmentUid)

        whenever(
            d2.noteModule().notes()
                .byEnrollmentUid().eq(enrollmentUid)
        ) doReturn mock()
        whenever(
            d2.noteModule().notes()
                .byEnrollmentUid().eq(enrollmentUid).get()
        ) doReturn Single.just(notes)

        val testObserver = repository.getEnrollmentNotes(teiUid).test()

        testObserver.assertNoErrors()
        testObserver.assertValueCount(1)
        testObserver.assertValue(notes)

        testObserver.dispose()
    }

    @Test
    fun `Should return notes for event`() {
        // TODO: Implement test for getEventNotes(eventUid)
    }

    @Test
    fun `Should check program write permission`() {
        val dummyProgram = Program.builder()
            .uid(UUID.randomUUID().toString())
            .access(
                Access.builder().data(
                    DataAccess.builder()
                        .read(true)
                        .write(true)
                        .build()
                ).build()
            ).build()
        whenever(
            d2.programModule().programs()
                .uid(programUid)
                .blockingGet()
        ) doReturn dummyProgram

        assert(repository.hasProgramWritePermission())
    }

    private fun dummyNote(): Note =
        Note.builder()
            .uid(UUID.randomUUID().toString())
            .value("Note")
            .build()

    private fun mockEnrollment(teiUid: String, enrollmentUid: String) {
        whenever(
            d2.enrollmentModule().enrollments()
                .byProgram().eq(programUid)
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byProgram().eq(programUid)
                .byTrackedEntityInstance()
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byProgram().eq(programUid)
                .byTrackedEntityInstance().eq(teiUid)
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byProgram().eq(programUid)
                .byTrackedEntityInstance().eq(teiUid)
                .one()
        ) doReturn mock()
        whenever(
            d2.enrollmentModule().enrollments()
                .byProgram().eq(programUid)
                .byTrackedEntityInstance().eq(teiUid)
                .one().blockingGet()
        ) doReturn Enrollment.builder().uid(enrollmentUid).build()
    }
}
