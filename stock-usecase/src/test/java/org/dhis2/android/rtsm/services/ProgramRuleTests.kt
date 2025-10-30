package org.dhis2.android.rtsm.services

import com.google.common.collect.Lists
import org.dhis2.mobileProgramRules.toRuleEngineInstant
import org.dhis2.mobileProgramRules.toRuleEngineInstantWithNoTime
import org.dhis2.mobileProgramRules.toRuleEngineLocalDate
import org.hisp.dhis.android.core.arch.helpers.DateUtils
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
        val rulesVariables =
            listOf<RuleVariable>(
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
        val rule1 =
            Rule(
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
        val enrollment = getEmptyRuleEnrollment()

        val ruleEvent =
            RuleEvent(
                "test_event",
                "test_program_stage",
                "",
                RuleEventStatus.ACTIVE,
                Date().toRuleEngineInstantWithNoTime(),
                Date().toRuleEngineInstant(),
                Date().toRuleEngineLocalDate(),
                null,
                "",
                "",
                listOf(
                    // PRevious Stock Balance
                    RuleDataValue(
                        "oc8tn8CewiP",
                        "3",
                    ),
                    // PSM Stock received
                    RuleDataValue(
                        "j3ydinp6Qp8",
                        "4",
                    ),
                    // PSM- Stock consumed distributed
                    RuleDataValue(
                        "lpGYJoVUudr",
                        "2",
                    ),
                    // PSM- Stock discarded
                    RuleDataValue(
                        "I7cmT3iXT0y",
                        "1",
                    ),
                    // PSM- Stock corrected
                    RuleDataValue(
                        "ej1YwWaYGmm",
                        "3",
                    ),
                ),
            )

        val ruleEffects =
            RuleEngine.getInstance().evaluate(
                ruleEvent,
                enrollment,
                listOf(),
                ruleEngineContext,
            )
        assertEquals(ruleEffects.find { it.ruleId == "rule1Uid" }?.data, "1")
    }

    @Test
    fun evaluateMostRecentEvent() {
        val dataElement1 = "oc8tn8CewiP"
        val dataElement2 = "eEZWHV8OFG0"
        val ruleEventAndroid =
            getRuleEvent(
                uid = "event_android",
                eventDate = "2025-09-25T13:04:13.783",
                created = "2025-09-25T13:04:13.783",
                dataValues =
                    listOf(
                        RuleDataValue(
                            dataElement1,
                            "3",
                        ),
                    ),
            )
        val ruleEventWeb =
            getRuleEvent(
                uid = "event_web",
                eventDate = "2025-09-25T00:00:00.000",
                created = "2025-09-25T15:06:41.483",
                dataValues =
                    listOf(
                        RuleDataValue(
                            dataElement1,
                            "10",
                        ),
                    ),
            )
        val ruleEventNew =
            getRuleEvent(
                uid = "event_new",
                eventDate = "2025-09-25T18:06:41.483",
                created = "2025-09-25T18:06:41.483",
                dataValues = listOf(),
            )

        val ruleEngineContext =
            RuleEngineContext(
                rules =
                    listOf(
                        Rule(
                            condition = "true",
                            actions =
                                listOf(
                                    RuleAction(
                                        data = "#{previousValue}",
                                        type = ProgramRuleActionType.ASSIGN.name,
                                        values =
                                            mapOf(
                                                "field" to dataElement2,
                                            ),
                                    ),
                                ),
                        ),
                    ),
                ruleVariables =
                    listOf(
                        RuleVariablePreviousEvent(
                            name = "previousValue",
                            useCodeForOptionSet = false,
                            options = emptyList(),
                            field = dataElement1,
                            fieldType = RuleValueType.NUMERIC,
                        ),
                    ),
            )

        val ruleEffects =
            RuleEngine.getInstance().evaluate(
                ruleEventNew,
                getEmptyRuleEnrollment(),
                listOf(ruleEventAndroid, ruleEventWeb),
                ruleEngineContext,
            )

        // The rule effect must assigned the value coming from event_web because its created date
        // is most recent compared to event_android.
        assertEquals(ruleEffects.size, 1)
        assertEquals(ruleEffects.first().data, "10")
    }

    private fun getRuleEvent(
        uid: String,
        eventDate: String,
        created: String,
        dataValues: List<RuleDataValue>,
    ): RuleEvent =
        RuleEvent(
            uid,
            "test_program_stage",
            "",
            RuleEventStatus.ACTIVE,
            DateUtils.DATE_FORMAT.parse(eventDate).toRuleEngineInstantWithNoTime(),
            DateUtils.DATE_FORMAT.parse(created).toRuleEngineInstant(),
            null,
            null,
            "",
            "",
            dataValues,
        )

    private fun getEmptyRuleEnrollment(): RuleEnrollment =
        RuleEnrollment(
            "test_enrollment",
            "test_program",
            Date().toRuleEngineLocalDate(),
            Date().toRuleEngineLocalDate(),
            RuleEnrollmentStatus.ACTIVE,
            "test_ou",
            "test_ou_code",
            emptyList(),
        )
}
