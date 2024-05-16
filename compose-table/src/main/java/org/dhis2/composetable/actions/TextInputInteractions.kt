package org.dhis2.composetable.actions

import org.dhis2.composetable.model.TextInputModel

interface TextInputInteractions {
    fun onTextChanged(textInputModel: TextInputModel) = run {}
    fun onSave() = run {}
    fun onNextSelected() = run {}
}
