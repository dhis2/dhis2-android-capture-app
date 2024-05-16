package org.dhis2.form.data

import org.dhis2.form.model.FieldUiModel
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.rules.models.RuleEffect

interface RulesUtilsProvider {

    fun applyRuleEffects(
        applyForEvent: Boolean,
        fieldViewModels: MutableMap<String, FieldUiModel>,
        calcResult: List<RuleEffect>,
        valueStore: FormValueStore?,
    ): RuleUtilsProviderResult

    fun applyRuleEffects(
        programStages: MutableMap<String, ProgramStage>,
        calcResult: Result<List<RuleEffect>>,
    )
}
