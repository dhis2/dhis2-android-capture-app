package org.dhis2.usescases.development

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RulesValidationsModelFactory(
    private val ruleValidator: ProgramRulesValidations
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return RulesValidationsModel(ruleValidator) as T
    }
}
