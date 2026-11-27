package org.dhis2.mobile.commons.providers

import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource

/**
 * Resolves the custom terminology a program defines for default labels (event, enrollment...).
 *
 * Getters return the label capitalized by default, ready to be shown on its own (a title, a
 * chip). Pass `keepConfiguredCase = true` to get the custom or default term exactly as
 * configured; do this whenever the term is inserted into a sentence, and let
 * [formatStringWithCustomLabel] decide its case from its position.
 */
interface CustomLabelProvider {
    suspend fun getCustomEnrollmentLabel(
        programUid: String?,
        quantity: Int? = null,
        keepConfiguredCase: Boolean = false,
    ): String

    suspend fun getTeTypeCustomLabel(
        teTypeUid: String,
        isPlural: Boolean,
        keepConfiguredCase: Boolean = false,
    ): String

    suspend fun getCustomOrgUnitLabel(
        programUid: String?,
        quantity: Int? = null,
        keepConfiguredCase: Boolean = false,
    ): String

    suspend fun getCustomFollowUpLabel(
        programUid: String?,
        keepConfiguredCase: Boolean = false,
    ): String

    suspend fun getCustomMarkedForFollowUpLabel(programUid: String?): String

    suspend fun getCustomMarkForFollowUpLabel(programUid: String?): String

    suspend fun getCustomEventLabel(
        customLabelContext: CustomLabelContext?,
        quantity: Int? = null,
        keepConfiguredCase: Boolean = false,
    ): String

    suspend fun getCustomProgramStageLabel(
        programUid: String?,
        keepConfiguredCase: Boolean = false,
        defaultResource: StringResource? = null,
    ): String

    suspend fun getCustomGroupByStageLabel(programUid: String?): String

    suspend fun getCustomRelationshipLabel(
        programUid: String?,
        quantity: Int?,
        keepConfiguredCase: Boolean = false,
    ): String

    /**
     * Formats [stringResource] with [customLabel], capitalizing the label only when it is the
     * first word of a sentence. Elsewhere the label is inserted unchanged, so fetch it with
     * `keepConfiguredCase = true`.
     */
    fun formatStringWithCustomLabel(
        stringResource: String,
        customLabel: String,
        quantity: Int? = null,
    ): String

    @Deprecated("Use suspend function", replaceWith = ReplaceWith("getCustomFollowUpLabel"))
    fun blockingCustomFollowUpLabel(programUid: String): String =
        runBlocking {
            getCustomFollowUpLabel(programUid)
        }

    @Deprecated(
        "Use suspend function",
        replaceWith = ReplaceWith("getCustomMarkedForFollowUpLabel"),
    )
    fun blockingCustomMarkedForFollowUpLabel(programUid: String?) =
        runBlocking {
            getCustomMarkedForFollowUpLabel(programUid)
        }

    @Deprecated(
        "Use suspend function",
        replaceWith = ReplaceWith("getCustomMarkedForFollowUpLabel"),
    )
    fun blockingCustomMarkForFollowUpLabel(programUid: String?) =
        runBlocking {
            getCustomMarkForFollowUpLabel(programUid)
        }

    @Deprecated(
        "Use suspend function",
        replaceWith = ReplaceWith("getCustomEventLabel"),
    )
    fun blockingCustomEventLabel(
        customLabelContext: CustomLabelContext?,
        quantity: Int?,
        keepConfiguredCase: Boolean = false,
    ) = runBlocking {
        getCustomEventLabel(customLabelContext, quantity, keepConfiguredCase)
    }

    @Deprecated(
        "Use suspend function",
        replaceWith = ReplaceWith("getCustomMarkedForFollowUpLabel"),
    )
    fun blockingCustomEnrollmentLabel(
        programUid: String?,
        quantity: Int? = null,
        keepConfiguredCase: Boolean = false,
    ) = runBlocking {
        getCustomEnrollmentLabel(programUid, quantity, keepConfiguredCase)
    }

    @Deprecated(
        "Use suspend function",
        replaceWith = ReplaceWith("getCustomRelationshipLabel"),
    )
    fun blockingCustomRelationshipLabel(
        programUid: String?,
        quantity: Int?,
        keepConfiguredCase: Boolean = false,
    ) = runBlocking {
        getCustomRelationshipLabel(programUid, quantity, keepConfiguredCase)
    }
}

fun CustomLabelProvider.getCustomOrgUnitLabelBlocking(
    programUid: String?,
    keepConfiguredCase: Boolean = false,
): String =
    runBlocking {
        getCustomOrgUnitLabel(programUid, null, keepConfiguredCase)
    }
