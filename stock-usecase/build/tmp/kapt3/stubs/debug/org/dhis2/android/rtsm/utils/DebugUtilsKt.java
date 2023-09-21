package org.dhis2.android.rtsm.utils;

import org.hisp.dhis.rules.models.Rule;
import org.hisp.dhis.rules.models.RuleActionAssign;
import org.hisp.dhis.rules.models.RuleDataValue;
import org.hisp.dhis.rules.models.RuleEffect;
import org.hisp.dhis.rules.models.RuleEvent;
import org.hisp.dhis.rules.models.RuleVariable;
import org.hisp.dhis.rules.models.RuleVariableCurrentEvent;
import timber.log.Timber;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000F\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u001a0\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\b0\u00052\f\u0010\t\u001a\b\u0012\u0004\u0012\u00020\n0\u0005\u001a\u0012\u0010\u000b\u001a\u00020\u00032\n\u0010\f\u001a\u00060\rj\u0002`\u000e\u001a,\u0010\u000f\u001a\u00020\u00032\u0006\u0010\u0010\u001a\u00020\u00112\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00130\u00052\u000e\u0010\u0014\u001a\n\u0012\u0004\u0012\u00020\u0015\u0018\u00010\u0005\u001a\u001a\u0010\u0016\u001a\u00020\u00032\n\u0010\f\u001a\u00060\rj\u0002`\u000e2\u0006\u0010\u0017\u001a\u00020\u0011\u001a\u0012\u0010\u0018\u001a\u00020\u00032\n\u0010\f\u001a\u00060\rj\u0002`\u000e\"\u000e\u0010\u0000\u001a\u00020\u0001X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0019"}, d2 = {"MAX_LEN", "", "debugRuleEngine", "", "rules", "", "Lorg/hisp/dhis/rules/models/Rule;", "ruleVariables", "Lorg/hisp/dhis/rules/models/RuleVariable;", "events", "Lorg/hisp/dhis/rules/models/RuleEvent;", "printEmpty", "buffer", "Ljava/lang/StringBuilder;", "Lkotlin/text/StringBuilder;", "printRuleEffects", "label", "", "ruleEffects", "Lorg/hisp/dhis/rules/models/RuleEffect;", "dataValues", "Lorg/hisp/dhis/rules/models/RuleDataValue;", "printRuleEngineData", "data", "printSeparator", "psm-v2.9-DEV_debug"})
public final class DebugUtilsKt {
    public static final int MAX_LEN = 120;
    
    public static final void debugRuleEngine(@org.jetbrains.annotations.NotNull
    java.util.List<? extends org.hisp.dhis.rules.models.Rule> rules, @org.jetbrains.annotations.NotNull
    java.util.List<? extends org.hisp.dhis.rules.models.RuleVariable> ruleVariables, @org.jetbrains.annotations.NotNull
    java.util.List<? extends org.hisp.dhis.rules.models.RuleEvent> events) {
    }
    
    public static final void printRuleEffects(@org.jetbrains.annotations.NotNull
    java.lang.String label, @org.jetbrains.annotations.NotNull
    java.util.List<? extends org.hisp.dhis.rules.models.RuleEffect> ruleEffects, @org.jetbrains.annotations.Nullable
    java.util.List<? extends org.hisp.dhis.rules.models.RuleDataValue> dataValues) {
    }
    
    public static final void printRuleEngineData(@org.jetbrains.annotations.NotNull
    java.lang.StringBuilder buffer, @org.jetbrains.annotations.NotNull
    java.lang.String data) {
    }
    
    public static final void printSeparator(@org.jetbrains.annotations.NotNull
    java.lang.StringBuilder buffer) {
    }
    
    public static final void printEmpty(@org.jetbrains.annotations.NotNull
    java.lang.StringBuilder buffer) {
    }
}