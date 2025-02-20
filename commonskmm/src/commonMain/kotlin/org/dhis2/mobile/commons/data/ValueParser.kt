package org.dhis2.mobile.commons.data

import org.dhis2.mobile.commons.model.internal.ValueInfo

internal interface ValueParser {
    suspend fun getValueInfo(uid: String, value: String): ValueInfo
    suspend fun valueFromOptionSetAsOptionName(optionSetUid: String, value: String): String
    suspend fun valueFromOrgUnitAsOrgUnitName(value: String): String
    suspend fun valueFromFileAsPath(value: String): String
}
