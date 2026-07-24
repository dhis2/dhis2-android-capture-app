package org.dhis2.mobile.commons.providers

import org.dhis2.mobile.commons.resources.Res
import org.dhis2.mobile.commons.resources.enrollment
import org.dhis2.mobile.commons.resources.org_unit
import org.hisp.dhis.android.core.D2
import org.jetbrains.compose.resources.getPluralString

class CustomLabelProviderImpl(
    private val d2: D2,
) : CustomLabelProvider {
    override suspend fun getCustomEnrollmentLabel(
        programUid: String?,
        quantity: Int?,
    ): String =
        try {
            d2
                .programModule()
                .programs()
                .uid(programUid)
                .blockingGet()
                ?.displayEnrollmentLabel()
        } catch (e: Exception) {
            null
        } ?: quantity?.let { getPluralString(Res.plurals.enrollment, it) } ?: getPluralString(Res.plurals.enrollment, 1)

    override suspend fun getCustomOrgUnitLabel(
        programUid: String?,
        quantity: Int?,
        capitalizeFirstLetter: Boolean,
    ): String =
        (
            try {
                d2
                    .programModule()
                    .programs()
                    .uid(programUid)
                    .blockingGet()
                    ?.displayOrgUnitLabel()
            } catch (e: Exception) {
                null
            } ?: quantity?.let { getPluralString(Res.plurals.org_unit, it) } ?: getPluralString(Res.plurals.org_unit, 1)
        ).let { label ->
            if (capitalizeFirstLetter) label.replaceFirstChar { it.uppercase() } else label
        }

    override fun formatStringWithCustomLabel(
        stringResource: String,
        customLabel: String,
        quantity: Int?,
    ): String =
        quantity?.let {
            stringResource.format(it, customLabel)
        } ?: stringResource.format(customLabel)
}
