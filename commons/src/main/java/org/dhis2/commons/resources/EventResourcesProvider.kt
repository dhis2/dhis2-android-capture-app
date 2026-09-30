package org.dhis2.commons.resources

import androidx.annotation.StringRes
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import org.dhis2.mobile.commons.providers.CustomLabelContext
import org.dhis2.mobile.commons.providers.CustomLabelProvider
import org.hisp.dhis.android.core.D2

class EventResourcesProvider(
    val d2: D2,
    val resourceManager: ResourceManager,
    val customLabelProvider: CustomLabelProvider,
) {
    fun formatWithProgramStageEventLabel(
        @StringRes stringResource: Int,
        programStageUid: String? = null,
        programUid: String?,
        quantity: Int = 1,
        formatWithQuantity: Boolean = false,
    ): String =
        programEventLabel(
            customLabelContext =
                when {
                    programStageUid != null && programUid != null ->
                        CustomLabelContext.ProgramStage(programStageUid, programUid)

                    programUid != null ->
                        CustomLabelContext.Program(programUid)

                    else -> null
                },
            quantity = quantity,
        ).formatLabel(
            stringResource,
            quantity,
            formatWithQuantity,
        )

    fun programEventLabel(
        customLabelContext: CustomLabelContext? = null,
        quantity: Int = 1,
    ) = customLabelProvider.blockingCustomEventLabel(
        customLabelContext,
        quantity,
    )

    fun formatWithProgramEventLabel(
        @StringRes stringResource: Int,
        programUid: String? = null,
        quantity: Int = 1,
        formatWithQuantity: Boolean = false,
    ): String =
        programEventLabel(programUid?.let { CustomLabelContext.Program(it) }, quantity).formatLabel(
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
