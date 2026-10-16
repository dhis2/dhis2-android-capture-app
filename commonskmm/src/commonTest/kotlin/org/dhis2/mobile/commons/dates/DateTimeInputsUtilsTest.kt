package org.dhis2.mobile.commons.dates

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DateTimeInputsUtilsTest {
    private val today = LocalDate(2026, 8, 11)

    // region calculateAgeFromDate

    @Test
    fun `should return age in years when birthday is today`() {
        assertEquals("26", calculateAgeFromDate("2000-08-11", "YEARS", today))
    }

    @Test
    fun `should not count the current year when birthday has not happened yet`() {
        assertEquals("25", calculateAgeFromDate("2000-08-12", "YEARS", today))
    }

    @Test
    fun `should count the current year when birthday already happened`() {
        assertEquals("26", calculateAgeFromDate("2000-08-10", "YEARS", today))
    }

    @Test
    fun `should return age in years for a leap day birth date`() {
        assertEquals("10", calculateAgeFromDate("2016-02-29", "YEARS", today))
    }

    @Test
    fun `should return age in months`() {
        assertEquals("3", calculateAgeFromDate("2026-05-11", "MONTHS", today))
    }

    @Test
    fun `should return age in months across a year boundary`() {
        assertEquals("9", calculateAgeFromDate("2025-11-11", "MONTHS", today))
    }

    @Test
    fun `should return age in days`() {
        assertEquals("10", calculateAgeFromDate("2026-08-01", "DAYS", today))
    }

    @Test
    fun `should return age in days across a full year`() {
        assertEquals("365", calculateAgeFromDate("2025-08-11", "DAYS", today))
    }

    @Test
    fun `should return null age for an unknown time unit`() {
        assertNull(calculateAgeFromDate("2000-08-11", "WEEKS", today))
    }

    @Test
    fun `should return null age for an unparseable date`() {
        assertNull(calculateAgeFromDate("not-a-date", "YEARS", today))
    }

    // endregion

    // region calculateDateFromAge

    @Test
    fun `should return date of birth from age in years`() {
        assertEquals("2020-08-11", calculateDateFromAge("6", "YEARS", today))
    }

    @Test
    fun `should return date of birth from age in months`() {
        assertEquals("2026-05-11", calculateDateFromAge("3", "MONTHS", today))
    }

    @Test
    fun `should return date of birth from age in days`() {
        assertEquals("2026-08-01", calculateDateFromAge("10", "DAYS", today))
    }

    @Test
    fun `should clamp to the end of a shorter month`() {
        assertEquals(
            "2026-02-28",
            calculateDateFromAge("1", "MONTHS", LocalDate(2026, 3, 31)),
        )
    }

    @Test
    fun `should return null date for a non numeric age`() {
        assertNull(calculateDateFromAge("six", "YEARS", today))
    }

    @Test
    fun `should return null date for an unknown time unit`() {
        assertNull(calculateDateFromAge("6", "WEEKS", today))
    }

    // endregion

    // region monthsBetween

    @Test
    fun `should count months between dates in the same year`() {
        assertEquals(3, monthsBetween(LocalDate(2026, 5, 1), LocalDate(2026, 8, 1)))
    }

    @Test
    fun `should count months between dates across a year boundary`() {
        assertEquals(9, monthsBetween(LocalDate(2025, 11, 1), LocalDate(2026, 8, 1)))
    }

    @Test
    fun `should count zero months within the same month`() {
        assertEquals(0, monthsBetween(LocalDate(2026, 8, 1), LocalDate(2026, 8, 31)))
    }

    // endregion
}
