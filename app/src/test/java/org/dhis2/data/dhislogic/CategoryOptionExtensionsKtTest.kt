package org.dhis2.data.dhislogic

import java.time.Instant
import java.util.Date
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryOptionExtensionsKtTest {

    @Test
    fun `Should return true if date is null`() {
        assertTrue(
            catOption(null, null).inDateRange(null) &&
                catOption(Date(), Date()).inDateRange(null) &&
                catOption(Date(), null).inDateRange(null) &&
                catOption(null, Date()).inDateRange(null)
        )
    }

    @Test
    fun `Should return true if date not null and option does not have start and end dates`() {
        assertTrue(
            catOption(null, null).inDateRange(Date())
        )
    }

    @Test
    fun `Should return true if date after option start date`() {
        val date = Date.from(Instant.parse("2020-01-02T00:00:00.00Z"))
        val startDate = Date.from(Instant.parse("2020-01-01T00:00:00.00Z"))
        assertTrue(
            catOption(startDate, null).inDateRange(date)
        )
    }

    @Test
    fun `Should return true if date before option end date`() {
        val date = Date.from(Instant.parse("2020-01-02T00:00:00.00Z"))
        val endDate = Date.from(Instant.parse("2020-01-03T00:00:00.00Z"))
        assertTrue(
            catOption(null, endDate).inDateRange(date)
        )
    }

    @Test
    fun `Should return true if date between option start and end date`() {
        val date = Date.from(Instant.parse("2020-01-02T00:00:00.00Z"))
        val startDate = Date.from(Instant.parse("2020-01-01T00:00:00.00Z"))
        val endDate = Date.from(Instant.parse("2020-01-03T00:00:00.00Z"))
        assertTrue(
            catOption(startDate, endDate).inDateRange(date)
        )
    }

    @Test
    fun `Should return true if orgUnitUid is null`() {
        assertTrue(
            catOption(null).inOrgUnit(null) &&
                catOption(listOf()).inOrgUnit(null)
        )
    }

    @Test
    fun `Should return true if orgUnits are null or empty`() {
        val orgUnitUid = "orgUnitUid"
        assertTrue(
            catOption(null).inOrgUnit(orgUnitUid) &&
                catOption(listOf()).inOrgUnit(orgUnitUid)
        )
    }

    @Test
    fun `Should return true if orgUnitUid is in orgUnits`() {
        val orgUnitUid = "orgUnitUid"
        val orgUnits = listOf(ObjectWithUid.create(orgUnitUid))
        assertTrue(
            catOption(orgUnits).inOrgUnit(orgUnitUid)
        )
    }

    @Test
    fun `Should return false if orgUnitUid is not in orgUnits`() {
        val orgUnitUid = "orgUnitUid"
        val orgUnits = listOf(ObjectWithUid.create("otherUid"))
        assertFalse(
            catOption(orgUnits).inOrgUnit(orgUnitUid)
        )
    }

    private fun catOption(startDate: Date?, endDate: Date?): CategoryOption {
        return CategoryOption.builder()
            .uid("CatOptUid")
            .displayName("CatOptName")
            .startDate(startDate)
            .endDate(endDate)
            .access(Access.create(true, true, DataAccess.create(true, true)))
            .build()
    }

    private fun catOption(orgUnits: List<ObjectWithUid>?): CategoryOption {
        return CategoryOption.builder()
            .uid("CatOptUid")
            .displayName("CatOptName")
            .organisationUnits(orgUnits)
            .access(Access.create(true, true, DataAccess.create(true, true)))
            .build()
    }
}
