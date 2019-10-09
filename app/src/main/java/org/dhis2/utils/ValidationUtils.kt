package org.dhis2.utils

import org.hisp.dhis.android.core.common.ValueType

import java.util.Locale

class ValidationUtils {

    companion object {

        @JvmStatic
        fun validate(valueType: ValueType, value: String?): String? {
            return value?.let {
                when(valueType) {
                    ValueType.INTEGER, ValueType.INTEGER_NEGATIVE, ValueType.INTEGER_POSITIVE, ValueType.INTEGER_ZERO_OR_POSITIVE, ValueType.PERCENTAGE -> String.format(Locale.US, "%.0f", it.toFloat())
                    ValueType.UNIT_INTERVAL -> it.toFloat().toString()
                    ValueType.NUMBER -> String.format(Locale.US, "%.1f", it.toDouble())
                    else -> it
                }
            }
        }
    }

}
