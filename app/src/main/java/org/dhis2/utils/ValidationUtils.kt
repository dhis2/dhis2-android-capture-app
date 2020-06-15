package org.dhis2.utils

import java.util.Locale
import org.hisp.dhis.android.core.common.ValueType

class ValidationUtils {

    companion object {

        @JvmStatic
        fun validate(valueType: ValueType, value: String?): String? {
            return value?.let {
                when (valueType) {
                    ValueType.INTEGER,
                    ValueType.INTEGER_NEGATIVE,
                    ValueType.INTEGER_POSITIVE,
                    ValueType.INTEGER_ZERO_OR_POSITIVE,
                    ValueType.PERCENTAGE -> String.format(Locale.US, "%.0f", it.toFloat())
                    ValueType.UNIT_INTERVAL -> it.toFloat().toString()
                    ValueType.NUMBER -> it.toDouble().toString()
                    else -> it
                }
            }
        }
    }
}
