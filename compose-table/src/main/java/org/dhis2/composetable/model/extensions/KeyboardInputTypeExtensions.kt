package org.dhis2.composetable.model.extensions

import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import org.dhis2.composetable.model.KeyboardInputType

fun KeyboardInputType.toKeyboardType(): KeyboardType = when (this) {
    is KeyboardInputType.NumericInput -> KeyboardType.Number
    is KeyboardInputType.NumberPassword -> KeyboardType.NumberPassword
    is KeyboardInputType.EmailInput -> KeyboardType.Email
    is KeyboardInputType.TextInput -> KeyboardType.Text
    is KeyboardInputType.PhoneInput -> KeyboardType.Phone
    is KeyboardInputType.URLInput -> KeyboardType.Uri
}

fun KeyboardInputType.keyboardCapitalization() = when {
    forceCapitalize -> KeyboardCapitalization.Characters
    else -> KeyboardCapitalization.None
}
