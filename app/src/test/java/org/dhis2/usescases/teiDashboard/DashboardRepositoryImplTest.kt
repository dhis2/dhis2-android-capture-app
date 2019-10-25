package org.dhis2.usescases.teiDashboard

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import com.squareup.sqlbrite2.BriteDatabase
import io.reactivex.Single
import org.dhis2.utils.CodeGenerator
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Before
import org.junit.Test


class DashboardRepositoryImplTest {

    private lateinit var repository: DashboardRepositoryImpl
    private val codeGenerator: CodeGenerator = mock()
    private val britedatabase: BriteDatabase = mock()
    private val d2: D2 = mock()

    @Before
    fun setUp() {
        repository = DashboardRepositoryImpl(codeGenerator, britedatabase, d2)
    }

    @Test
    fun `Should return program stage to show display generate event`() {

        whenever(d2.eventModule()) doReturn mock()
        whenever(d2.eventModule().events()) doReturn mock()
        whenever(d2.eventModule().events().uid("event_uid")) doReturn mock()
        whenever(d2.eventModule().events().uid("event_uid").get()) doReturn Single.just(getMockSingleEvent())

        whenever(d2.programModule()) doReturn mock()
        whenever(d2.programModule().programStages()) doReturn mock()
        whenever(d2.programModule().programStages().uid("program_stage")) doReturn mock()
        whenever(d2.programModule().programStages().uid("program_stage").get()) doReturn Single.just(getMockStage())

        val testObserver = repository.displayGenerateEvent("event_uid").test()

        testObserver.assertValue(getMockStage())

        testObserver.dispose()
    }

    private fun getMockSingleEvent(): Event {
        return Event.builder()
                .uid("event_uid")
                .programStage("program_stage")
                .program("program")
                .build()
    }

    private fun getMockStage(): ProgramStage {
        return ProgramStage.builder()
                .uid("program_stage")
                .build()

    }

}