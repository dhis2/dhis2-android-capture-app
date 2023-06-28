package org.dhis2.composetable.model

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType

data class TextInputModel(
    val id: String = "",
    val mainLabel: String = "",
    val secondaryLabels: List<String> = emptyList(),
    val currentValue: String? = null,
    val keyboardInputType: KeyboardInputType = KeyboardInputType.TextInput(),
    val selection: TextRange? = null,
    val error: String? = null,
    val warning: String? = null,
    private val clearable: Boolean = false
) {
    fun showClearButton() = clearable && currentValue?.isNotEmpty() == true
    fun errorOrWarningMessage() = error ?: warning
    fun hasErrorOrWarning() = errorOrWarningMessage() != null

    fun actionIconCanBeClicked(hasFocus: Boolean) = hasFocus && error == null
}

sealed class KeyboardInputType(
    open val multiline: Boolean = false,
    open val forceCapitalize: Boolean = false
) {
    data class TextInput(
        override val multiline: Boolean = false,
        override val forceCapitalize: Boolean = false
    ) : KeyboardInputType(multiline, forceCapitalize)

    data class NumericInput(
        override val multiline: Boolean = false,
        override val forceCapitalize: Boolean = false,
        val allowDecimal: Boolean = true,
        val allowSigned: Boolean = true
    ) : KeyboardInputType(multiline, forceCapitalize)

    data class NumberPassword(
        override val multiline: Boolean = false,
        override val forceCapitalize: Boolean = false
    ) : KeyboardInputType(multiline, forceCapitalize)

    data class PhoneInput(
        override val multiline: Boolean = false,
        override val forceCapitalize: Boolean = false
    ) : KeyboardInputType(multiline)

    data class EmailInput(
        override val multiline: Boolean = false,
        override val forceCapitalize: Boolean = false
    ) : KeyboardInputType(multiline, forceCapitalize)

    data class URLInput(
        override val multiline: Boolean = false,
        override val forceCapitalize: Boolean = false
    ) : KeyboardInputType(multiline, forceCapitalize)
}

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
