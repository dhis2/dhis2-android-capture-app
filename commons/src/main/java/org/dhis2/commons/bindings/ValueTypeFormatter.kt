package org.dhis2.commons.bindings

import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.rules.models.RuleEffect

fun RuleEffect.formatData(valueType: ValueType? = null): String? {
    return data()?.let { value ->
        when {
            valueType?.isInteger == true -> formatInteger(value)
            valueType?.isDecimal == true -> formatDecimal(value)
            valueType?.isBoolean == true -> formatBoolean(value)
            else -> value
        }
    }
}

private fun formatInteger(value: String): String {
    return value.toDouble().toInt().toString()
}

private fun formatDecimal(value: String): String {
    return value.toDouble().toString()
}

private fun formatBoolean(value: String): String {
    return when (value) {
        "1" -> true
        "0" -> false
        else -> value.toBoolean()
    }.toString()
}
