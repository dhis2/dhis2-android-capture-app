package org.dhis2.android.rtsm.services.rules

import javax.inject.Inject
import org.apache.commons.jexl2.JexlEngine
import org.hisp.dhis.rules.RuleExpressionEvaluator

class ExpressionEvaluatorImpl @Inject constructor(
    private val jexl: JexlEngine
) : RuleExpressionEvaluator {

    override fun evaluate(expression: String) = jexl.createExpression(expression)
        .evaluate(null).toString()
}
