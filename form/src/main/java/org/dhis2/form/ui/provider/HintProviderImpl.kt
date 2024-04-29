package org.dhis2.form.ui.provider

import android.content.Context
import org.dhis2.form.R
import org.hisp.dhis.android.core.common.ValueType

class HintProviderImpl(val context: Context) : HintProvider {

    override fun provideDateHint(valueType: ValueType) = when (valueType) {
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
        ValueType.URL -> context.getString(R.string.enter_url)
        ValueType.FILE_RESOURCE,
        ValueType.COORDINATE,
        ValueType.USERNAME,
        ValueType.TRACKER_ASSOCIATE,
        ValueType.AGE,
        ValueType.IMAGE,
        ValueType.BOOLEAN,
        ValueType.TRUE_ONLY,
        -> context.getString(R.string.enter_value)
        ValueType.TIME -> context.getString(R.string.select_time)
        ValueType.ORGANISATION_UNIT -> context.getString(R.string.choose_ou)
        ValueType.DATETIME,
        ValueType.DATE,
        -> context.getString(R.string.choose_date)
        ValueType.REFERENCE -> ""
        ValueType.GEOJSON -> ""
        ValueType.MULTI_TEXT -> ""
    }
}
