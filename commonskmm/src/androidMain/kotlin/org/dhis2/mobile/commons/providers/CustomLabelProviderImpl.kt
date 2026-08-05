package org.dhis2.mobile.commons.providers

import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import org.dhis2.mobile.commons.resources.Res
import org.dhis2.mobile.commons.resources.enrollment
import org.dhis2.mobile.commons.resources.event
import org.dhis2.mobile.commons.resources.follow_up
import org.dhis2.mobile.commons.resources.mark_for_follow_up
import org.dhis2.mobile.commons.resources.marked_for_follow_up
import org.dhis2.mobile.commons.resources.org_unit
import org.dhis2.mobile.commons.resources.tei
import org.hisp.dhis.android.core.D2
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString

class CustomLabelProviderImpl(
    private val d2: D2,
) : CustomLabelProvider {
    private suspend fun execute(
        capitalizeFirstLetter: Boolean,
        defaultLabel: suspend () -> String,
        block: suspend () -> String?,
    ): String {
        val customLabel =
            try {
                block()?.capitalize(Locale.current)
            } catch (e: Exception) {
                null
            }
        return customLabel ?: defaultLabel().let {
            if (capitalizeFirstLetter) it.capitalize(Locale.current) else it
        }
    }

    private suspend fun execute(
        defaultResource: PluralStringResource,
        quantity: Int?,
        capitalizeFirstLetter: Boolean,
        block: suspend () -> String?,
    ): String =
        execute(
            capitalizeFirstLetter = capitalizeFirstLetter,
            defaultLabel = { getPluralString(defaultResource, quantity ?: 1) },
            block = block,
        )

    private suspend fun execute(
        defaultResource: StringResource,
        capitalizeFirstLetter: Boolean,
        block: suspend () -> String?,
    ): String =
        execute(
            capitalizeFirstLetter = capitalizeFirstLetter,
            defaultLabel = { getString(defaultResource) },
            block = block,
        )

    override suspend fun getCustomEnrollmentLabel(
        programUid: String?,
        quantity: Int?,
    ): String =
        execute(
            defaultResource = Res.plurals.enrollment,
            quantity = quantity,
            capitalizeFirstLetter = false,
        ) {
            d2
                .programModule()
                .programs()
                .uid(programUid)
                .blockingGet()
                ?.displayEnrollmentLabel()
        }

    override suspend fun getTeTypeCustomLabel(
        teTypeUid: String,
        isPlural: Boolean,
    ): String =
        execute(
            defaultResource = Res.plurals.tei,
            quantity = if (isPlural) 2 else 1,
            capitalizeFirstLetter = true,
        ) {
            if (isPlural) {
                d2
                    .trackedEntityModule()
                    .trackedEntityTypes()
                    .uid(teTypeUid)
                    .blockingGet()
                    ?.displayTrackedEntityTypesLabel
            } else {
                d2
                    .trackedEntityModule()
                    .trackedEntityTypes()
                    .uid(teTypeUid)
                    .blockingGet()
                    ?.displayName
            }
        }

    override suspend fun getCustomOrgUnitLabel(
        programUid: String?,
        quantity: Int?,
        capitalizeFirstLetter: Boolean,
    ): String =
        execute(
            defaultResource = Res.plurals.org_unit,
            quantity = quantity,
            capitalizeFirstLetter = capitalizeFirstLetter,
        ) {
            d2
                .programModule()
                .programs()
                .uid(programUid)
                .blockingGet()
                ?.displayOrgUnitLabel()
        }

    override suspend fun getCustomFollowUpLabel(
        programUid: String?,
        capitalizeFirstLetter: Boolean,
    ): String =
        execute(
            defaultResource = Res.string.follow_up,
            capitalizeFirstLetter = capitalizeFirstLetter,
        ) {
            d2
                .programModule()
                .programs()
                .uid(programUid)
                .blockingGet()
                ?.displayFollowUpLabel
        }

    override suspend fun getCustomMarkedForFollowUpLabel(programUid: String?): String {
        val followUpCustomLabel = getCustomFollowUpLabel(programUid)
        return getString(Res.string.marked_for_follow_up).format(followUpCustomLabel)
    }

    override suspend fun getCustomMarkForFollowUpLabel(programUid: String?): String {
        val followUpCustomLabel = getCustomFollowUpLabel(programUid)
        return getString(Res.string.mark_for_follow_up).format(followUpCustomLabel)
    }

    override suspend fun getCustomEventLabel(
        programUid: String?,
        quantity: Int?,
    ) = execute(
        defaultResource = Res.plurals.event,
        quantity = quantity,
        capitalizeFirstLetter = false,
    ) {
        d2
            .programModule()
            .programs()
            .uid(programUid)
            .blockingGet()
            ?.let {
                if (quantity != null && quantity > 1) it.displayEventsLabel else it.displayEventLabel
            }
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
