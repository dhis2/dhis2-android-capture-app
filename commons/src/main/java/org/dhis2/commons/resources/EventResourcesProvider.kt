package org.dhis2.commons.resources

import androidx.annotation.StringRes
import org.dhis2.mobile.commons.providers.CustomLabelContext
import org.dhis2.mobile.commons.providers.CustomLabelProvider
import org.dhis2.mobile.commons.providers.formatWithTerm
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
            keepConfiguredCase = true,
        ).formatLabel(
            stringResource,
            quantity,
            formatWithQuantity,
        )

    fun programEventLabel(
        customLabelContext: CustomLabelContext? = null,
        quantity: Int = 1,
        keepConfiguredCase: Boolean = false,
    ) = customLabelProvider.blockingCustomEventLabel(
        customLabelContext,
        quantity,
        keepConfiguredCase,
    )

    fun formatWithProgramEventLabel(
        @StringRes stringResource: Int,
        programUid: String? = null,
        quantity: Int = 1,
        formatWithQuantity: Boolean = false,
    ): String =
        programEventLabel(
            customLabelContext = programUid?.let { CustomLabelContext.Program(it) },
            quantity = quantity,
            keepConfiguredCase = true,
        ).formatLabel(
            stringResource,
            quantity,
            formatWithQuantity,
        )

    private fun String.formatLabel(
        @StringRes stringResource: Int,
        quantity: Int = 1,
        formatWithQuantity: Boolean = false,
    ): String =
        resourceManager
            .getString(stringResource)
            .formatWithTerm(this, quantity.takeIf { formatWithQuantity })
}
