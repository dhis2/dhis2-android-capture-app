package org.dhis2.commons.bindings

import org.hisp.dhis.android.core.common.ValueType

fun String.formatData(valueType: ValueType? = null): String? {
    return when {
        valueType?.isInteger == true -> formatInteger(this)
        valueType?.isDecimal == true -> formatDecimal(this)
        valueType?.isBoolean == true -> formatBoolean(this)
        else -> this
    }
}

private fun formatInteger(value: String): String? {
    return value.toDoubleOrNull()?.toInt()?.toString()
}

private fun formatDecimal(value: String): String? {
    return value.toDoubleOrNull()?.toString()
}

private fun formatBoolean(value: String): String? {
    return when (value) {
        "1" -> true
        "0" -> false
        else -> value.toBooleanStrictOrNull()
    }?.toString()
}
