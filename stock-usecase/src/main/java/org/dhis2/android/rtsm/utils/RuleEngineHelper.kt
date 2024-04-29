package org.dhis2.android.rtsm.utils

import org.dhis2.commons.rules.RuleEngineContextData
import org.hisp.dhis.rules.api.RuleEngineContext
import org.hisp.dhis.rules.models.Rule
import org.hisp.dhis.rules.models.RuleEvent
import org.hisp.dhis.rules.models.RuleVariable

class RuleEngineHelper {
    companion object {
        @JvmStatic
        fun getRuleEngine(
            rules: List<Rule>,
            ruleVariables: List<RuleVariable>,
            constants: Map<String, String>,
            supplementaryData: Map<String, List<String>>,
            events: List<RuleEvent>,
        ): RuleEngineContextData {
            debugRuleEngine(rules, ruleVariables, events)

            val ruleEngineContext = RuleEngineContext(
                rules,
                ruleVariables,
                supplementaryData,
                constants,
            )

            return RuleEngineContextData(
                ruleEngineContext = ruleEngineContext,
                ruleEnrollment = null,
                ruleEvents = events,
            )
        }
    }
}
