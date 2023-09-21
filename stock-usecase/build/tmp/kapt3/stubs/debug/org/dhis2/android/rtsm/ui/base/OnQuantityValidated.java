package org.dhis2.android.rtsm.ui.base;

import org.hisp.dhis.rules.models.RuleEffect;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\bf\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005H&\u00a8\u0006\u0007"}, d2 = {"Lorg/dhis2/android/rtsm/ui/base/OnQuantityValidated;", "", "validationCompleted", "", "ruleEffects", "", "Lorg/hisp/dhis/rules/models/RuleEffect;", "psm-v2.9-DEV_debug"})
public abstract interface OnQuantityValidated {
    
    public abstract void validationCompleted(@org.jetbrains.annotations.NotNull
    java.util.List<? extends org.hisp.dhis.rules.models.RuleEffect> ruleEffects);
}