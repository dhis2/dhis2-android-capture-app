package org.dhis2.mobile.commons.category.domain.model

import kotlinx.datetime.LocalDate

/**
 * An option of a [Category]. It is valid from [startDate] to [endDate], both included.
 */
data class CategoryOption(
    val uid: String,
    val name: String,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val canWrite: Boolean,
    val orgUnitRestriction: OrgUnitRestriction,
)

/**
 * The org units a [CategoryOption] is available in.
 */
sealed interface OrgUnitRestriction {
    /** The option is available in every org unit. */
    data object Unrestricted : OrgUnitRestriction

    /**
     * The option is available only in [orgUnitUids]. An empty set means the option is assigned
     * only to org units the user cannot access, so it is available nowhere.
     */
    data class RestrictedTo(
        val orgUnitUids: Set<String>,
    ) : OrgUnitRestriction
}
