package org.dhis2.utils.filters.workingLists

import org.dhis2.commons.filters.FilterResources
import org.dhis2.commons.filters.workingLists.RelativePeriodToStringMapper
import org.hisp.dhis.android.core.common.RelativePeriod
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class RelativePeriodToStringMapperTest {

    private val filterResources: FilterResources = mock()
    private lateinit var mapper: RelativePeriodToStringMapper

    @Before
    fun setUp() {
        mapper = RelativePeriodToStringMapper(filterResources)
    }

    @Test
    fun `Should return string for relative period`() {
        mapper.map(RelativePeriod.TODAY)
        verify(filterResources).todayLabel()
        mapper.map(RelativePeriod.LAST_60_DAYS)
        verify(filterResources).lastNDays(60)
        mapper.map(RelativePeriod.LAST_BIMONTH)
        verify(filterResources).lastBiMonth()
        mapper.map(RelativePeriod.MONTHS_LAST_YEAR)
        verify(filterResources).monthsLastYear()
        mapper.map(RelativePeriod.LAST_2_SIXMONTHS)
        verify(filterResources).lastNSixMonths(2)
        mapper.map(RelativePeriod.THIS_WEEK)
        verify(filterResources).thisWeek()
        mapper.map(RelativePeriod.LAST_52_WEEKS)
        verify(filterResources).lastNWeeks(52)
    }

    @Test
    fun `Should return null`() {
        assertTrue(mapper.map(null) == null)
    }

    @Test
    fun `Should return span string`() {
        mapper.span()
        verify(filterResources, times(1)).span()
    }
}
