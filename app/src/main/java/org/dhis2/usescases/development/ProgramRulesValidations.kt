package org.dhis2.usescases.development

import android.os.Build
import java.util.HashMap
import org.dhis2.Bindings.toRuleList
import org.dhis2.Bindings.toRuleVariableList
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.antlr.Parser
import org.hisp.dhis.antlr.ParserExceptionWithoutContext
import org.hisp.dhis.rules.RuleVariableValue
import org.hisp.dhis.rules.models.RuleAction
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

const val SEPARATOR = "*************"
const val LINE_BREAK = "\n"

class ProgramRulesValidations(val d2: D2) {

    private var hasError = false

    fun validateRules(): String {
        val valueMap: Map<String, RuleVariableValue?> = ruleVariableMap()
        val checkResult = StringBuilder("")
        programRules().forEach { rule ->
            hasError = false
            val ruleConditionResult = process(rule.condition(), valueMap)
            if (ruleConditionResult.isNotEmpty()) {
                setConditionError(rule.uid(), ruleConditionResult, checkResult)
            }
            rule.actions().forEach { ruleAction ->
                unsupportedActionCheck(rule.uid(), ruleAction, checkResult)
                val actionConditionResult = process(ruleAction.data(), valueMap)
                setConditionError(rule.uid(), actionConditionResult, checkResult)
            }
        }
        return checkResult.toString()
    }

    private fun programRules() =
        d2.programModule().programRules().withProgramRuleActions().blockingGet().toRuleList()

    private fun ruleVariableMap() =
        d2.programModule().programRuleVariables().blockingGet().toRuleVariableList(
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
            it.name() to ruleVariableValue(null, ruleValueType)
        }.toMap()

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

    private fun process(condition: String, valueMap: Map<String, RuleVariableValue?>): String {
        if (condition.isEmpty()) {
            return "Condition is empty"
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
                !isOldAndroidVersion()
            )
            convertInteger(result).toString()
            ""
        } catch (e: ParserExceptionWithoutContext) {
            "Condition " + condition + " not executed: " + e.message
        } catch (e: Exception) {
            "Unexpected exception while evaluating " + condition + ": " + e.message
        }
    }

    private fun isOldAndroidVersion(): Boolean {
        return Build.VERSION.SDK_INT < 21
    }

    private fun convertInteger(result: Any): Any? {
        return if (result is Double && result % 1 == 0.0) {
            result.toInt()
        } else result
    }

    private fun setFirstError(
        ruleUid: String,
        checkResult: StringBuilder
    ) {
        if (!hasError) {
            hasError = true
            checkResult.apply {
                appendMessage("Program rule uid: %s".format(ruleUid), true)
            }
        }
    }

    private fun setConditionError(
        ruleUid: String,
        conditionResult: String,
        checkResult: StringBuilder
    ) {
        checkResult.apply {
            setFirstError(ruleUid, this)
            appendMessage("- %s".format(conditionResult))
        }
    }

    private fun unsupportedActionCheck(
        ruleUid: String,
        ruleAction: RuleAction,
        checkResult: StringBuilder
    ) {
        if (ruleAction is RuleActionUnsupported) {
            checkResult.apply {
                setFirstError(ruleUid, checkResult)
                appendMessage("- %s".format(ruleAction.content()))
            }
        }
    }

    private fun StringBuilder.appendMessage(message: String, addSeparator: Boolean = false) {
        if (addSeparator) {
            append(SEPARATOR)
            append(LINE_BREAK)
        }
        append(message)
        append(LINE_BREAK)
    }
}
