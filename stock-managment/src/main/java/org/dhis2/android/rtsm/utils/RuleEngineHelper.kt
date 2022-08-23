package org.dhis2.android.rtsm.utils

import org.hisp.dhis.rules.RuleEngine
import org.hisp.dhis.rules.RuleEngineContext
import org.hisp.dhis.rules.models.Rule
import org.hisp.dhis.rules.models.RuleEvent
import org.hisp.dhis.rules.models.RuleVariable
import org.hisp.dhis.rules.models.TriggerEnvironment

class RuleEngineHelper {
    companion object {
        @JvmStatic
        fun getRuleEngine(
            rules: List<Rule>,
            ruleVariables: List<RuleVariable>,
            constants: Map<String, String>,
            supplementaryData: Map<String, List<String>>,
            events: List<RuleEvent>
        ): RuleEngine {
            debugRuleEngine(rules, ruleVariables, events)

            return RuleEngineContext.builder()
                .rules(rules)
                .ruleVariables(ruleVariables)
                .constantsValue(constants)
                .supplementaryData(supplementaryData)
                .build()
                .toEngineBuilder()
                .triggerEnvironment(TriggerEnvironment.ANDROIDCLIENT)
                .events(events)
                .build()
        }
    }
}