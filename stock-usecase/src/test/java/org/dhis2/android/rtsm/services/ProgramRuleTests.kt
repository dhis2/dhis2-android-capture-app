package org.dhis2.android.rtsm.services

import com.google.common.collect.Lists
import org.dhis2.commons.rules.toRuleEngineInstant
import org.dhis2.commons.rules.toRuleEngineLocalDate
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.rules.api.RuleEngine
import org.hisp.dhis.rules.api.RuleEngineContext
import org.hisp.dhis.rules.models.Rule
import org.hisp.dhis.rules.models.RuleAction
import org.hisp.dhis.rules.models.RuleDataValue
import org.hisp.dhis.rules.models.RuleEnrollment
import org.hisp.dhis.rules.models.RuleEnrollmentStatus
import org.hisp.dhis.rules.models.RuleEvent
import org.hisp.dhis.rules.models.RuleEventStatus
import org.hisp.dhis.rules.models.RuleValueType
import org.hisp.dhis.rules.models.RuleVariable
import org.hisp.dhis.rules.models.RuleVariableCurrentEvent
import org.hisp.dhis.rules.models.RuleVariablePreviousEvent
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.Date

@RunWith(JUnit4::class)
class ProgramRuleTests {
    /**
     * Gets the rule engine with all the rule variables configured in the server
     */
    private fun getRuleEngineContext(rules: List<Rule>): RuleEngineContext {
        // Variable used by the program rules in the program rule expressions and actions
        val rulesVariables = listOf<RuleVariable>(
            RuleVariablePreviousEvent(
                "PSM- Initial stock on hand - Previous event",
                false,
                emptyList(),
                "ypCQAFr1a5l",
                RuleValueType.NUMERIC,
            ),
            RuleVariableCurrentEvent(
                "PSM- Previous stock balance",
                false,
                emptyList(),
                "oc8tn8CewiP",
                RuleValueType.NUMERIC,
            ),
            RuleVariableCurrentEvent(
                "PSM- Stock consumed distributed",
                false,
                emptyList(),
                "lpGYJoVUudr",
                RuleValueType.NUMERIC,
            ),
            RuleVariableCurrentEvent(
                "PSM- Stock corrected",
                false,
                emptyList(),
                "ej1YwWaYGmm",
                RuleValueType.NUMERIC,
            ),
            RuleVariableCurrentEvent(
                "PSM- Stock discarded",
                false,
                emptyList(),
                "I7cmT3iXT0y",
                RuleValueType.NUMERIC,
            ),
            RuleVariableCurrentEvent(
                "PSM- Stock received",
                false,
                emptyList(),
                "j3ydinp6Qp8",
                RuleValueType.NUMERIC,
            ),
        )
        return RuleEngineContext(
            rules,
            rulesVariables,
            emptyMap(),
            emptyMap(),
        )
    }

    /**
     * Create 3 the tree program rules configured in th server
     *
     * Rule 1: PSM- Assign Stock on Hand
     * Expression: true
     * Actions: assign value to Data element PSM-Stock on hand
     *
     * //TODO Create the two remaining program rules
     */
    private fun createRules(): List<Rule> {
        val rules = Lists.newArrayList<Rule>()
        // Rule 1: PSM- Assign Stock on Hand
        val assignAction =
            RuleAction(
                "#{PSM- Previous stock balance} + " +
                    "#{PSM- Stock received} - #{PSM- Stock consumed distributed} -" +
                    " #{PSM- Stock discarded} - #{PSM- Stock corrected}",
                ProgramRuleActionType.ASSIGN.name,
                mapOf(Pair("field", "ypCQAFr1a5l")),
            )
        val rule1 = Rule(
            "true",
            listOf(assignAction),
            "rule1Uid",
            "PSM- Assign Stock on Hand",
            null,
            1,
        )
        rules.add(rule1)

        // TODO Add the two remaining program rules

        return rules
    }

    @Test
    @Throws(Exception::class)
    fun evaluateTOneRuleTest() {
        val ruleEngineContext = getRuleEngineContext(createRules())
        val enrollment = RuleEnrollment(
            "test_enrollment",
            "test_program",
            Date().toRuleEngineLocalDate(),
            Date().toRuleEngineLocalDate(),
            RuleEnrollmentStatus.ACTIVE,
            "test_ou",
            "test_ou_code",
            emptyList(),
        )

        val ruleEvent = RuleEvent(
            "test_event",
            "test_program_stage",
            "",
            RuleEventStatus.ACTIVE,
            Date().toRuleEngineInstant(),
            Date().toRuleEngineLocalDate(),
            null,
            "",
            "",
            listOf(
                // PRevious Stock Balance
                RuleDataValue(
                    Date().toRuleEngineInstant(),
                    "",
                    "oc8tn8CewiP",
                    "3",
                ),
                // PSM Stock received
                RuleDataValue(
                    Date().toRuleEngineInstant(),
                    "",
                    "j3ydinp6Qp8",
                    "4",
                ),
                // PSM- Stock consumed distributed
                RuleDataValue(
                    Date().toRuleEngineInstant(),
                    "",
                    "lpGYJoVUudr",
                    "2",
                ),
                // PSM- Stock discarded
                RuleDataValue(
                    Date().toRuleEngineInstant(),
                    "",
                    "I7cmT3iXT0y",
                    "1",
                ),
                // PSM- Stock corrected
                RuleDataValue(
                    Date().toRuleEngineInstant(),
                    "",
                    "ej1YwWaYGmm",
                    "3",
                ),
            ),
        )

        val ruleEffects = RuleEngine.getInstance().evaluate(
            ruleEvent,
            enrollment,
            listOf(),
            ruleEngineContext,
        )
        assertEquals(ruleEffects.find { it.ruleId == "rule1Uid" }?.data, "1")
    }
}
