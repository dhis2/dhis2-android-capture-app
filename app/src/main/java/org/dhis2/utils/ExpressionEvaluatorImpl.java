package org.dhis2.utils;

import org.apache.commons.jexl2.JexlEngine;
import org.hisp.dhis.rules.RuleExpressionEvaluator;

import javax.annotation.Nonnull;

import androidx.annotation.NonNull;

/**
 * QUADRAM. Created by ppajuelo on 19/09/2018.
 */

public class ExpressionEvaluatorImpl implements RuleExpressionEvaluator {

    private JexlEngine jexl;

    public ExpressionEvaluatorImpl(@NonNull JexlEngine jexl) {
        this.jexl = jexl;
    }

    @Nonnull
    @Override
    public String evaluate(@NonNull String expression) {
        try {
            return jexl.createExpression(expression).evaluate(null).toString();
        } catch (Exception e) {
            return expression;
        }
    }
}
