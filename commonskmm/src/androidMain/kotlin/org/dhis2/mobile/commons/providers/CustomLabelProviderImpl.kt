package org.dhis2.mobile.commons.providers

import org.dhis2.mobile.commons.resources.Res
import org.dhis2.mobile.commons.resources.enrollment
import org.hisp.dhis.android.core.D2
import org.jetbrains.compose.resources.getPluralString

class CustomLabelProviderImpl(
    private val d2: D2,
) : CustomLabelProvider {
    suspend fun getCustomEnrollmentLabel(
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

    fun formatStringWithCustomLabel(
        stringResource: String,
        customLabel: String,
        quantity: Int? = null,
    ): String =
        quantity?.let {
            stringResource.format(it, customLabel)
        } ?: stringResource.format(customLabel)
}
