package org.dhis2.mobile.commons.extensions

import org.dhis2.mobile.commons.data.getValueInfo
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue

suspend fun TrackedEntityDataValue?.userFriendlyValue(addPercentageSymbol: Boolean = true): String? =
    this?.value()?.let { value ->
        val uid = dataElement()
        if (value.isEmpty() || uid == null) value else value.userFriendlyValue(uid, addPercentageSymbol)
    }

suspend fun TrackedEntityAttributeValue?.userFriendlyValue(addPercentageSymbol: Boolean = true): String? =
    this?.value()?.let { value ->
        val uid = trackedEntityAttribute()
        if (value.isEmpty() || uid == null) value else value.userFriendlyValue(uid, addPercentageSymbol)
    }

/**
 * Formats this value for display when the field's [valueType] and [optionSetUid] are already
 * known, so the field is not looked up by uid. Use it for fields that are neither a data element
 * nor a tracked entity attribute, such as the enrollment and event detail fields of a form.
 */
suspend fun String.userFriendlyValue(
    valueType: ValueType?,
    optionSetUid: String?,
    addPercentageSymbol: Boolean = true,
): String =
    toUserFriendlyValue(addPercentageSymbol) { value ->
        getValueInfo(
            valueType,
            optionSetUid,
            value,
        )
    }
