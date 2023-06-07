package org.dhis2.android.rtsm.utils

import org.hisp.dhis.rules.models.Rule
import org.hisp.dhis.rules.models.RuleActionAssign
import org.hisp.dhis.rules.models.RuleDataValue
import org.hisp.dhis.rules.models.RuleEffect
import org.hisp.dhis.rules.models.RuleEvent
import org.hisp.dhis.rules.models.RuleVariable
import org.hisp.dhis.rules.models.RuleVariableCurrentEvent
import timber.log.Timber

const val MAX_LEN = 120

fun debugRuleEngine(rules: List<Rule>, ruleVariables: List<RuleVariable>, events: List<RuleEvent>) {
    val buffer = StringBuilder()
    buffer.append("-----                       Rules Engine dump                -----------")
    buffer.append("\n\n")
    printSeparator(buffer)
    printRuleEngineData(buffer, "Rules:")
    printSeparator(buffer)
    rules.forEach { rule ->
        printRuleEngineData(buffer, "uid:               ${rule.uid()}")
        printRuleEngineData(buffer, "name:              ${rule.name()}")
        printRuleEngineData(buffer, "condition:         ${rule.condition()}")
        printRuleEngineData(buffer, "actions:")
        rule.actions().forEach { action ->
            printRuleEngineData(buffer, "   type:           ${action.javaClass.simpleName}")
            if (action is RuleActionAssign) {
                printRuleEngineData(buffer, "   field:          ${action.field()}")
            }
            printRuleEngineData(buffer, "   data:           ${action.data()}")
            printEmpty(buffer)
        }
    }

    printSeparator(buffer)
    printRuleEngineData(buffer, "Variables:")
    ruleVariables.forEach {
        var variable = "   name = ${it.name()}"
        if (it is RuleVariableCurrentEvent) {
            variable += ", DE = ${it.dataElement()}"
        }

        printRuleEngineData(buffer, variable)
    }

    printSeparator(buffer)
    printRuleEngineData(buffer, "Events:")
    printSeparator(buffer)
    events.forEach {
        printRuleEngineData(
            buffer,
            "   Event uid = ${it.event()}, status = ${it.status()}, eventDate = ${it.eventDate()}"
        )

        printRuleEngineData(buffer, "   Data values:")
        it.dataValues().forEach { dv ->
            printRuleEngineData(
                buffer,
                "      DE = ${dv.dataElement()}, value = ${dv.value()}"
            )
        }

        printEmpty(buffer)
    }
    printSeparator(buffer)
    buffer.append("\n\n")
    Timber.d(buffer.toString())
}

fun printRuleEffects(
    label: String,
    ruleEffects: List<RuleEffect>,
    dataValues: List<RuleDataValue>?
) {
    val buffer = StringBuilder()
    buffer.append("\n")
    printSeparator(buffer)
    printRuleEngineData(buffer, "$label:")
    printSeparator(buffer)
    ruleEffects.forEach { ruleEffect ->
        when (ruleEffect.ruleAction()) {
            is RuleActionAssign -> {
                val ruleAction = ruleEffect.ruleAction() as RuleActionAssign
                printRuleEngineData(
                    buffer,
                    "field = ${ruleAction.field()}, " +
                        "data = ${ruleEffect.data()}, " +
                        "rule = ${ruleAction.data()}"
                )
            }
        }
    }

    dataValues?.let { printSeparator(buffer) }
    dataValues?.let { printRuleEngineData(buffer, "   Rule Data values:") }
    dataValues?.forEach {
        printRuleEngineData(
            buffer,
            "      DE = ${it.dataElement()}, value = ${it.value()}, eventDate= ${it.eventDate()}"
        )
    }
    printSeparator(buffer)
    Timber.d(buffer.toString())
}

fun printRuleEngineData(buffer: StringBuilder, data: String) {
    val charsLen = data.length
    val desiredLen = MAX_LEN - charsLen - 2
//    println(data)
    buffer.append("|" + data.padEnd(if (desiredLen > MAX_LEN) desiredLen else MAX_LEN) + "|\n")
}

fun printSeparator(buffer: StringBuilder) {
    buffer.append("".padEnd(MAX_LEN + 2, '-') + "\n")
}

fun printEmpty(buffer: StringBuilder) {
    buffer.append("|" + "".padEnd(MAX_LEN) + "|\n")
}
