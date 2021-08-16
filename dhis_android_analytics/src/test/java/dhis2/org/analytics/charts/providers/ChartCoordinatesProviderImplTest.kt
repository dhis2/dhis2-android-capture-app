package dhis2.org.analytics.charts.providers

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import java.util.Date
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.analytics.linelist.LineListResponse
import org.hisp.dhis.android.core.analytics.linelist.LineListResponseValue
import org.hisp.dhis.android.core.period.Period
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

class ChartCoordinatesProviderImplTest {
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val periodStepProvider: PeriodStepProvider = mock()
    private val coordinatesProvider = ChartCoordinatesProviderImpl(d2, periodStepProvider)

    @Test
    fun `Should get coordinates for data elements`() {
        mockedLineListResponse(false)
        val result = coordinatesProvider.dataElementCoordinates(
            "stageUid",
            "teiUid",
            "dataElementUid"
        )
        assertTrue(
            result.isNotEmpty()
        )
    }

    @Test
    fun `Should return empty coordinates for data elements`() {
        mockedLineListResponse(true)
        val result = coordinatesProvider.dataElementCoordinates(
            "stageUid",
            "teiUid",
            "dataElementUid"
        )
        assertTrue(
            result.isEmpty()
        )
    }

    @Test
    fun `Should get coordinates for indicators`() {
        mockedIndicatorLineListResponse(false)
        val result = coordinatesProvider.indicatorCoordinates(
            "stageUid",
            "teiUid",
            "indicatorUid"
        )
        assertTrue(
            result.isNotEmpty()
        )
    }

    @Test
    fun `Should return empty coordinates for indicator`() {
        mockedIndicatorLineListResponse(true)
        val result = coordinatesProvider.indicatorCoordinates(
            "stageUid",
            "teiUid",
            "indicatorUid"
        )
        assertTrue(
            result.isEmpty()
        )
    }

    private fun mockedLineListResponse(emptyList: Boolean) {
        whenever(
            d2.analyticsModule().eventLineList()
                .byProgramStage().eq("stageUid")
        ) doReturn mock()
        whenever(
            d2.analyticsModule().eventLineList()
                .byProgramStage().eq("stageUid")
                .byTrackedEntityInstance()
        ) doReturn mock()
        whenever(
            d2.analyticsModule().eventLineList()
                .byProgramStage().eq("stageUid")
                .byTrackedEntityInstance().eq("teiUid")
        ) doReturn mock()
        whenever(
            d2.analyticsModule().eventLineList()
                .byProgramStage().eq("stageUid")
                .byTrackedEntityInstance().eq("teiUid")
                .withDataElement("dataElementUid")
        ) doReturn mock()
        if (emptyList) {
            whenever(
                d2.analyticsModule().eventLineList()
                    .byProgramStage().eq("stageUid")
                    .byTrackedEntityInstance().eq("teiUid")
                    .withDataElement("dataElementUid")
                    .blockingEvaluate()
            ) doReturn emptyList()
        } else {
            whenever(
                d2.analyticsModule().eventLineList()
                    .byProgramStage().eq("stageUid")
                    .byTrackedEntityInstance().eq("teiUid")
                    .withDataElement("dataElementUid")
                    .blockingEvaluate()
            ) doReturn listOf(
                LineListResponse(
                    "uid",
                    Date(),
                    Period.builder().build(),
                    "orgUnit",
                    "orgUnitUid",
                    listOf(
                        LineListResponseValue("uid", "field", "125")
                    )
                )
            )
        }
    }

    private fun mockedIndicatorLineListResponse(emptyList: Boolean) {
        whenever(
            d2.analyticsModule().eventLineList()
                .byProgramStage().eq("stageUid")
        ) doReturn mock()
        whenever(
            d2.analyticsModule().eventLineList()
                .byProgramStage().eq("stageUid")
                .byTrackedEntityInstance()
        ) doReturn mock()
        whenever(
            d2.analyticsModule().eventLineList()
                .byProgramStage().eq("stageUid")
                .byTrackedEntityInstance().eq("teiUid")
        ) doReturn mock()
        whenever(
            d2.analyticsModule().eventLineList()
                .byProgramStage().eq("stageUid")
                .byTrackedEntityInstance().eq("teiUid")
                .withProgramIndicator("indicatorUid")
        ) doReturn mock()
        if (emptyList) {
            whenever(
                d2.analyticsModule().eventLineList()
                    .byProgramStage().eq("stageUid")
                    .byTrackedEntityInstance().eq("teiUid")
                    .withProgramIndicator("indicatorUid")
                    .blockingEvaluate()
            ) doReturn emptyList()
        } else {
            whenever(
                d2.analyticsModule().eventLineList()
                    .byProgramStage().eq("stageUid")
                    .byTrackedEntityInstance().eq("teiUid")
                    .withProgramIndicator("indicatorUid")
                    .blockingEvaluate()
            ) doReturn listOf(
                LineListResponse(
                    "uid",
                    Date(),
                    Period.builder().build(),
                    "orgUnit",
                    "orgUnitUid",
                    listOf(
                        LineListResponseValue("uid", "field", "125")
                    )
                )
            )
        }
    }
}
