package org.dhis2.utils

import org.apache.commons.jexl2.JexlEngine
import org.hisp.dhis.rules.RuleExpressionEvaluator

class ExpressionEvaluatorImpl(val jexl: JexlEngine): RuleExpressionEvaluator {

    override fun evaluate(expression: String): String {
        return try {
            jexl.createExpression(expression).evaluate(null).toString()
        } catch (e: Exception) {
            expression
        }
    }

}