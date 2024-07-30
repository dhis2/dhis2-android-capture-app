package org.dhis2.commons.resources

import androidx.annotation.StringRes
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import org.dhis2.commons.R
import org.hisp.dhis.android.core.D2

class EventResourcesProvider(
    val d2: D2,
    val resourceManager: ResourceManager,
) {
    private fun programStageEventLabel(
        programStageUid: String? = null,
        programUid: String? = null,
        quantity: Int = 1,
    ) = try {
        d2
            .programModule()
            .programStages()
            .uid(programStageUid)
            .blockingGet()
            ?.displayEventLabel()
    } catch (e: Exception) {
        null
    } ?: programEventLabel(programUid, quantity)

    fun formatWithProgramStageEventLabel(
        @StringRes stringResource: Int,
        programStageUid: String? = null,
        programUid: String?,
        quantity: Int = 1,
        formatWithQuantity: Boolean = false,
    ): String =
        programStageEventLabel(programStageUid, programUid, quantity).formatLabel(
            stringResource,
            quantity,
            formatWithQuantity,
        )

    fun programEventLabel(
        programUid: String? = null,
        quantity: Int = 1,
    ) = try {
        d2
            .programModule()
            .programs()
            .uid(programUid)
            .blockingGet()
            ?.displayEventLabel()
    } catch (e: Exception) {
        null
    } ?: resourceManager.getPlural(R.plurals.event_label, quantity)

    fun formatWithProgramEventLabel(
        @StringRes stringResource: Int,
        programUid: String? = null,
        quantity: Int = 1,
        formatWithQuantity: Boolean = false,
    ): String =
        programEventLabel(programUid, quantity).formatLabel(
            stringResource,
            quantity,
            formatWithQuantity,
        )

    private fun String.formatLabel(
        @StringRes stringResource: Int,
        quantity: Int = 1,
        formatWithQuantity: Boolean = false,
    ): String =
        with(resourceManager.getString(stringResource)) {
            val finalLabel =
                when {
                    startsWith("%s") -> this@formatLabel.capitalize(Locale.current)
                    else -> this@formatLabel
                }
            when {
                formatWithQuantity -> format(quantity, finalLabel)
                else -> format(finalLabel)
            }
        }
}
