package org.dhis2.utils

import org.dhis2.form.data.FieldUiModel
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.rules.models.RuleEffect

interface RulesUtilsProvider {

    fun applyRuleEffects(
        fieldViewModels: MutableMap<String, FieldUiModel>,
        calcResult: Result<RuleEffect>,
        rulesActionCallbacks: RulesActionCallbacks
    )

    fun applyRuleEffects(
        programStages: MutableMap<String, ProgramStage>,
        calcResult: Result<RuleEffect>
    )
}
