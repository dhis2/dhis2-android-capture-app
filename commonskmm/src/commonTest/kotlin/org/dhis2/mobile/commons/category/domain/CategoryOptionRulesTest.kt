package org.dhis2.mobile.commons.category.domain

import kotlinx.datetime.LocalDate
import org.dhis2.mobile.commons.category.domain.model.Category
import org.dhis2.mobile.commons.category.domain.model.CategoryOption
import org.dhis2.mobile.commons.category.domain.model.CategorySelectionError
import org.dhis2.mobile.commons.category.domain.model.NoOptionsReason
import org.dhis2.mobile.commons.category.domain.model.OrgUnitRestriction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CategoryOptionRulesTest {
    private val orgUnit = "orgUnit"
    private val date = LocalDate(2024, 6, 15)

    @Test
    fun `option is valid on its start and end date`() {
        val option = option(startDate = LocalDate(2024, 1, 1), endDate = LocalDate(2024, 3, 31))

        assertFalse(option.isValidOn(LocalDate(2023, 12, 31)))
        assertTrue(option.isValidOn(LocalDate(2024, 1, 1)))
        assertTrue(option.isValidOn(LocalDate(2024, 2, 15)))
        assertTrue(option.isValidOn(LocalDate(2024, 3, 31)))
        assertFalse(option.isValidOn(LocalDate(2024, 4, 1)))
    }

    @Test
    fun `option without dates or without a reference date is valid`() {
        assertTrue(option().isValidOn(LocalDate(1900, 1, 1)))
        assertTrue(option(startDate = LocalDate(2024, 1, 1)).isValidOn(LocalDate(2100, 1, 1)))
        assertTrue(option(endDate = LocalDate(2024, 1, 1)).isValidOn(LocalDate(1900, 1, 1)))
        assertTrue(option(startDate = LocalDate(2024, 1, 1), endDate = LocalDate(2024, 1, 2)).isValidOn(null))
    }

    @Test
    fun `org unit availability follows the restriction`() {
        assertTrue(option(orgUnitRestriction = OrgUnitRestriction.Unrestricted).isAvailableIn(orgUnit))
        assertTrue(restrictedTo(orgUnit).isAvailableIn(orgUnit))
        assertFalse(restrictedTo("other").isAvailableIn(orgUnit))
        assertFalse(restrictedTo().isAvailableIn(orgUnit))
    }

    @Test
    fun `without an org unit only options available nowhere are ruled out`() {
        assertTrue(option().isAvailableIn(null))
        assertTrue(restrictedTo("other").isAvailableIn(null))
        assertFalse(restrictedTo().isAvailableIn(null))
    }

    @Test
    fun `option is selectable only with write access, in the org unit and on the date`() {
        assertTrue(option().isSelectable(orgUnit, date))
        assertFalse(option(canWrite = false).isSelectable(orgUnit, date))
        assertFalse(restrictedTo("other").isSelectable(orgUnit, date))
        assertFalse(option(endDate = LocalDate(2024, 6, 14)).isSelectable(orgUnit, date))
    }

    @Test
    fun `selection is read-only when a selected option can't be written`() {
        val readOnly = Category("c1", "C1", listOf(option(uid = "view", canWrite = false)), "view")
        val writable = Category("c2", "C2", listOf(option(uid = "write")), "write")

        assertTrue(listOf(writable, readOnly).isReadOnly())
        assertFalse(listOf(writable).isReadOnly())
        assertFalse(listOf(readOnly.copy(selectedOptionUid = null)).isReadOnly())
        assertTrue(listOf(option(), option(canWrite = false)).isReadOnlySelection())
        assertFalse(listOf(option()).isReadOnlySelection())
    }

    @Test
    fun `selectable options are filtered and never pre-selected`() {
        val valid = option(uid = "valid")
        val category = Category("category", "Category", listOf(valid, option(uid = "noWrite", canWrite = false)))

        assertEquals(listOf(valid), category.selectableOptions(orgUnit, date, isReadOnly = false))
        assertNull(category.selectedOptionUid)
    }

    @Test
    fun `read-only selection offers only the selected option`() {
        val selected = option(uid = "view", canWrite = false)
        val category = Category("category", "Category", listOf(selected, option(uid = "other")), "view")

        assertEquals(listOf(selected), category.selectableOptions(orgUnit, date, isReadOnly = true))
        assertNull(category.selectionError(orgUnit, date, isReadOnly = true))
    }

    @Test
    fun `selection error reports why the selected option is no longer valid`() {
        val expired = option(uid = "expired", endDate = LocalDate(2024, 1, 1))
        val elsewhere = option(uid = "elsewhere", orgUnitRestriction = OrgUnitRestriction.RestrictedTo(setOf("other")))
        val both = expired.copy(uid = "both", orgUnitRestriction = elsewhere.orgUnitRestriction)
        val category = Category("category", "Category", listOf(expired, elsewhere, both, option(uid = "valid")))

        fun errorFor(uid: String?) = category.copy(selectedOptionUid = uid).selectionError(orgUnit, date, isReadOnly = false)

        assertEquals(CategorySelectionError.OUT_OF_DATE_RANGE, errorFor("expired"))
        assertEquals(CategorySelectionError.NOT_IN_ORG_UNIT, errorFor("elsewhere"))
        assertEquals(CategorySelectionError.OUT_OF_DATE_RANGE, errorFor("both"))
        assertNull(errorFor("valid"))
        assertNull(errorFor(null))
    }

    @Test
    fun `no options reason goes from access to org unit to date`() {
        fun reasonFor(vararg options: CategoryOption) =
            Category("category", "Category", options.toList()).noOptionsReason(orgUnit, date, isReadOnly = false)

        assertNull(reasonFor(option()))
        assertEquals(NoOptionsReason.NO_ACCESS, reasonFor())
        assertEquals(NoOptionsReason.NO_ACCESS, reasonFor(option(canWrite = false)))
        assertEquals(
            NoOptionsReason.NOT_IN_ORG_UNIT,
            reasonFor(option(canWrite = false), restrictedTo("other").copy(endDate = LocalDate(2024, 1, 1))),
        )
        assertEquals(
            NoOptionsReason.NOT_VALID_FOR_DATE,
            reasonFor(restrictedTo("other"), option(endDate = LocalDate(2024, 1, 1))),
        )
    }

    @Test
    fun `selection is completed when every category keeps a valid option`() {
        val first = Category("c1", "C1", listOf(option(uid = "a")), "a")
        val second = Category("c2", "C2", listOf(option(uid = "b"), option(uid = "expired", endDate = LocalDate(2024, 1, 1))))

        assertFalse(listOf(first, second).isCompleted(orgUnit, date))
        assertFalse(listOf(first, second.copy(selectedOptionUid = "expired")).isCompleted(orgUnit, date))
        assertTrue(listOf(first, second.copy(selectedOptionUid = "b")).isCompleted(orgUnit, date))
    }

    @Test
    fun `read-only selection is completed`() {
        val readOnly = Category("c1", "C1", listOf(option(uid = "view", canWrite = false, endDate = LocalDate(2024, 1, 1))), "view")

        assertTrue(listOf(readOnly).isCompleted(orgUnit, date))
    }

    private fun restrictedTo(vararg orgUnitUids: String) = option(orgUnitRestriction = OrgUnitRestriction.RestrictedTo(orgUnitUids.toSet()))

    private fun option(
        uid: String = "option",
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        canWrite: Boolean = true,
        orgUnitRestriction: OrgUnitRestriction = OrgUnitRestriction.Unrestricted,
    ) = CategoryOption(
        uid = uid,
        name = uid,
        startDate = startDate,
        endDate = endDate,
        canWrite = canWrite,
        orgUnitRestriction = orgUnitRestriction,
    )
}
