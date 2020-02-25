package org.dhis2.usescases.notes.noteDetail

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Single
import org.dhis2.usescases.notes.NoteType
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.note.Note
import org.hisp.dhis.android.core.note.NoteCreateProjection
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.RETURNS_DEEP_STUBS

class NoteDetailRepositoryTest {

    private lateinit var repository: NoteDetailRepository

    private val d2: D2 = Mockito.mock(D2::class.java, RETURNS_DEEP_STUBS)
    private val programUid = "programUid"

    @Before
    fun setUp() {
        repository = NoteDetailRepositoryImpl(d2, programUid)
    }

    @Test
    fun `Should return a note`() {
        val note = dummyNote()

        whenever(d2.noteModule().notes().uid(any())) doReturn mock()
        whenever(d2.noteModule().notes().uid(any()).get()) doReturn Single.just(note)

        val testObserver = repository.getNote("noteId").test()

        testObserver.assertValueCount(1)
        testObserver.assertValueAt(0) {
            it.value().equals("Note value")
        }
    }

    @Test
    fun `Should save Enrollment note`() {
        val note = dummyNote()
        val enrollmentUid = "EnrollmentUid"

        val noteType = NoteType.ENROLLMENT
        val message = "Note message"
        val teiUid = "uid"

        mockEnrollment(teiUid)

        whenever(
            d2.noteModule().notes().add(
                NoteCreateProjection.builder().enrollment(enrollmentUid).value(message).build()
            )
        ) doReturn Single.just(note.uid())

        val testObserver = repository.saveNote(noteType, teiUid, message).test()

        testObserver.assertValueCount(1)
        testObserver.assertValueAt(0) {
            it == "noteId"
        }
    }

    @Test
    fun `Should save an Event note`() {
        val eventUid = "uid"
        val message = "Note message"

        val testObserver = repository.saveNote(NoteType.EVENT, eventUid, message).test()

        testObserver.assertValueAt(0) {
            it == ""
        }
    }

    private fun dummyNote() =
        Note.builder()
            .uid("noteId")
            .value("Note value")
            .build()

    private fun mockEnrollment(teiUid: String) {
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
        ) doReturn Enrollment.builder().uid("EnrollmentUid").build()
    }
}
