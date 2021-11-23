package org.dhis2.utils

import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.form.model.FieldUiModel
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.rules.models.RuleEffect

interface RulesUtilsProvider {

    fun applyRuleEffects(
        applyForEvent: Boolean,
        fieldViewModels: MutableMap<String, FieldUiModel>,
        calcResult: Result<RuleEffect>,
        valueStore: ValueStore?
    ): RuleUtilsProviderResult

    fun applyRuleEffects(
        programStages: MutableMap<String, ProgramStage>,
        calcResult: Result<RuleEffect>
    )
}
