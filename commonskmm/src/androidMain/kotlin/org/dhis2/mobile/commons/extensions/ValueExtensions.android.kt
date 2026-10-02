package org.dhis2.mobile.commons.extensions

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
