package org.dhis2.data.forms

import org.hisp.dhis.rules.api.RuleEngineContext
import org.hisp.dhis.rules.models.RuleEnrollment
import org.hisp.dhis.rules.models.RuleEvent

data class RuleEngineContextData(
    val ruleEngineContext: RuleEngineContext,
    val ruleEnrollment: RuleEnrollment?,
    val ruleEvents: List<RuleEvent>
)