package org.dhis2.Bindings

import android.content.Context
import org.dhis2.R
import org.hisp.dhis.android.core.common.ValueType

fun ValueType.toHint(context: Context): String {
    return when (this) {
        ValueType.TEXT -> context.getString(R.string.enter_text)
        ValueType.LONG_TEXT -> context.getString(R.string.enter_long_text)
        ValueType.LETTER -> context.getString(R.string.enter_letter)
        ValueType.NUMBER -> context.getString(R.string.enter_number)
        ValueType.UNIT_INTERVAL -> context.getString(R.string.enter_unit_interval)
        ValueType.PERCENTAGE -> context.getString(R.string.enter_percentage)
        ValueType.INTEGER -> context.getString(R.string.enter_number)
        ValueType.INTEGER_POSITIVE -> context.getString(R.string.enter_positive_integer)
        ValueType.INTEGER_NEGATIVE -> context.getString(R.string.enter_negative_integer)
        ValueType.INTEGER_ZERO_OR_POSITIVE ->
            context.getString(R.string.enter_positive_integer_or_zero)
        ValueType.PHONE_NUMBER -> context.getString(R.string.enter_phone_number)
        ValueType.EMAIL -> context.getString(R.string.enter_email)
        ValueType.ORGANISATION_UNIT -> context.getString(R.string.choose_ou)
        ValueType.URL -> context.getString(R.string.enter_url)
        ValueType.FILE_RESOURCE,
        ValueType.COORDINATE,
        ValueType.USERNAME,
        ValueType.TRACKER_ASSOCIATE,
        ValueType.AGE,
        ValueType.IMAGE,
        ValueType.BOOLEAN,
        ValueType.TRUE_ONLY,
        ValueType.DATE,
        ValueType.DATETIME,
        ValueType.TIME -> context.getString(R.string.enter_value)
    }
}

fun Context.valueTypeHintMap(): Map<ValueType, String> {
    return ValueType.values().map {
        it to it.toHint(this)
    }.toMap()
}
