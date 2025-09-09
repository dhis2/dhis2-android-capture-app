package org.dhis2.mobile.commons.data

import org.dhis2.mobile.commons.model.internal.ValueInfo

interface ValueParser {
    suspend fun getValueInfo(
        uid: String,
        value: String,
    ): ValueInfo

    suspend fun valueFromMultiTextAsOptionNames(
        optionSetUid: String,
        value: String,
    ): String

    suspend fun valueFromOptionSetAsOptionName(
        optionSetUid: String,
        value: String,
    ): String

    suspend fun valueFromOrgUnitAsOrgUnitName(value: String): String

    suspend fun valueToFileName(value: String): String

    suspend fun valueFromCoordinateAsLatLong(value: String): String

    suspend fun valueFromBooleanType(value: String): String
}
