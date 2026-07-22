package org.dhis2.mobile.commons.providers

import kotlinx.coroutines.runBlocking

interface CustomLabelProvider {
    suspend fun getCustomEnrollmentLabel(
        programUid: String?,
        quantity: Int? = null,
    ): String

    suspend fun getCustomOrgUnitLabel(
        programUid: String?,
        quantity: Int? = null,
        capitalizeFirstLetter: Boolean = true,
    ): String

    fun formatStringWithCustomLabel(
        stringResource: String,
        customLabel: String,
        quantity: Int? = null,
    ): String
}

fun CustomLabelProvider.getCustomOrgUnitLabelBlocking(
    programUid: String?,
    capitalizeFirstLetter: Boolean = true,
): String =
    runBlocking {
        getCustomOrgUnitLabel(programUid, null, capitalizeFirstLetter)
    }
