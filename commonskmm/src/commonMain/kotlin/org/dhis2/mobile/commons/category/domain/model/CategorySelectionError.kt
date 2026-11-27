package org.dhis2.mobile.commons.category.domain.model

/**
 * Why the option selected for a [Category] can no longer be selected.
 */
enum class CategorySelectionError {
    /** The selected option is not valid on the reference date. */
    OUT_OF_DATE_RANGE,

    /** The selected option is not available in the org unit. */
    NOT_IN_ORG_UNIT,
}

/**
 * Why a [Category] has no option to select, from the most to the least fundamental cause.
 */
enum class NoOptionsReason {
    /** The user can't write data for any option. */
    NO_ACCESS,

    /** None of the writable options is available in the org unit. */
    NOT_IN_ORG_UNIT,

    /** None of the writable options in the org unit is valid on the reference date. */
    NOT_VALID_FOR_DATE,
}
