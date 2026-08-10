package org.dhis2.mobile.commons.providers

import kotlinx.coroutines.runBlocking

interface CustomLabelProvider {
    suspend fun getCustomEnrollmentLabel(
        programUid: String?,
        quantity: Int? = null,
    ): String

    suspend fun getTeTypeCustomLabel(
        teTypeUid: String,
        isPlural: Boolean,
    ): String

    suspend fun getCustomOrgUnitLabel(
        programUid: String?,
        quantity: Int? = null,
        capitalizeFirstLetter: Boolean = true,
    ): String

    suspend fun getCustomFollowUpLabel(
        programUid: String?,
        capitalizeFirstLetter: Boolean = true,
    ): String

    suspend fun getCustomMarkedForFollowUpLabel(programUid: String?): String

    suspend fun getCustomMarkForFollowUpLabel(programUid: String?): String

    suspend fun getCustomEventLabel(
        programUid: String?,
        quantity: Int? = null,
    ): String

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
        programUid: String?,
        quantity: Int?,
    ) = runBlocking {
        getCustomEventLabel(programUid, quantity)
    }
}

fun CustomLabelProvider.getCustomOrgUnitLabelBlocking(
    programUid: String?,
    capitalizeFirstLetter: Boolean = true,
): String =
    runBlocking {
        getCustomOrgUnitLabel(programUid, null, capitalizeFirstLetter)
    }
