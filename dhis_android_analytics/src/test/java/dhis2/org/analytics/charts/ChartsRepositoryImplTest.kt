package dhis2.org.analytics.charts

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import java.util.Date
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ChartsRepositoryImplTest {
    private lateinit var repository: ChartsRepository
    private val d2: D2 = mock()
    private val baseRepository: BaseRepository = mock()

    @Before
    fun setUp() {
        repository = ChartsRepositoryImpl(d2, baseRepository)
    }

    @Test
    fun `Should get list of graphs`() {
        val expectedGraphNames = listOf(
            "name1-de1", "name1-de2", "name2-de1", "name2-de2"
        )
        whenever(baseRepository.repeatableStagesForEnrollment(any())) doReturn mockedStages()
        whenever(baseRepository.numericDataElements(any())) doReturn mockedDataElements()
        whenever(
            baseRepository.activeAndCompletedEventsWithData(
                any(),
                any(),
                any()
            )
        ) doReturn mockedData()
        val result = repository.getAnalyticsForEnrollment("enrollmentUid")
        assertTrue(result.size == 4)
        result.forEachIndexed { index, graph ->
            assertTrue(graph.title == expectedGraphNames[index])
        }
    }

    private fun mockedStages(): List<ProgramStage> {
        return listOf(
            ProgramStage.builder().uid("stage1")
                .displayName("name1")
                .periodType(PeriodType.Daily)
                .build(),
            ProgramStage.builder().uid("stage2")
                .displayName("name2")
                .periodType(PeriodType.Weekly)
                .build()
        )
    }

    private fun mockedDataElements(): List<DataElement> {
        return listOf(
            DataElement.builder().uid("de1").displayFormName("de1").build(),
            DataElement.builder().uid("de2").displayFormName("de2").build()
        )
    }

    private fun mockedData(): List<Pair<Event, TrackedEntityDataValue>> {
        return listOf(
            Pair(
                Event.builder().uid("event1").eventDate(Date()).build(),
                TrackedEntityDataValue.builder().event("event1").dataElement("de").value("1")
                    .build()
            )
        )
    }
}
