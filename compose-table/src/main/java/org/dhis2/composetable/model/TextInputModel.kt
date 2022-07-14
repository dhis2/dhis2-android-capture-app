package org.dhis2.composetable.model


data class TextInputModel(
    val dataElementName: String ="",
    val dataElementUid: String = "",
    val categoryOptionComboOptionNames: List<String> = emptyList(),
    val categoryOptionComboUid: String = "",
    val currentValue: String? = null,
    val keyboardInputType: KeyboardInputType = KeyboardInputType.TextInput()
)

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