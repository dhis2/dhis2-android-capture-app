package org.dhis2.usescases.troubleshooting

import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.form.bindings.toRuleEngineObject
import org.dhis2.form.bindings.toRuleVariableList
import org.dhis2.usescases.development.ProgramRuleValidation
import org.dhis2.usescases.development.RuleValidation
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramRuleActionType
import org.hisp.dhis.antlr.ParserExceptionWithoutContext
import org.hisp.dhis.lib.expression.Expression
import org.hisp.dhis.lib.expression.spi.ExpressionData
import org.hisp.dhis.rules.api.EnvironmentVariables.ENV_VARIABLES
import org.hisp.dhis.rules.api.ItemValueType
import org.hisp.dhis.rules.engine.RuleVariableValue
import org.hisp.dhis.rules.models.RuleAction
import org.hisp.dhis.rules.models.RuleValueType
import org.hisp.dhis.rules.models.RuleVariableAttribute
import org.hisp.dhis.rules.models.RuleVariableCalculatedValue
import org.hisp.dhis.rules.models.RuleVariableCurrentEvent
import org.hisp.dhis.rules.models.RuleVariableNewestEvent
import org.hisp.dhis.rules.models.RuleVariableNewestStageEvent
import org.hisp.dhis.rules.models.RuleVariablePreviousEvent

