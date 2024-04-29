package org.dhis2.data.dhislogic

import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.junit.Assert
import org.junit.Test
import java.time.Instant
import java.util.Date

class OrganisationUnitExtensionsTest {

    @Test
    fun `Should return true if date is null`() {
        Assert.assertTrue(
            orgUnit(null, null).inDateRange(null) &&
                orgUnit(Date(), Date()).inDateRange(null) &&
                orgUnit(Date(), null).inDateRange(null) &&
                orgUnit(null, Date()).inDateRange(null),
        )
    }

    @Test
    fun `Should return true if date not null and option does not have start and end dates`() {
        Assert.assertTrue(
            orgUnit(null, null).inDateRange(Date()),
        )
    }

    @Test
    fun `Should return true if date after option start date`() {
        val date = Date.from(Instant.parse("2020-01-02T00:00:00.00Z"))
        val startDate = Date.from(Instant.parse("2020-01-01T00:00:00.00Z"))
        Assert.assertTrue(
            orgUnit(startDate, null).inDateRange(date),
        )
    }

    @Test
    fun `Should return true if date before option end date`() {
        val date = Date.from(Instant.parse("2020-01-02T00:00:00.00Z"))
        val endDate = Date.from(Instant.parse("2020-01-03T00:00:00.00Z"))
        Assert.assertTrue(
            orgUnit(null, endDate).inDateRange(date),
        )
    }

    @Test
    fun `Should return true if date between option start and end date`() {
        val date = Date.from(Instant.parse("2020-01-02T00:00:00.00Z"))
        val startDate = Date.from(Instant.parse("2020-01-01T00:00:00.00Z"))
        val endDate = Date.from(Instant.parse("2020-01-03T00:00:00.00Z"))
        Assert.assertTrue(
            orgUnit(startDate, endDate).inDateRange(date),
        )
    }

    private fun orgUnit(startDate: Date?, endDate: Date?): OrganisationUnit {
        return OrganisationUnit.builder()
            .uid("orgUnitUid")
            .displayName("OrgUnitName")
            .openingDate(startDate)
            .closedDate(endDate)
            .build()
    }
}
