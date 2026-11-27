package org.dhis2.mobile.commons.category.data

import kotlinx.datetime.LocalDate
import org.dhis2.mobile.commons.category.domain.model.OrgUnitRestriction
import org.hisp.dhis.android.core.category.Category
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.common.ObjectWithUid
import java.util.Calendar
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CategoryMappersTest {
    @Test
    fun `null org units map to unrestricted`() {
        val option = sdkOption(organisationUnits = null).toDomain()

        assertEquals(OrgUnitRestriction.Unrestricted, option.orgUnitRestriction)
    }

    @Test
    fun `empty org units map to a restriction to no org unit`() {
        val option = sdkOption(organisationUnits = emptyList()).toDomain()

        assertEquals(OrgUnitRestriction.RestrictedTo(emptySet()), option.orgUnitRestriction)
    }

    @Test
    fun `org units map to their uids`() {
        val option = sdkOption(organisationUnits = listOf(ObjectWithUid.create("ou1"), ObjectWithUid.create("ou2"))).toDomain()

        assertEquals(OrgUnitRestriction.RestrictedTo(setOf("ou1", "ou2")), option.orgUnitRestriction)
    }

    @Test
    fun `name falls back to code and then to empty`() {
        assertEquals("Display", sdkOption(displayName = "Display", code = "CODE").toDomain().name)
        assertEquals("CODE", sdkOption(displayName = null, code = "CODE").toDomain().name)
        assertEquals("", sdkOption(displayName = null, code = null).toDomain().name)
    }

    @Test
    fun `write access maps to canWrite`() {
        assertTrue(sdkOption(write = true).toDomain().canWrite)
        assertFalse(sdkOption(write = false).toDomain().canWrite)
    }

    @Test
    fun `dates keep only their local day`() {
        val start = localDate(2024, Calendar.JANUARY, 1, 23, 30)
        val option = sdkOption(startDate = start, endDate = null).toDomain()

        assertEquals(LocalDate(2024, 1, 1), option.startDate)
        assertNull(option.endDate)
    }

    @Test
    fun `category name falls back to uid`() {
        val category =
            Category
                .builder()
                .uid("categoryUid")
                .build()
                .toDomain(emptyList())

        assertEquals("categoryUid", category.name)
        assertNull(category.selectedOptionUid)
    }

    private fun sdkOption(
        displayName: String? = "Option",
        code: String? = "CODE",
        write: Boolean = true,
        startDate: java.util.Date? = null,
        endDate: java.util.Date? = null,
        organisationUnits: List<ObjectWithUid>? = null,
    ): CategoryOption =
        CategoryOption
            .builder()
            .uid("optionUid")
            .displayName(displayName)
            .code(code)
            .startDate(startDate)
            .endDate(endDate)
            .access(Access.create(true, true, DataAccess.create(true, write)))
            .organisationUnits(organisationUnits)
            .build()

    private fun localDate(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
    ): java.util.Date =
        Calendar
            .getInstance()
            .apply {
                clear()
                set(year, month, day, hour, minute)
            }.time
}