class TroubleshootingRepository(
    val d2: D2,
    val metadataIconProvider: MetadataIconProvider,
) {

    fun validateProgramRules(): List<ProgramRuleValidation> {
        val programRulesMap = mutableMapOf<Program, MutableList<RuleValidation>>()
        programRules().mapNotNull { programAndRule ->
            val rule = programAndRule.second
            val program = program(programAndRule.first?.uid())
            val valueMap: Map<String, RuleVariableValue> = ruleVariableMap(program?.uid())
            var ruleValidationItem = RuleValidation(rule, ruleExternalLink(rule.uid))
            val ruleConditionResult = process(
                rule.condition,
                valueMap,
                null,
                Expression.Mode.RULE_ENGINE_CONDITION,
            )
            if (ruleConditionResult.isNotEmpty()) {
                ruleValidationItem = ruleValidationItem.copy(conditionError = ruleConditionResult)
            }
            val ruleActionConditions = rule.actions.mapNotNull { ruleAction ->
                when (ruleAction.type) {
                    "unsupported" -> {
                        "%s is not supported".format(ruleAction.type)
                    }

                    "error" -> {
                        ruleAction.type + " : " + ruleAction.data
                    }

                    else -> {
                        evaluateAction(ruleAction, valueMap)
                    }
                }
            }
            if (ruleActionConditions.isNotEmpty()) {
                ruleValidationItem = ruleValidationItem.copy(actionsError = ruleActionConditions)
            }
            if (ruleValidationItem.hasError()) {
                Pair(program, ruleValidationItem)
            } else {
                null
            }
        }.forEach { (program, ruleValidation) ->
            program?.let {
                if (programRulesMap.containsKey(it)) {
                    programRulesMap[it]?.add(ruleValidation)
                } else {
                    programRulesMap[it] = mutableListOf(ruleValidation)
                }
            }
        }
        return programRulesMap.map { (program, validationList) ->
            ProgramRuleValidation(
                programUid = program.uid(),
                programName = program.displayName() ?: program.uid(),
                metadataIconData = metadataIconProvider(program.style(), sizeInDp = 24),
                validations = validationList,
            )
        }.sortedBy { it.programName }
    }

    private fun program(programUid: String?) =
        d2.programModule().programs().uid(programUid).blockingGet()

    private fun programRules() =
        d2.programModule().programRules().withProgramRuleActions().blockingGet().map {
            Pair(it.program(), it.toRuleEngineObject())
        }

    private fun ruleExternalLink(uid: String) =
        "%s/api/programRules/%s?fields=*,programRuleActions[*]".format(
            d2.systemInfoModule().systemInfo().blockingGet()?.contextPath(),
            uid,
        )

    private fun ruleVariableMap(programUid: String?, values: Map<String, String>? = null) =
        d2.programModule().programRuleVariables()
            .byProgramUid().eq(programUid)
            .blockingGet().toRuleVariableList(
                d2.trackedEntityModule().trackedEntityAttributes(),
                d2.dataElementModule().dataElements(),
                d2.optionModule().options(),
            ).mapNotNull {
                val ruleValueType = it.fieldType
                val valueKey = when (it) {
                    is RuleVariableCalculatedValue -> it.name
                    is RuleVariableAttribute,
                    is RuleVariableNewestStageEvent,
                    is RuleVariableNewestEvent,
                    is RuleVariableCurrentEvent,
                    is RuleVariablePreviousEvent,
                    -> it.field

                    else -> null
                }

                val value = values?.get(valueKey)
                ruleVariableValue(value, ruleValueType, values == null)?.let { value ->
                    it.name to value
                }
            }.toMap().toMutableMap().apply {
                ENV_VARIABLES.forEach { (envLabelKey, type) ->
                    val value = values?.get(envLabelKey)
                    val ruleValueType = when (type) {
                        ItemValueType.NUMBER -> RuleValueType.NUMERIC
                        ItemValueType.DATE -> RuleValueType.DATE
                        ItemValueType.TEXT -> RuleValueType.TEXT
                        ItemValueType.BOOLEAN -> RuleValueType.BOOLEAN
                    }
                    this[envLabelKey] = RuleVariableValue(
                        ruleValueType,
                        value ?: ruleValueType.defaultValue().toString(),
                    )
                }
            }

    private fun ruleVariableValue(
        value: String?,
        ruleValueType: RuleValueType?,
        addDefaultValue: Boolean = false,
    ): RuleVariableValue? {
        val valueToUse = if (addDefaultValue) {
            ruleValueType?.defaultValue().toString()
        } else {
            value
        }
        return valueToUse?.let {
            RuleVariableValue(
                ruleValueType!!,
                valueToUse,
            )
        }
    }

    private fun process(
        condition: String,
        valueMap: Map<String, RuleVariableValue>,
        ruleActionType: String? = null,
        mode: Expression.Mode,
    ): String {
        if (condition.isEmpty()) {
            return if (ruleActionType != null) {
                "$ruleActionType: Condition is empty"
            } else {
                "%s Condition is empty"
            }
        }
        return try {
            val expression = Expression(condition, mode)
            val values = valueMap.mapValues { (_, value) ->
                value.toVariableValue()
            }
            val expressionData = ExpressionData(
                values,
                emptyMap(),
                emptyMap(),
                emptyMap(),
                emptyMap(),
            )
            val result = expression.evaluate(
                { name ->
                    throw UnsupportedOperationException(name)
                },
                expressionData,
            )!!
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
        } else {
            result
        }
    }

    private fun RuleAction.ruleActionType() = this.javaClass.simpleName.removePrefix("AutoValue_")

    private fun RuleAction.needsContent() = when (this.type) {
        ProgramRuleActionType.HIDEFIELD.name, ProgramRuleActionType.HIDEOPTION.name,
        ProgramRuleActionType.HIDEOPTIONGROUP.name, ProgramRuleActionType.HIDEPROGRAMSTAGE.name,
        ProgramRuleActionType.HIDESECTION.name, ProgramRuleActionType.SETMANDATORYFIELD.name,
        ProgramRuleActionType.SHOWOPTIONGROUP.name,
        -> false

        else -> true
    }

    private fun evaluateAction(
        ruleAction: RuleAction,
        valueMap: Map<String, RuleVariableValue>,
    ): String? {
        return if (ruleAction.needsContent()) {
            val actionConditionResult =
                process(
                    ruleAction.data!!,
                    valueMap,
                    ruleAction.ruleActionType(),
                    Expression.Mode.RULE_ENGINE_ACTION,
                )
            actionConditionResult.ifEmpty {
                null
            }
        } else {
            checkActionVariables(ruleAction)
        }
    }

    private fun checkActionVariables(ruleAction: RuleAction): String? {
        return when {
            ruleAction.type == ProgramRuleActionType.HIDEFIELD.name && ruleAction.field()
                ?.isEmpty() == true ->
                "Missing field"

            ruleAction.type == ProgramRuleActionType.HIDEOPTION.name &&
                (
                    ruleAction.field()?.isEmpty() == true ||
                        ruleAction.values["option"].isNullOrEmpty()
                    ) ->
                "Missing field or option"

            ruleAction.type == ProgramRuleActionType.HIDEOPTIONGROUP.name && (
                ruleAction.field()?.isEmpty() == true ||
                    ruleAction.values["optionGroup"].isNullOrEmpty()
                ) ->
                "Missing field or option group"

            ruleAction.type == ProgramRuleActionType.HIDEPROGRAMSTAGE.name &&
                ruleAction.values["programStage"].isNullOrEmpty() ->
                "Missing program stage"

            ruleAction.type == ProgramRuleActionType.HIDESECTION.name &&
                ruleAction.values["programStageSection"].isNullOrEmpty() ->
                "Missing program stage section"

            ruleAction.type == ProgramRuleActionType.SETMANDATORYFIELD.name &&
                ruleAction.field().isNullOrEmpty() ->
                "Missing field"

            ruleAction.type == ProgramRuleActionType.SHOWOPTIONGROUP.name &&
                ruleAction.values["optionGroup"].isNullOrEmpty() ->
                "Missing option group"

            else -> null
        }
    }
}
