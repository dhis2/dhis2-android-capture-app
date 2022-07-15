package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import org.dhis2.composetable.model.KeyboardInputType
import org.hisp.dhis.android.core.common.ValueType

fun ValueType.toKeyBoardInputType(): KeyboardInputType? {
    return when (this) {
        ValueType.TEXT -> KeyboardInputType.TextInput()
        ValueType.LONG_TEXT -> KeyboardInputType.TextInput(multiline = true)
        ValueType.LETTER -> KeyboardInputType.TextInput(forceCapitalize = true)
        ValueType.PHONE_NUMBER -> KeyboardInputType.PhoneInput()
        ValueType.EMAIL -> KeyboardInputType.EmailInput()
        ValueType.NUMBER -> KeyboardInputType.NumericInput()
        ValueType.UNIT_INTERVAL,
        ValueType.PERCENTAGE -> KeyboardInputType.NumericInput(allowSigned = false)
        ValueType.INTEGER,
        ValueType.INTEGER_NEGATIVE -> KeyboardInputType.NumericInput(
            allowDecimal = false,
            allowSigned = true
        )
        ValueType.INTEGER_POSITIVE,
        ValueType.INTEGER_ZERO_OR_POSITIVE -> KeyboardInputType.NumericInput(
            allowDecimal = false,
            allowSigned = false
        )
        ValueType.URL -> KeyboardInputType.URLInput()
        else -> null
    }
}
