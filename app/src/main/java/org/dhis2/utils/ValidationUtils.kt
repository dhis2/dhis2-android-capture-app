package org.dhis2.utils

import org.hisp.dhis.android.core.common.ValueType

class ValidationUtils {

    companion object {

        @JvmStatic
        fun validate(valueType: ValueType, value: String?): String? {
            return value?.let {
                if (value == "-") {
                    "0"
                } else {
                    when (valueType) {
                        ValueType.INTEGER,
                        ValueType.INTEGER_NEGATIVE,
                        ValueType.INTEGER_POSITIVE,
                        ValueType.INTEGER_ZERO_OR_POSITIVE,
                        ValueType.PERCENTAGE,
                        -> it.toIntOrNull()?.toString() ?: value
                        ValueType.UNIT_INTERVAL -> it.toFloatOrNull()?.toString() ?: value
                        ValueType.NUMBER ->
                            if (it.toDouble() % 1.toDouble() == 0.toDouble()) {
                                it.toIntOrNull()?.toString() ?: value
                            } else {
                                it.toDouble().toString()
                            }
                        else -> it
                    }
                }
            }
        }
    }
}
