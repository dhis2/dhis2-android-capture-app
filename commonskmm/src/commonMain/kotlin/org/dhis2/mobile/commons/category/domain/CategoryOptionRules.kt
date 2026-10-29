package org.dhis2.mobile.commons.category.domain

import kotlinx.datetime.LocalDate
import org.dhis2.mobile.commons.category.domain.model.Category
import org.dhis2.mobile.commons.category.domain.model.CategoryOption
import org.dhis2.mobile.commons.category.domain.model.CategorySelectionError
import org.dhis2.mobile.commons.category.domain.model.NoOptionsReason
import org.dhis2.mobile.commons.category.domain.model.OrgUnitRestriction

/**
 * Whether the option is valid on [date]. Both the start and the end date are valid days, as
 * on the server (E1056/E1057). A `null` date doesn't restrict the option.
 */
fun CategoryOption.isValidOn(date: LocalDate?): Boolean =
    date == null ||
        (startDate == null || startDate <= date) &&
        (endDate == null || date <= endDate)

/**
 * Whether the option is available in [orgUnitUid]. A `null` org unit only rules out the
 * options available nowhere.
 */
fun CategoryOption.isAvailableIn(orgUnitUid: String?): Boolean =
    when (orgUnitRestriction) {
        OrgUnitRestriction.Unrestricted -> true
        is OrgUnitRestriction.RestrictedTo ->
            orgUnitRestriction.orgUnitUids.isNotEmpty() &&
                (orgUnitUid == null || orgUnitUid in orgUnitRestriction.orgUnitUids)
    }

/**
 * Whether the user can pick the option for data captured in [orgUnitUid] on [date].
 */
fun CategoryOption.isSelectable(
    orgUnitUid: String?,
    date: LocalDate?,
): Boolean = canWrite && isAvailableIn(orgUnitUid) && isValidOn(date)

val Category.selectedOption: CategoryOption?
    get() = options.firstOrNull { it.uid == selectedOptionUid }

/**
 * A selection is read-only when the user can view but not write data for one of its selected
 * options: it is shown but can't be changed.
 */
fun List<CategoryOption>.isReadOnlySelection(): Boolean = any { !it.canWrite }

fun List<Category>.isReadOnly(): Boolean = mapNotNull { it.selectedOption }.isReadOnlySelection()

/**
 * The options the user can pick. A read-only selection offers only the selected option.
 */
fun Category.selectableOptions(
    orgUnitUid: String?,
    date: LocalDate?,
    isReadOnly: Boolean,
): List<CategoryOption> =
    if (isReadOnly) {
        listOfNotNull(selectedOption)
    } else {
        options.filter { it.isSelectable(orgUnitUid, date) }
    }

/**
 * Why the selected option can no longer be picked, or `null` when it can or nothing is selected.
 * A date out of range wins over an org unit the option is not available in.
 */
fun Category.selectionError(
    orgUnitUid: String?,
    date: LocalDate?,
    isReadOnly: Boolean,
): CategorySelectionError? {
    val option = selectedOption ?: return null
    return when {
        option in selectableOptions(orgUnitUid, date, isReadOnly) -> null
        !option.isValidOn(date) -> CategorySelectionError.OUT_OF_DATE_RANGE
        else -> CategorySelectionError.NOT_IN_ORG_UNIT
    }
}

/**
 * Why there is no option to pick, or `null` when there is at least one.
 */
fun Category.noOptionsReason(
    orgUnitUid: String?,
    date: LocalDate?,
    isReadOnly: Boolean,
): NoOptionsReason? {
    val writableOptions = options.filter { it.canWrite }
    return when {
        selectableOptions(orgUnitUid, date, isReadOnly).isNotEmpty() -> null
        writableOptions.isEmpty() -> NoOptionsReason.NO_ACCESS
        writableOptions.none { it.isAvailableIn(orgUnitUid) } -> NoOptionsReason.NOT_IN_ORG_UNIT
        else -> NoOptionsReason.NOT_VALID_FOR_DATE
    }
}

/**
 * Whether every category has a selected option the user can keep.
 */
fun List<Category>.isCompleted(
    orgUnitUid: String?,
    date: LocalDate?,
): Boolean {
    val isReadOnly = isReadOnly()
    return all { category ->
        category.selectedOption?.let { it in category.selectableOptions(orgUnitUid, date, isReadOnly) } == true
    }
}
