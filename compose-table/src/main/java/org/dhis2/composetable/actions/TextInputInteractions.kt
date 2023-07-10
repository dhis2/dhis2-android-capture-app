package org.dhis2.composetable.actions

import org.dhis2.composetable.model.TextInputModel

interface TextInputInteractions {
    fun onTextChanged(inputTextValue: String) = run {}
    fun onSave() = run {}
    fun onNextSelected() = run {}
}
