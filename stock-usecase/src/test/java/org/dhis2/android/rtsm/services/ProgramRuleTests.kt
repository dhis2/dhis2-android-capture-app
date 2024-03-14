package org.dhis2.android.rtsm.services

import com.google.common.collect.Lists
import org.hisp.dhis.rules.RuleEngine
import org.hisp.dhis.rules.RuleEngineContext
import org.hisp.dhis.rules.models.Rule
import org.hisp.dhis.rules.models.RuleAction
import org.hisp.dhis.rules.models.RuleActionAssign
import org.hisp.dhis.rules.models.RuleDataValue
import org.hisp.dhis.rules.models.RuleEnrollment
import org.hisp.dhis.rules.models.RuleEvent
import org.hisp.dhis.rules.models.RuleValueType
import org.hisp.dhis.rules.models.RuleVariable
import org.hisp.dhis.rules.models.RuleVariableCurrentEvent
import org.hisp.dhis.rules.models.RuleVariablePreviousEvent
import org.hisp.dhis.rules.models.TriggerEnvironment
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.Arrays
import java.util.Date

@RunWith(JUnit4::class)
class ProgramRuleTests {
    /**
     * Gets the rule engine with all the rule variables configured in the server
     */
    private fun getRuleEngine(rules: List<Rule>): RuleEngine.Builder {
        // Variable used by the program rules in the program rule expressions and actions
        val rulesVariables = listOf<RuleVariable>(
            RuleVariablePreviousEvent
                .create(
                    "PSM- Initial stock on hand - Previous event",
                    "ypCQAFr1a5l",
                    RuleValueType.NUMERIC,
                    false,
                    emptyList(),
                ),
            RuleVariableCurrentEvent
                .create(
                    "PSM- Previous stock balance",
                    "oc8tn8CewiP",
                    RuleValueType.NUMERIC,
                    false,
                    emptyList(),
                ),
            RuleVariableCurrentEvent
                .create(
                    "PSM- Stock consumed distributed",
                    "lpGYJoVUudr",
                    RuleValueType.NUMERIC,
                    false,
                    emptyList(),
                ),
            RuleVariableCurrentEvent
                .create(
                    "PSM- Stock corrected",
                    "ej1YwWaYGmm",
                    RuleValueType.NUMERIC,
                    false,
                    emptyList(),
                ),
            RuleVariableCurrentEvent
                .create(
                    "PSM- Stock discarded",
                    "I7cmT3iXT0y",
                    RuleValueType.NUMERIC,
                    false,
                    emptyList(),
                ),
            RuleVariableCurrentEvent
                .create(
                    "PSM- Stock received",
                    "j3ydinp6Qp8",
                    RuleValueType.NUMERIC,
                    false,
                    emptyList(),
                ),
        )
        return RuleEngineContext
            .builder()
            .rules(rules)
            .ruleVariables(rulesVariables)
            .supplementaryData(HashMap())
            .constantsValue(HashMap())
            .build().toEngineBuilder().triggerEnvironment(TriggerEnvironment.SERVER)
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
        val assignAction: RuleAction =
            RuleActionAssign.create(
                null,
                "#{PSM- Previous stock balance} + " +
                    "#{PSM- Stock received} - #{PSM- Stock consumed distributed} -" +
                    " #{PSM- Stock discarded} - #{PSM- Stock corrected}",
                "ypCQAFr1a5l",
            )
        val rule1 = Rule
            .create(
                null,
                1,
                "true",
                listOf(assignAction),
                "PSM- Assign Stock on Hand",
                "rule1Uid",
            )
        rules.add(rule1)

        // TODO Add the two remaining program rules

        return rules
    }

    @Test
    @Throws(Exception::class)
    fun evaluateTOneRuleTest() {
        val ruleEngineBuilder = getRuleEngine(createRules())
        val enrollment = RuleEnrollment.builder()
            .enrollment("test_enrollment")
            .programName("test_program")
            .incidentDate(Date())
            .enrollmentDate(Date())
            .status(RuleEnrollment.Status.ACTIVE)
            .organisationUnit("test_ou")
            .organisationUnitCode("test_ou_code")
            .attributeValues(Arrays.asList())
            .build()
        val ruleEvent = RuleEvent.builder()
            .event("test_event")
            .programStage("test_program_stage")
            .programStageName("")
            .status(RuleEvent.Status.ACTIVE)
            .eventDate(Date())
            .dueDate(Date())
            .organisationUnit("")
            .organisationUnitCode("")
            .dataValues(
                listOf(
                    // PRevious Stock Balance
                    RuleDataValue.create(
                        Date(),
                        "",
                        "oc8tn8CewiP",
                        "3",
                    ),
                    // PSM Stock received
                    RuleDataValue.create(
                        Date(),
                        "",
                        "j3ydinp6Qp8",
                        "4",
                    ),
                    // PSM- Stock consumed distributed
                    RuleDataValue.create(
                        Date(),
                        "",
                        "lpGYJoVUudr",
                        "2",
                    ),
                    // PSM- Stock discarded
                    RuleDataValue.create(
                        Date(),
                        "",
                        "I7cmT3iXT0y",
                        "1",
                    ),
                    // PSM- Stock corrected
                    RuleDataValue.create(
                        Date(),
                        "",
                        "ej1YwWaYGmm",
                        "3",
                    ),
                ),
            )
            .build()
        val ruleEngine = ruleEngineBuilder.enrollment(enrollment).build()
        val ruleEffects = ruleEngine.evaluate(ruleEvent).call()
        assertEquals(ruleEffects.find { it.ruleId().equals("rule1Uid") }?.data(), "1")
    }
}
