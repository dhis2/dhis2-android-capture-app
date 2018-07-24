package com.dhis2.utils;

import com.google.auto.value.AutoValue;

import org.hisp.dhis.rules.models.RuleVariable;

import javax.annotation.Nonnull;

/**
 * QUADRAM. Created by ppajuelo on 24/07/2018.
 */

@AutoValue
public abstract class RuleVariableCalculatedValue extends RuleVariable {

    @Nonnull
    public static RuleVariableCalculatedValue create(@Nonnull String name) {
        return new AutoValue_RuleVariableCalculatedValue(name);
    }

}
