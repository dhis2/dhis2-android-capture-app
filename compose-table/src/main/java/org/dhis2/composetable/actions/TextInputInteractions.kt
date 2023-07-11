package org.dhis2.composetable.actions

interface TextInputInteractions {
    fun onTextChanged(inputTextValue: String) = run {}
    fun onSave() = run {}
    fun onNextSelected() = run {}
}
