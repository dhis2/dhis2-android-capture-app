package org.dhis2.mobileProgramRules

import kotlinx.coroutines.test.runTest
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.rules.models.Rule
import org.hisp.dhis.rules.models.RuleAction
import org.hisp.dhis.rules.models.RuleEnrollment
import org.hisp.dhis.rules.models.RuleEnrollmentStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

class RuleEngineHelperTest {
    private val rulesRepository: RulesRepository = mock()

    private fun createFeedbackRule(
        data: String,
        actionType: String,
        priority: Int?,
    ): Rule =
        Rule(
            condition = "true",
            actions =
                listOf(
                    RuleAction(data = "'$data'", type = actionType),
                ),
            uid = data,
            name = "Rule 1",
            programStage = null,
            priority = priority,
        )

    private fun createEnrollment(): RuleEnrollment =
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

    @Test
    fun `Should return effects order by priority`() =
        runTest {
            val rules =
                listOf(
                    createFeedbackRule(
                        data = "Rule1",
                        actionType = ProgramRuleActionType.DISPLAYKEYVALUEPAIR.name,
                        priority = 1,
                    ),
                    createFeedbackRule(
                        data = "Rule2",
                        actionType = ProgramRuleActionType.DISPLAYTEXT.name,
                        priority = 3,
                    ),
                    createFeedbackRule(
                        data = "Rule3",
                        actionType = ProgramRuleActionType.DISPLAYKEYVALUEPAIR.name,
                        priority = null,
                    ),
                    createFeedbackRule(
                        data = "Rule4",
                        actionType = ProgramRuleActionType.DISPLAYKEYVALUEPAIR.name,
                        priority = 2,
                    ),
                )

            whenever(rulesRepository.rules(any(), anyOrNull())) doReturn rules
            whenever(rulesRepository.ruleVariables(any())) doReturn emptyList()
            whenever(rulesRepository.supplementaryData(any())) doReturn emptyMap()
            whenever(rulesRepository.constants()) doReturn emptyMap()
            whenever(rulesRepository.enrollmentEvents(any())) doReturn emptyList()
            whenever(rulesRepository.getRuleEnrollment(any())) doReturn createEnrollment()
            whenever(rulesRepository.queryAttributeValues(any())) doReturn emptyList()
            whenever(rulesRepository.enrollmentProgram(any())) doReturn Pair("program", "orgunit")

            val engineHelper =
                RuleEngineHelper(EvaluationType.Enrollment("enrollment"), rulesRepository)

            val ruleEffects = engineHelper.evaluate()

            assertEquals(
                ruleEffects.map { it.data },
                listOf("Rule1", "Rule4", "Rule2", "Rule3"),
            )
        }
}
