package org.dhis2.usescases.troubleshooting

import org.dhis2.Bindings.toRuleEngineObject
import org.dhis2.Bindings.toRuleVariableList
import org.dhis2.usescases.development.RuleValidation
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.antlr.Parser
import org.hisp.dhis.antlr.ParserExceptionWithoutContext
import org.hisp.dhis.rules.ItemValueType
import org.hisp.dhis.rules.RuleVariableValue
import org.hisp.dhis.rules.models.RuleAction
import org.hisp.dhis.rules.models.RuleActionHideField
import org.hisp.dhis.rules.models.RuleActionHideOption
import org.hisp.dhis.rules.models.RuleActionHideOptionGroup
import org.hisp.dhis.rules.models.RuleActionHideProgramStage
import org.hisp.dhis.rules.models.RuleActionHideSection
import org.hisp.dhis.rules.models.RuleActionSetMandatoryField
import org.hisp.dhis.rules.models.RuleActionShowOptionGroup
import org.hisp.dhis.rules.models.RuleActionUnsupported
import org.hisp.dhis.rules.models.RuleValueType
import org.hisp.dhis.rules.models.RuleVariableAttribute
import org.hisp.dhis.rules.models.RuleVariableCalculatedValue
import org.hisp.dhis.rules.models.RuleVariableCurrentEvent
import org.hisp.dhis.rules.models.RuleVariableNewestEvent
import org.hisp.dhis.rules.models.RuleVariableNewestStageEvent
import org.hisp.dhis.rules.models.RuleVariablePreviousEvent
import org.hisp.dhis.rules.parser.expression.CommonExpressionVisitor
import org.hisp.dhis.rules.parser.expression.ParserUtils
import org.hisp.dhis.rules.utils.RuleEngineUtils
import java.util.HashMap

class TroubleshootingRepository(val d2:D2) {

    fun validateProgramRules(): List<RuleValidation> {
        return programRules().mapNotNull { programAndRule ->
            val rule = programAndRule.second
            val program = program(programAndRule.first?.uid())
            val valueMap: Map<String, RuleVariableValue?> = ruleVariableMap(program.uid())
            var ruleValidationItem = RuleValidation(rule, program, ruleExternalLink(rule.uid()))
            val ruleConditionResult = process(rule.condition(), valueMap)
            if (ruleConditionResult.isNotEmpty()) {
                ruleValidationItem = ruleValidationItem.copy(conditionError = ruleConditionResult)
            }
            val ruleActionConditions = rule.actions().mapNotNull { ruleAction ->
                if (ruleAction is RuleActionUnsupported) {
                    "%s is not supported".format(ruleAction.actionValueType())
                } else {
                    evaluateAction(ruleAction, valueMap)
                }
            }
            if (ruleActionConditions.isNotEmpty()) {
                ruleValidationItem = ruleValidationItem.copy(actionsError = ruleActionConditions)
            }
            if (ruleValidationItem.hasError()) {
                ruleValidationItem
            } else {
                null
            }
        }.sortedBy { it.program.displayName() }
    }

    private fun program(programUid: String?) =
        d2.programModule().programs().uid(programUid).blockingGet()

    private fun programRules() =
        d2.programModule().programRules().withProgramRuleActions().blockingGet().map {
            Pair(it.program(), it.toRuleEngineObject())
        }

    private fun ruleExternalLink(uid: String) =
        "%s/api/programRules/%s?fields=*,programRuleActions[*]".format(
            d2.systemInfoModule().systemInfo().blockingGet().contextPath(),
            uid
        )

    private fun ruleVariableMap(programUid: String, values: Map<String, String>? = null) =
        d2.programModule().programRuleVariables()
            .byProgramUid().eq(programUid)
            .blockingGet().toRuleVariableList(
                d2.trackedEntityModule().trackedEntityAttributes(),
                d2.dataElementModule().dataElements()
            ).map {
                val ruleValueType = when (it) {
                    is RuleVariableCalculatedValue -> it.calculatedValueType()
                    is RuleVariableAttribute -> it.trackedEntityAttributeType()
                    is RuleVariableNewestStageEvent -> it.dataElementType()
                    is RuleVariableNewestEvent -> it.dataElementType()
                    is RuleVariableCurrentEvent -> it.dataElementType()
                    is RuleVariablePreviousEvent -> it.dataElementType()
                    else -> null
                }
                val valueKey = when (it) {
                    is RuleVariableCalculatedValue -> it.name()
                    is RuleVariableAttribute -> it.trackedEntityAttribute()
                    is RuleVariableNewestStageEvent -> it.dataElement()
                    is RuleVariableNewestEvent -> it.dataElement()
                    is RuleVariableCurrentEvent -> it.dataElement()
                    is RuleVariablePreviousEvent -> it.dataElement()
                    else -> null
                }

                val value = values?.get(valueKey)

                it.name() to ruleVariableValue(value, ruleValueType, values == null)
            }.toMap().toMutableMap().apply {
                RuleEngineUtils.ENV_VARIABLES.forEach { (envLabelKey, type) ->
                    val value = values?.get(envLabelKey)
                    val ruleValueType = when (type) {
                        ItemValueType.NUMBER -> RuleValueType.NUMERIC
                        ItemValueType.DATE -> RuleValueType.DATE
                        ItemValueType.TEXT -> RuleValueType.TEXT
                        ItemValueType.BOOLEAN -> RuleValueType.BOOLEAN
                    }
                    this[envLabelKey] = RuleVariableValue.create(
                        value ?: ruleValueType.defaultValue(),
                        ruleValueType
                    )
                }
            }

