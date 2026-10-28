package org.dhis2.mobile.commons.providers

import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import org.dhis2.mobile.commons.resources.Res
import org.dhis2.mobile.commons.resources.enrollment
import org.dhis2.mobile.commons.resources.event
import org.dhis2.mobile.commons.resources.follow_up
import org.dhis2.mobile.commons.resources.group_by_stage_label
import org.dhis2.mobile.commons.resources.mark_for_follow_up
import org.dhis2.mobile.commons.resources.marked_for_follow_up
import org.dhis2.mobile.commons.resources.org_unit
import org.dhis2.mobile.commons.resources.program_stage
import org.dhis2.mobile.commons.resources.relationship
import org.dhis2.mobile.commons.resources.stage
import org.dhis2.mobile.commons.resources.tei
import org.hisp.dhis.android.core.D2
import org.jetbrains.compose.resources.PluralStringResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getPluralString
import org.jetbrains.compose.resources.getString

class CustomLabelProviderImpl(
    private val d2: D2,
) : CustomLabelProvider {
    private val Int?.isPlural: Boolean
        get() = this != null && this > 1

    private suspend fun execute(
        keepConfiguredCase: Boolean,
        defaultLabel: suspend () -> String,
        block: suspend () -> String?,
    ): String {
        val customLabel =
            try {
                block()
            } catch (_: Exception) {
                null
            }
        return (customLabel ?: defaultLabel()).let {
            if (keepConfiguredCase) it else it.capitalize(Locale.current)
        }
    }

    private suspend fun execute(
        defaultResource: PluralStringResource,
        quantity: Int?,
        keepConfiguredCase: Boolean,
        block: suspend () -> String?,
    ): String =
        execute(
            keepConfiguredCase = keepConfiguredCase,
            defaultLabel = { getPluralString(defaultResource, quantity ?: 1) },
            block = block,
        )

    private suspend fun execute(
        defaultResource: StringResource,
        keepConfiguredCase: Boolean,
        block: suspend () -> String?,
    ): String =
        execute(
            keepConfiguredCase = keepConfiguredCase,
            defaultLabel = { getString(defaultResource) },
            block = block,
        )

    override suspend fun getCustomEnrollmentLabel(
        programUid: String?,
        quantity: Int?,
        keepConfiguredCase: Boolean,
    ): String =
        execute(
            defaultResource = Res.plurals.enrollment,
            quantity = quantity,
            keepConfiguredCase = keepConfiguredCase,
        ) {
            if (quantity.isPlural) {
                d2
                    .programModule()
                    .programs()
                    .uid(programUid)
                    .blockingGet()
                    ?.displayEnrollmentsLabel()
            } else {
                d2
                    .programModule()
                    .programs()
                    .uid(programUid)
                    .blockingGet()
                    ?.displayEnrollmentLabel()
            }
        }

    override suspend fun getTeTypeCustomLabel(
        teTypeUid: String,
        isPlural: Boolean,
        keepConfiguredCase: Boolean,
    ): String =
        execute(
            defaultResource = Res.plurals.tei,
            quantity = if (isPlural) 2 else 1,
            keepConfiguredCase = keepConfiguredCase,
        ) {
            val teType =
                d2
                    .trackedEntityModule()
                    .trackedEntityTypes()
                    .uid(teTypeUid)
                    .blockingGet()
            if (isPlural) {
                teType?.displayTrackedEntityTypesLabel ?: teType?.displayName
            } else {
                teType?.displayName
            }
        }

    override suspend fun getCustomOrgUnitLabel(
        programUid: String?,
        quantity: Int?,
        keepConfiguredCase: Boolean,
    ): String =
        execute(
            defaultResource = Res.plurals.org_unit,
            quantity = quantity,
            keepConfiguredCase = keepConfiguredCase,
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
        keepConfiguredCase: Boolean,
    ): String =
        execute(
            defaultResource = Res.string.follow_up,
            keepConfiguredCase = keepConfiguredCase,
        ) {
            d2
                .programModule()
                .programs()
                .uid(programUid)
                .blockingGet()
                ?.displayFollowUpLabel
        }

    override suspend fun getCustomMarkedForFollowUpLabel(programUid: String?): String {
        val followUpCustomLabel = getCustomFollowUpLabel(programUid, keepConfiguredCase = true)
        return getString(Res.string.marked_for_follow_up).formatWithTerm(followUpCustomLabel)
    }

    override suspend fun getCustomMarkForFollowUpLabel(programUid: String?): String {
        val followUpCustomLabel = getCustomFollowUpLabel(programUid, keepConfiguredCase = true)
        return getString(Res.string.mark_for_follow_up).formatWithTerm(followUpCustomLabel)
    }

    private fun eventLabel(
        quantity: Int?,
        singular: String?,
        plural: String?,
    ) = if (quantity.isPlural) plural else singular

    private fun programEventLabel(
        programUid: String,
        quantity: Int?,
    ) = d2
        .programModule()
        .programs()
        .uid(programUid)
        .blockingGet()
        ?.let { eventLabel(quantity, it.displayEventLabel, it.displayEventsLabel) }

    private fun programStageEventLabel(
        programStageUid: String,
        quantity: Int?,
    ) = d2
        .programModule()
        .programStages()
        .uid(programStageUid)
        .blockingGet()
        ?.let { eventLabel(quantity, it.displayEventLabel, it.displayEventsLabel) }

    override suspend fun getCustomEventLabel(
        customLabelContext: CustomLabelContext?,
        quantity: Int?,
        keepConfiguredCase: Boolean,
    ) = execute(
        defaultResource = Res.plurals.event,
        quantity = quantity,
        keepConfiguredCase = keepConfiguredCase,
    ) {
        when (customLabelContext) {
            null -> null

            is CustomLabelContext.Program ->
                programEventLabel(customLabelContext.programUid, quantity)

            is CustomLabelContext.ProgramStage ->
                programStageEventLabel(customLabelContext.programStageUid, quantity)
                    ?: programEventLabel(customLabelContext.programUid, quantity)
        }
    }

    override suspend fun getCustomProgramStageLabel(
        programUid: String?,
        keepConfiguredCase: Boolean,
        defaultResource: StringResource?,
    ): String =
        execute(
            defaultResource = defaultResource ?: Res.string.program_stage,
            keepConfiguredCase = keepConfiguredCase,
        ) {
            d2
                .programModule()
                .programs()
                .uid(programUid)
                .blockingGet()
                ?.displayProgramStageLabel
        }

    override suspend fun getCustomGroupByStageLabel(programUid: String?): String {
        val customProgramStageLabel =
            getCustomProgramStageLabel(
                programUid = programUid,
                keepConfiguredCase = true,
                defaultResource = Res.string.stage,
            )
        return getString(Res.string.group_by_stage_label).formatWithTerm(customProgramStageLabel)
    }

    override suspend fun getCustomRelationshipLabel(
        programUid: String?,
        quantity: Int?,
        keepConfiguredCase: Boolean,
    ): String =
        execute(
            Res.plurals.relationship,
            quantity = quantity,
            keepConfiguredCase = keepConfiguredCase,
        ) {
            d2
                .programModule()
                .programs()
                .uid(programUid)
                .blockingGet()
                ?.displayRelationshipLabel
        }

    override fun formatStringWithCustomLabel(
        stringResource: String,
        customLabel: String,
        quantity: Int?,
    ): String = stringResource.formatWithTerm(customLabel, quantity)
}
