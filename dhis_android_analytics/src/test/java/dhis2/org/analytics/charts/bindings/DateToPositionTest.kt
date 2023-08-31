package dhis2.org.analytics.charts.bindings

import java.time.YearMonth
import java.util.GregorianCalendar
import org.hisp.dhis.android.core.period.PeriodType
import org.junit.Assert.assertArrayEquals
import org.junit.Test

class DateToPositionTest {
    val dateToPosition = DateToPosition()

    @Test
    fun `Should return correct position in months`() {
        val testDates = listOf(
            GregorianCalendar(2021, 1, 3).time,
            GregorianCalendar(2021, 2, 5).time,
            GregorianCalendar(2021, 3, 2).time,
            GregorianCalendar(2021, 4, 1).time,
            GregorianCalendar(2022, 4, 8).time
        )

        val expectedPositions = listOf(
            0.071428575f,
            1.1290323f,
            2.0333333f,
            3f,
            15.225806f
        )

        listOf(
            PeriodType.Daily,
            PeriodType.Weekly,
            PeriodType.WeeklySaturday,
            PeriodType.WeeklySunday,
            PeriodType.WeeklyThursday,
            PeriodType.WeeklyWednesday,
            PeriodType.BiWeekly,
            PeriodType.Monthly,
            PeriodType.BiMonthly,
            PeriodType.Quarterly,
            PeriodType.SixMonthly,
            PeriodType.SixMonthlyApril,
            PeriodType.SixMonthlyNov
        ).forEach { period ->
            val positions = mutableListOf<Float>()
            var minYearMonth: YearMonth? = null

            testDates.forEach { date ->
                positions.add(
                    dateToPosition(
                        date,
                        period,
                        minYearMonth
                    ) {
                        minYearMonth = it
                    }
                )
            }

            assertArrayEquals(expectedPositions.toTypedArray(), positions.toTypedArray())
        }
    }

    @Test
    fun `Should return correct position in years`() {
        val testDates = listOf(
            GregorianCalendar(2021, 1, 3).time,
            GregorianCalendar(2022, 2, 5).time,
            GregorianCalendar(2023, 3, 2).time,
            GregorianCalendar(2024, 1, 1).time,
            GregorianCalendar(2025, 4, 8).time
        )

        val expectedPositions = listOf(
            0f,
            1f,
            2f,
            3f,
            4f
        )

        listOf(
            PeriodType.Yearly,
            PeriodType.FinancialApril,
            PeriodType.FinancialJuly,
            PeriodType.FinancialOct,
            PeriodType.FinancialNov
        ).forEach { period ->
            val positions = mutableListOf<Float>()
            var minYearMonth: YearMonth? = null

            testDates.forEach { date ->
                positions.add(
                    dateToPosition(
                        date,
                        period,
                        minYearMonth
                    ) {
                        minYearMonth = it
                    }
                )
            }

            assertArrayEquals(expectedPositions.toTypedArray(), positions.toTypedArray())
        }
    }
}
