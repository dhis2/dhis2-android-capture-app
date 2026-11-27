package org.dhis2.mobile.commons.category.data

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.dhis2.mobile.commons.category.domain.model.Category
import org.dhis2.mobile.commons.category.domain.model.CategoryOption
import org.dhis2.mobile.commons.category.domain.model.OrgUnitRestriction
import org.dhis2.mobile.commons.extensions.toKtxInstant
import java.util.Date
import org.hisp.dhis.android.core.category.Category as SdkCategory
import org.hisp.dhis.android.core.category.CategoryOption as SdkCategoryOption

fun SdkCategory.toDomain(
    options: List<CategoryOption>,
    selectedOptionUid: String? = null,
): Category =
    Category(
        uid = uid(),
        name = displayName() ?: uid(),
        options = options,
        selectedOptionUid = selectedOptionUid,
    )

/**
 * Maps an SDK option. The org units must be loaded (`withOrganisationUnits()`) for the
 * restriction to be right: the SDK uses `null` for an unrestricted option and an empty list
 * for an option assigned only to org units the user cannot access.
 */
fun SdkCategoryOption.toDomain(): CategoryOption =
    CategoryOption(
        uid = uid(),
        name = displayName() ?: code() ?: "",
        startDate = startDate()?.toLocalDate(),
        endDate = endDate()?.toLocalDate(),
        canWrite = access()?.data()?.write() == true,
        orgUnitRestriction =
            organisationUnits()?.let { orgUnits ->
                OrgUnitRestriction.RestrictedTo(orgUnits.map { it.uid() }.toSet())
            } ?: OrgUnitRestriction.Unrestricted,
    )

fun Date.toLocalDate(): LocalDate = toKtxInstant().toLocalDateTime(TimeZone.currentSystemDefault()).date
