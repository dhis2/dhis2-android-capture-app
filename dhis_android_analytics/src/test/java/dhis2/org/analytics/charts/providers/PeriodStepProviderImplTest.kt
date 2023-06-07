package dhis2.org.analytics.charts.providers

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import java.util.Date
import java.util.GregorianCalendar
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.period.Period
import org.hisp.dhis.android.core.period.PeriodType
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito

class PeriodStepProviderImplTest {
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val periodStepProvider = PeriodStepProviderImpl(d2)

    @Test
    fun `Should get correct period step`() {
        periodStepProvider.periodStep(PeriodType.Monthly)
        verify(d2.periodModule().periodHelper(), times(2)).blockingGetPeriodForPeriodTypeAndDate(
            any(),
            any(),
            any()
        )
    }

    @Test
    fun `Should return correct difference for a daily period`() {
        val initialPeriod = generatePeriod(
            PeriodType.Daily,
            GregorianCalendar(2021, 0, 1).time,
            GregorianCalendar(2021, 0, 2).time
        )

        val endPeriod = generatePeriod(
            PeriodType.Daily,
            GregorianCalendar(2021, 0, 4).time,
            GregorianCalendar(2021, 0, 5).time
        )

        val diff = periodStepProvider.getPeriodDiff(initialPeriod, endPeriod)

        assertTrue(diff == 3)
    }

    @Test
    fun `Should return correct difference for a daily period in February`() {
        val initialPeriod = generatePeriod(
            PeriodType.Daily,
            GregorianCalendar(2021, 1, 28).time,
            GregorianCalendar(2021, 2, 0).time
        )

        val endPeriod = generatePeriod(
            PeriodType.Daily,
            GregorianCalendar(2021, 2, 1).time,
            GregorianCalendar(2021, 2, 2).time
        )

        val diff = periodStepProvider.getPeriodDiff(initialPeriod, endPeriod)

        assertTrue(diff == 1)
    }

    @Test
    fun `Should return correct difference for a weekly period`() {
        val initialPeriod = generatePeriod(
            PeriodType.Weekly,
            GregorianCalendar(2021, 6, 19).time,
            GregorianCalendar(2021, 6, 26).time
        )

        val endPeriod = generatePeriod(
            PeriodType.Weekly,
            GregorianCalendar(2021, 6, 26).time,
            GregorianCalendar(2021, 7, 2).time
        )

        val diff = periodStepProvider.getPeriodDiff(initialPeriod, endPeriod)

        assertTrue(diff == 1)
    }

    @Test
    fun `Should return correct difference for a biweekly period`() {
        val initialPeriod = generatePeriod(
            PeriodType.BiWeekly,
            GregorianCalendar(2021, 6, 19).time,
            GregorianCalendar(2021, 7, 2).time
        )

        val endPeriod = generatePeriod(
            PeriodType.BiWeekly,
            GregorianCalendar(2021, 7, 16).time,
            GregorianCalendar(2021, 7, 23).time
        )

        val diff = periodStepProvider.getPeriodDiff(initialPeriod, endPeriod)

        assertTrue(diff == 2)
    }

    @Test
    fun `Should return correct difference for a monthly period`() {
        val initialPeriod = generatePeriod(
            PeriodType.Monthly,
            GregorianCalendar(2021, 0, 1).time,
            GregorianCalendar(2021, 0, 31).time
        )

        val endPeriod = generatePeriod(
            PeriodType.Monthly,
            GregorianCalendar(2021, 11, 1).time,
            GregorianCalendar(2021, 11, 31).time
        )

        val diff = periodStepProvider.getPeriodDiff(initialPeriod, endPeriod)

        assertTrue(diff == 11)
    }

    @Test
    fun `Should return correct difference for a bimonthly period`() {
        val initialPeriod = generatePeriod(
            PeriodType.BiMonthly,
            GregorianCalendar(2021, 0, 1).time,
            GregorianCalendar(2021, 1, 28).time
        )

        val endPeriod = generatePeriod(
            PeriodType.BiMonthly,
            GregorianCalendar(2021, 10, 1).time,
            GregorianCalendar(2021, 11, 31).time
        )

        val diff = periodStepProvider.getPeriodDiff(initialPeriod, endPeriod)

        assertTrue(diff == 5)
    }

    @Test
    fun `Should return correct difference for a quarterly period`() {
        val initialPeriod = generatePeriod(
            PeriodType.Quarterly,
            GregorianCalendar(2021, 0, 1).time,
            GregorianCalendar(2021, 2, 31).time
        )

        val endPeriod = generatePeriod(
            PeriodType.Quarterly,
            GregorianCalendar(2021, 9, 1).time,
            GregorianCalendar(2021, 11, 31).time
        )

        val diff = periodStepProvider.getPeriodDiff(initialPeriod, endPeriod)

        assertTrue(diff == 3)
    }

    @Test
    fun `Should return correct difference for a sixmonthly period`() {
        val initialPeriod = generatePeriod(
            PeriodType.SixMonthlyApril,
            GregorianCalendar(2021, 3, 1).time,
            GregorianCalendar(2021, 8, 28).time
        )

        val endPeriod = generatePeriod(
            PeriodType.SixMonthlyApril,
            GregorianCalendar(2023, 9, 1).time,
            GregorianCalendar(2023, 11, 31).time
        )

        val diff = periodStepProvider.getPeriodDiff(initialPeriod, endPeriod)

        assertTrue(diff == 5)
    }

    @Test
    fun `Should return correct difference for a yearly period`() {
        val initialPeriod = generatePeriod(
            PeriodType.FinancialJuly,
            GregorianCalendar(2021, 6, 1).time,
            GregorianCalendar(2021, 6, 31).time
        )

        val endPeriod = generatePeriod(
            PeriodType.FinancialJuly,
            GregorianCalendar(2023, 6, 1).time,
            GregorianCalendar(2023, 6, 31).time
        )

        val diff = periodStepProvider.getPeriodDiff(initialPeriod, endPeriod)

        assertTrue(diff == 2)
    }

    private fun generatePeriod(periodType: PeriodType, startDate: Date, endDate: Date) =
        Period.builder()
            .periodId("")
            .periodType(periodType)
            .startDate(startDate)
            .endDate(endDate)
            .build()
}
