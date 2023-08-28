package org.dhis2.composetable.model

import androidx.compose.ui.text.TextRange

data class TextInputModel(
    val id: String = "",
    val mainLabel: String = "",
    val secondaryLabels: List<String> = emptyList(),
    val currentValue: String? = null,
    val keyboardInputType: KeyboardInputType = KeyboardInputType.TextInput(),
    val selection: TextRange? = null,
    val error: String? = null,
    val warning: String? = null,
    private val clearable: Boolean = false,
) {
    fun showClearButton() = clearable && currentValue?.isNotEmpty() == true
    fun errorOrWarningMessage() = error ?: warning
    fun hasErrorOrWarning() = errorOrWarningMessage() != null

    fun actionIconCanBeClicked(hasFocus: Boolean) = hasFocus && error == null
}
