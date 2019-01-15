package org.dhis2.utils;

import android.support.annotation.NonNull;

import org.apache.commons.jexl2.JexlEngine;
import org.hisp.dhis.rules.RuleExpressionEvaluator;

import javax.annotation.Nonnull;

/**
 * QUADRAM. Created by ppajuelo on 19/09/2018.
 */

public class ExpressionEvaluatorImpl implements RuleExpressionEvaluator {

    private final JexlEngine JEXL;

    public ExpressionEvaluatorImpl(@NonNull JexlEngine jexl) {

        this.JEXL = jexl;
    }

    @Nonnull
    @Override
    public String evaluate(@Nonnull String expression) {
        if (expression == null) {
            throw new NullPointerException("expression == null");
        }

        try {
            String result = JEXL.createExpression(expression).evaluate(null).toString();
            return result;
        } catch (Exception e) {
            return expression;
        }
    }
}