    private fun ruleVariableValue(
        value: String?,
        ruleValueType: RuleValueType?,
        addDefaultValue: Boolean = false
    ): RuleVariableValue? {
        val valueToUse = if (addDefaultValue) {
            ruleValueType?.defaultValue()
        } else {
            value
        }
        return valueToUse?.let { RuleVariableValue.create(valueToUse, ruleValueType!!) }
    }

    private fun process(
        condition: String,
        valueMap: Map<String, RuleVariableValue?>,
        ruleActionType: String? = null
    ): String {
        if (condition.isEmpty()) {
            return if (ruleActionType != null) {
                "$ruleActionType: Condition is empty"
            } else {
                "%s Condition is empty"
            }
        }
        return try {
            val commonExpressionVisitor =
                CommonExpressionVisitor.newBuilder()
                    .withFunctionMap(RuleEngineUtils.FUNCTIONS)
                    .withFunctionMethod(ParserUtils.FUNCTION_EVALUATE)
                    .withVariablesMap(valueMap)
                    .withSupplementaryData(HashMap())
                    .validateCommonProperties()
            val result = Parser.visit(
                condition,
                commonExpressionVisitor,
                true
            )
            convertInteger(result).toString()
            ""
        } catch (e: ParserExceptionWithoutContext) {
            "Condition " + condition + " not executed: " + e.message
        } catch (e: Exception) {
            "Unexpected exception while evaluating " + condition + ": " + e.message
        }
    }

    private fun convertInteger(result: Any): Any {
        return if (result is Double && result % 1 == 0.0) {
            result.toInt()
        } else result
    }

    private fun RuleAction.ruleActionType() = this.javaClass.simpleName.removePrefix("AutoValue_")

    private fun RuleAction.needsContent() = when (this) {
        is RuleActionHideField, is RuleActionHideOption,
        is RuleActionHideOptionGroup, is RuleActionHideProgramStage,
        is RuleActionHideSection, is RuleActionSetMandatoryField,
        is RuleActionShowOptionGroup -> false
        else -> true
    }

    private fun evaluateAction(
        ruleAction: RuleAction,
        valueMap: Map<String, RuleVariableValue?>
    ): String? {
        return if (ruleAction.needsContent()) {
            val actionConditionResult =
                process(ruleAction.data(), valueMap, ruleAction.ruleActionType())
            if (actionConditionResult.isNotEmpty()) {
                actionConditionResult
            } else {
                null
            }
        } else {
            checkActionVariables(ruleAction)
        }
    }

    private fun checkActionVariables(ruleAction: RuleAction): String? {
        return when {
            ruleAction is RuleActionHideField && ruleAction.field().isEmpty() ->
                "Missing field"
            ruleAction is RuleActionHideOption && (
                    ruleAction.field()
                        .isEmpty() || ruleAction.option().isEmpty()
                    ) ->
                "Missing field or option"
            ruleAction is RuleActionHideOptionGroup && (
                    ruleAction.field()
                        .isEmpty() || ruleAction.optionGroup().isEmpty()
                    ) ->
                "Missing field or option group"
            ruleAction is RuleActionHideProgramStage && ruleAction.programStage().isEmpty() ->
                "Missing program stage"
            ruleAction is RuleActionHideSection && ruleAction.programStageSection().isEmpty() ->
                "Missing program stage section"
            ruleAction is RuleActionSetMandatoryField && ruleAction.field().isEmpty() ->
                "Missing field"
            ruleAction is RuleActionShowOptionGroup && ruleAction.optionGroup().isEmpty() ->
                "Missing option group"
            else -> null
        }
    }

}