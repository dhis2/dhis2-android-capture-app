package org.dhis2.data.dhislogic

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.period.Period
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.period.internal.PeriodHelper
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.GregorianCalendar
import java.util.Locale

class DhisPeriodUtilsTest {

    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val periodHelper: PeriodHelper = mock()
    private lateinit var periodUtils: DhisPeriodUtils
    private val testDate = GregorianCalendar(2019, 0, 11).time

    @Before
    fun setUp() {
        whenever(d2.periodModule().periodHelper()) doReturn periodHelper
        periodUtils = DhisPeriodUtils(
            d2,
            "%s - %s",
            "Week %d %s to %s",
            "%d %s - %d %s",
        )
    }

    @Test
    fun `Null period should return daily text`() {
        whenever(
            periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                PeriodType.Daily,
                testDate,
            ),
        ) doReturn Period.builder()
            .periodId(null)
            .startDate(testDate)
            .endDate(testDate)
            .build()

        Assert.assertEquals(
            "11/1/2019",
            periodUtils.getPeriodUIString(null, testDate, Locale.ENGLISH),
        )
    }

    @Test
    fun `Daily period should return expected result`() {
        whenever(
            periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                PeriodType.Daily,
                testDate,
            ),
        ) doReturn Period.builder()
            .periodId(null)
            .startDate(testDate)
            .endDate(testDate)
            .build()

        Assert.assertEquals(
            "11/1/2019",
            periodUtils.getPeriodUIString(
                PeriodType.Daily,
                testDate,
                Locale.ENGLISH,
            ),
        )
    }

    @Test
    fun `Weekly period should return expected result`() {
        whenever(
            periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                PeriodType.Weekly,
                testDate,
            ),
        ) doReturn Period.builder()
            .periodId("2019W2")
            .startDate(GregorianCalendar(2019, 0, 7).time)
            .endDate(GregorianCalendar(2019, 0, 13).time)
            .build()

        Assert.assertEquals(
            "Week 2 2019-01-07 To 2019-01-13",
            periodUtils.getPeriodUIString(
                PeriodType.Weekly,
                testDate,
                Locale.ENGLISH,
            ),
        )
    }

    @Test
    fun `WeeklyWednesday period should return expected result`() {
        whenever(
            periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                PeriodType.WeeklyWednesday,
                testDate,
            ),
        ) doReturn Period.builder()
            .periodId("2019WedW2")
            .startDate(GregorianCalendar(2019, 0, 9).time)
            .endDate(GregorianCalendar(2019, 0, 15).time)
            .build()

        Assert.assertEquals(
            "Week 2 2019-01-09 To 2019-01-15",
            periodUtils.getPeriodUIString(
                PeriodType.WeeklyWednesday,
                testDate,
                Locale.ENGLISH,
            ),
        )
    }

    @Test
    fun `WeeklyThursday period should return expected result`() {
        whenever(
            periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                PeriodType.WeeklyThursday,
                testDate,
            ),
        ) doReturn Period.builder()
            .periodId("2019ThuW2")
            .startDate(GregorianCalendar(2019, 0, 10).time)
            .endDate(GregorianCalendar(2019, 0, 16).time)
            .build()

        Assert.assertEquals(
            "Week 2 2019-01-10 To 2019-01-16",
            periodUtils.getPeriodUIString(
                PeriodType.WeeklyThursday,
                testDate,
                Locale.ENGLISH,
            ),
        )
    }

    @Test
    fun `WeeklySaturday period should return expected result`() {
        whenever(
            periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                PeriodType.WeeklySaturday,
                testDate,
            ),
        ) doReturn Period.builder()
            .periodId("2019SatW1")
            .startDate(GregorianCalendar(2019, 0, 5).time)
            .endDate(GregorianCalendar(2019, 0, 11).time)
            .build()

        Assert.assertEquals(
            "Week 1 2019-01-05 To 2019-01-11",
            periodUtils.getPeriodUIString(
                PeriodType.WeeklySaturday,
                testDate,
                Locale.ENGLISH,
            ),
        )
    }

    @Test
    fun `WeeklySunday period should return expected result`() {
        whenever(
            periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                PeriodType.WeeklySunday,
                testDate,
            ),
        ) doReturn Period.builder()
            .periodId("2019SunW2")
            .startDate(GregorianCalendar(2019, 0, 6).time)
            .endDate(GregorianCalendar(2019, 0, 12).time)
            .build()

        Assert.assertEquals(
            "Week 2 2019-01-06 To 2019-01-12",
            periodUtils.getPeriodUIString(
                PeriodType.WeeklySunday,
                testDate,
                Locale.ENGLISH,
            ),
        )
    }

    @Test
    fun `BiWeekly period should return expected result`() {
        whenever(
            periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                PeriodType.BiWeekly,
                testDate,
            ),
        ) doReturn Period.builder()
            .periodId("2019W2")
            .startDate(GregorianCalendar(2019, 0, 9).time)
            .endDate(GregorianCalendar(2019, 0, 15).time)
            .build()

        whenever(
            periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                PeriodType.Weekly,
                GregorianCalendar(2019, 0, 9).time,
            ),
        )doReturn Period.builder()
            .periodId("2019W2")
            .startDate(GregorianCalendar(2019, 0, 9).time)
            .endDate(GregorianCalendar(2019, 0, 15).time)
            .build()

        whenever(
            periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                PeriodType.Weekly,
                GregorianCalendar(2019, 0, 15).time,
            ),
        )doReturn Period.builder()
            .periodId("2019W3")
            .startDate(GregorianCalendar(2019, 0, 16).time)
            .endDate(GregorianCalendar(2019, 0, 22).time)
            .build()

        Assert.assertEquals(
            "2 2019 - 3 2019",
            periodUtils.getPeriodUIString(
                PeriodType.BiWeekly,
                testDate,
                Locale.ENGLISH,
            ),
        )
    }

    @Test
    fun `Monthly period should return expected result`() {
        whenever(
            periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                PeriodType.Monthly,
                testDate,
            ),
        ) doReturn Period.builder()
            .periodId("periodId")
            .startDate(GregorianCalendar(2019, 0, 1).time)
            .endDate(GregorianCalendar(2019, 0, 31).time)
            .build()

        Assert.assertEquals(
            "Jan 2019",
            periodUtils.getPeriodUIString(
                PeriodType.Monthly,
                testDate,
                Locale.ENGLISH,
            ),
        )
    }

    @Test
    fun `BiMonthly period should return expected result`() {
        whenever(
            periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                PeriodType.BiMonthly,
                testDate,
            ),
        ) doReturn Period.builder()
            .periodId("periodId")
            .startDate(GregorianCalendar(2019, 0, 1).time)
            .endDate(GregorianCalendar(2019, 1, 28).time)
            .build()

        Assert.assertEquals(
            "Jan 2019 - Feb 2019",
            periodUtils.getPeriodUIString(
                PeriodType.BiMonthly,
                testDate,
                Locale.ENGLISH,
            ),
        )
    }

    @Test
    fun `Quaterly period should return expected result`() {
        whenever(
            periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                PeriodType.Quarterly,
                testDate,
            ),
        ) doReturn Period.builder()
            .periodId("periodId")
            .startDate(GregorianCalendar(2019, 0, 1).time)
            .endDate(GregorianCalendar(2019, 2, 31).time)
            .build()

        Assert.assertEquals(
            "Jan 2019 - Mar 2019",
            periodUtils.getPeriodUIString(
                PeriodType.Quarterly,
                testDate,
                Locale.ENGLISH,
            ),
        )
    }

    @Test
    fun `SixMonthly period should return expected result`() {
        whenever(
            periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                PeriodType.SixMonthly,
                testDate,
            ),
        ) doReturn Period.builder()
            .periodId("periodId")
            .startDate(GregorianCalendar(2019, 0, 1).time)
            .endDate(GregorianCalendar(2019, 5, 30).time)
            .build()

        Assert.assertEquals(
            "Jan 2019 - Jun 2019",
            periodUtils.getPeriodUIString(
                PeriodType.SixMonthly,
                testDate,
                Locale.ENGLISH,
            ),
        )
    }

    @Test
    fun `SixMonthlyApril period should return expected result`() {
        whenever(
            periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                PeriodType.SixMonthlyApril,
                testDate,
            ),
        ) doReturn Period.builder()
            .periodId("periodId")
            .startDate(GregorianCalendar(2018, 9, 1).time)
            .endDate(GregorianCalendar(2019, 2, 31).time)
            .build()

        Assert.assertEquals(
            "Oct 2018 - Mar 2019",
            periodUtils.getPeriodUIString(
                PeriodType.SixMonthlyApril,
                testDate,
                Locale.ENGLISH,
            ),
        )
    }

    @Test
    fun `Yearly period should return expected result`() {
        whenever(
            periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                PeriodType.Yearly,
                testDate,
            ),
        ) doReturn Period.builder()
            .periodId("periodId")
            .startDate(GregorianCalendar(2019, 0, 1).time)
            .endDate(GregorianCalendar(2019, 11, 31).time)
            .build()

        Assert.assertEquals(
            "2019",
            periodUtils.getPeriodUIString(
                PeriodType.Yearly,
                testDate,
                Locale.ENGLISH,
            ),
        )
    }

    @Test
    fun `FinancialApril period should return expected result`() {
        whenever(
            periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                PeriodType.FinancialApril,
                testDate,
            ),
        ) doReturn Period.builder()
            .periodId("periodId")
            .startDate(GregorianCalendar(2018, 3, 1).time)
            .endDate(GregorianCalendar(2019, 2, 31).time)
            .build()

        Assert.assertEquals(
            "Apr 2018 - Mar 2019",
            periodUtils.getPeriodUIString(
                PeriodType.FinancialApril,
                testDate,
                Locale.ENGLISH,
            ),
        )
    }

    @Test
    fun `FinancialJuly period should return expected result`() {
        whenever(
            periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                PeriodType.FinancialJuly,
                testDate,
            ),
        ) doReturn Period.builder()
            .periodId("periodId")
            .startDate(GregorianCalendar(2018, 6, 1).time)
            .endDate(GregorianCalendar(2019, 5, 30).time)
            .build()

        Assert.assertEquals(
            "Jul 2018 - Jun 2019",
            periodUtils.getPeriodUIString(
                PeriodType.FinancialJuly,
                testDate,
                Locale.ENGLISH,
            ),
        )
    }

    @Test
    fun `FinancialOct period should return expected result`() {
        whenever(
            periodHelper.blockingGetPeriodForPeriodTypeAndDate(
                PeriodType.FinancialOct,
                testDate,
            ),
        ) doReturn Period.builder()
            .periodId("periodId")
            .startDate(GregorianCalendar(2018, 9, 1).time)
            .endDate(GregorianCalendar(2019, 8, 30).time)
            .build()

        Assert.assertEquals(
            "Oct 2018 - Sep 2019",
            periodUtils.getPeriodUIString(
                PeriodType.FinancialOct,
                testDate,
                Locale.ENGLISH,
            ),
        )
    }
}
