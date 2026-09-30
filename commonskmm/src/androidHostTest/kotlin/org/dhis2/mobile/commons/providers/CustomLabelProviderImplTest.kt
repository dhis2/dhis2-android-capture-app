package org.dhis2.mobile.commons.providers

import kotlinx.coroutines.test.runTest
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramStage
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

class CustomLabelProviderImplTest {
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)

    private val customLabelProvider = CustomLabelProviderImpl(d2)

    // region event label

    @Test
    fun `program context returns singular event label when quantity is null`() =
        runTest {
            mockProgram(eventLabel = "consultation", eventsLabel = "consultations")

            val result =
                customLabelProvider.getCustomEventLabel(
                    customLabelContext = CustomLabelContext.Program(PROGRAM_UID),
                    quantity = null,
                )

            assertEquals("Consultation", result)
        }

    @Test
    fun `program context returns singular event label when quantity is one`() =
        runTest {
            mockProgram(eventLabel = "consultation", eventsLabel = "consultations")

            val result =
                customLabelProvider.getCustomEventLabel(
                    customLabelContext = CustomLabelContext.Program(PROGRAM_UID),
                    quantity = 1,
                )

            assertEquals("Consultation", result)
        }

    @Test
    fun `program context returns plural event label when quantity is greater than one`() =
        runTest {
            mockProgram(eventLabel = "consultation", eventsLabel = "consultations")

            val result =
                customLabelProvider.getCustomEventLabel(
                    customLabelContext = CustomLabelContext.Program(PROGRAM_UID),
                    quantity = 2,
                )

            assertEquals("Consultations", result)
        }

    @Test
    fun `program stage context prefers stage event label over program event label`() =
        runTest {
            mockProgram(eventLabel = "consultation", eventsLabel = "consultations")
            mockProgramStage(eventLabel = "visit", eventsLabel = "visits")

            val result =
                customLabelProvider.getCustomEventLabel(
                    customLabelContext = CustomLabelContext.ProgramStage(STAGE_UID, PROGRAM_UID),
                    quantity = 1,
                )

            assertEquals("Visit", result)
        }

    @Test
    fun `program stage context returns plural stage event label when quantity is greater than one`() =
        runTest {
            mockProgram(eventLabel = "consultation", eventsLabel = "consultations")
            mockProgramStage(eventLabel = "visit", eventsLabel = "visits")

            val result =
                customLabelProvider.getCustomEventLabel(
                    customLabelContext = CustomLabelContext.ProgramStage(STAGE_UID, PROGRAM_UID),
                    quantity = 2,
                )

            assertEquals("Visits", result)
        }

    @Test
    fun `program stage context falls back to program event label when stage label is null`() =
        runTest {
            mockProgram(eventLabel = "consultation", eventsLabel = "consultations")
            mockProgramStage(eventLabel = null, eventsLabel = null)

            val result =
                customLabelProvider.getCustomEventLabel(
                    customLabelContext = CustomLabelContext.ProgramStage(STAGE_UID, PROGRAM_UID),
                    quantity = 1,
                )

            assertEquals("Consultation", result)
        }

    @Test
    fun `program stage context falls back to program plural label when stage plural label is null`() =
        runTest {
            mockProgram(eventLabel = "consultation", eventsLabel = "consultations")
            mockProgramStage(eventLabel = "visit", eventsLabel = null)

            val result =
                customLabelProvider.getCustomEventLabel(
                    customLabelContext = CustomLabelContext.ProgramStage(STAGE_UID, PROGRAM_UID),
                    quantity = 2,
                )

            assertEquals("Consultations", result)
        }

    @Test
    fun `program stage context falls back to program event label when stage does not exist`() =
        runTest {
            mockProgram(eventLabel = "consultation", eventsLabel = "consultations")
            whenever(
                d2
                    .programModule()
                    .programStages()
                    .uid(STAGE_UID)
                    .blockingGet(),
            ) doReturn null

            val result =
                customLabelProvider.getCustomEventLabel(
                    customLabelContext = CustomLabelContext.ProgramStage(STAGE_UID, PROGRAM_UID),
                    quantity = 1,
                )

            assertEquals("Consultation", result)
        }

    // endregion

    // region enrollment label

    @Test
    fun `returns singular enrollment label when quantity is one`() =
        runTest {
            mockProgram(enrollmentLabel = "case", enrollmentsLabel = "cases")

            val result =
                customLabelProvider.getCustomEnrollmentLabel(
                    programUid = PROGRAM_UID,
                    quantity = 1,
                )

            assertEquals("Case", result)
        }

    @Test
    fun `returns plural enrollment label when quantity is greater than one`() =
        runTest {
            mockProgram(enrollmentLabel = "case", enrollmentsLabel = "cases")

            val result =
                customLabelProvider.getCustomEnrollmentLabel(
                    programUid = PROGRAM_UID,
                    quantity = 3,
                )

            assertEquals("Cases", result)
        }

    // endregion

    private fun mockProgram(
        eventLabel: String? = null,
        eventsLabel: String? = null,
        enrollmentLabel: String? = null,
        enrollmentsLabel: String? = null,
    ) {
        val program =
            mock<Program> {
                on { displayEventLabel } doReturn eventLabel
                on { displayEventsLabel } doReturn eventsLabel
                on { displayEnrollmentLabel() } doReturn enrollmentLabel
                on { displayEnrollmentsLabel() } doReturn enrollmentsLabel
            }
        whenever(
            d2
                .programModule()
                .programs()
                .uid(PROGRAM_UID)
                .blockingGet(),
        ) doReturn program
    }

    private fun mockProgramStage(
        eventLabel: String?,
        eventsLabel: String?,
    ) {
        val programStage =
            mock<ProgramStage> {
                on { displayEventLabel } doReturn eventLabel
                on { displayEventsLabel } doReturn eventsLabel
            }
        whenever(
            d2
                .programModule()
                .programStages()
                .uid(STAGE_UID)
                .blockingGet(),
        ) doReturn programStage
    }

    companion object {
        private const val PROGRAM_UID = "programUid"
        private const val STAGE_UID = "programStageUid"
    }
}
