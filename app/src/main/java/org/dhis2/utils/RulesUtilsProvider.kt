package org.dhis2.utils

import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.rules.models.RuleEffect

interface RulesUtilsProvider {

    fun applyRuleEffects(
        fieldViewModels: MutableMap<String, FieldViewModel>,
        calcResult: Result<RuleEffect>,
        rulesActionCallbacks: RulesActionCallbacks
    )

    fun applyRuleEffects(
        programStages: MutableMap<String, ProgramStage>,
        calcResult: Result<RuleEffect>
    )
}
