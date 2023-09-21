package org.dhis2.android.rtsm.utils;

import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.dataelement.DataElementCollectionRepository;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.option.OptionCollectionRepository;
import org.hisp.dhis.android.core.program.ProgramRule;
import org.hisp.dhis.android.core.program.ProgramRuleAction;
import org.hisp.dhis.android.core.program.ProgramRuleActionType;
import org.hisp.dhis.android.core.program.ProgramRuleVariable;
import org.hisp.dhis.android.core.program.ProgramRuleVariableCollectionRepository;
import org.hisp.dhis.android.core.program.ProgramRuleVariableSourceType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeCollectionRepository;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.rules.Option;
import org.hisp.dhis.rules.models.Rule;
import org.hisp.dhis.rules.models.RuleAction;
import org.hisp.dhis.rules.models.RuleActionAssign;
import org.hisp.dhis.rules.models.RuleActionErrorOnCompletion;
import org.hisp.dhis.rules.models.RuleActionHideField;
import org.hisp.dhis.rules.models.RuleActionShowError;
import org.hisp.dhis.rules.models.RuleDataValue;
import org.hisp.dhis.rules.models.RuleValueType;
import org.hisp.dhis.rules.models.RuleVariable;
import org.hisp.dhis.rules.models.RuleVariableAttribute;
import org.hisp.dhis.rules.models.RuleVariableCalculatedValue;
import org.hisp.dhis.rules.models.RuleVariableCurrentEvent;
import org.hisp.dhis.rules.models.RuleVariableNewestEvent;
import org.hisp.dhis.rules.models.RuleVariableNewestStageEvent;
import org.hisp.dhis.rules.models.RuleVariablePreviousEvent;
import timber.log.Timber;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000r\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u001a@\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u00012\u0006\u0010\u0003\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\u00062\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r\u001a\u0010\u0010\u000e\u001a\u00020\u00042\u0006\u0010\u000f\u001a\u00020\u0010H\u0002\u001a\u0016\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\u0001*\b\u0012\u0004\u0012\u00020\u00130\u0001\u001a6\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00150\u0001*\b\u0012\u0004\u0012\u00020\u00160\u00012\u0006\u0010\u0017\u001a\u00020\u00182\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\f\u001a\u00020\r\u001a\n\u0010\u001b\u001a\u00020\u001c*\u00020\u001d\u001a\n\u0010\u001b\u001a\u00020\u0012*\u00020\u0013\u001a\u0016\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u001c0\u0001*\b\u0012\u0004\u0012\u00020\u001d0\u0001\u001a\n\u0010\u001f\u001a\u00020 *\u00020!\u001a\"\u0010\"\u001a\u00020#*\u00020\u00102\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r\u001a.\u0010$\u001a\b\u0012\u0004\u0012\u00020#0\u0001*\b\u0012\u0004\u0012\u00020\u00100\u00012\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r\u00a8\u0006%"}, d2 = {"getOptions", "", "Lorg/hisp/dhis/rules/Option;", "useCodeForOptionSet", "", "dataElementUid", "", "trackedEntityAttributeUid", "attributeRepository", "Lorg/hisp/dhis/android/core/trackedentity/TrackedEntityAttributeCollectionRepository;", "dataElementRepository", "Lorg/hisp/dhis/android/core/dataelement/DataElementCollectionRepository;", "optionRepository", "Lorg/hisp/dhis/android/core/option/OptionCollectionRepository;", "isCalculatedValue", "it", "Lorg/hisp/dhis/android/core/program/ProgramRuleVariable;", "toRuleActionList", "Lorg/hisp/dhis/rules/models/RuleAction;", "Lorg/hisp/dhis/android/core/program/ProgramRuleAction;", "toRuleDataValue", "Lorg/hisp/dhis/rules/models/RuleDataValue;", "Lorg/hisp/dhis/android/core/trackedentity/TrackedEntityDataValue;", "event", "Lorg/hisp/dhis/android/core/event/Event;", "ruleVariableRepository", "Lorg/hisp/dhis/android/core/program/ProgramRuleVariableCollectionRepository;", "toRuleEngineObject", "Lorg/hisp/dhis/rules/models/Rule;", "Lorg/hisp/dhis/android/core/program/ProgramRule;", "toRuleList", "toRuleValueType", "Lorg/hisp/dhis/rules/models/RuleValueType;", "Lorg/hisp/dhis/android/core/common/ValueType;", "toRuleVariable", "Lorg/hisp/dhis/rules/models/RuleVariable;", "toRuleVariableList", "psm-v2.9-DEV_debug"})
public final class RuleExtensionsKt {
    
    @org.jetbrains.annotations.NotNull
    public static final java.util.List<org.hisp.dhis.rules.models.Rule> toRuleList(@org.jetbrains.annotations.NotNull
    java.util.List<? extends org.hisp.dhis.android.core.program.ProgramRule> $this$toRuleList) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final java.util.List<org.hisp.dhis.rules.models.RuleAction> toRuleActionList(@org.jetbrains.annotations.NotNull
    java.util.List<? extends org.hisp.dhis.android.core.program.ProgramRuleAction> $this$toRuleActionList) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final java.util.List<org.hisp.dhis.rules.models.RuleVariable> toRuleVariableList(@org.jetbrains.annotations.NotNull
    java.util.List<? extends org.hisp.dhis.android.core.program.ProgramRuleVariable> $this$toRuleVariableList, @org.jetbrains.annotations.NotNull
    org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeCollectionRepository attributeRepository, @org.jetbrains.annotations.NotNull
    org.hisp.dhis.android.core.dataelement.DataElementCollectionRepository dataElementRepository, @org.jetbrains.annotations.NotNull
    org.hisp.dhis.android.core.option.OptionCollectionRepository optionRepository) {
        return null;
    }
    
    private static final boolean isCalculatedValue(org.hisp.dhis.android.core.program.ProgramRuleVariable it) {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final org.hisp.dhis.rules.models.Rule toRuleEngineObject(@org.jetbrains.annotations.NotNull
    org.hisp.dhis.android.core.program.ProgramRule $this$toRuleEngineObject) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final org.hisp.dhis.rules.models.RuleAction toRuleEngineObject(@org.jetbrains.annotations.NotNull
    org.hisp.dhis.android.core.program.ProgramRuleAction $this$toRuleEngineObject) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final org.hisp.dhis.rules.models.RuleVariable toRuleVariable(@org.jetbrains.annotations.NotNull
    org.hisp.dhis.android.core.program.ProgramRuleVariable $this$toRuleVariable, @org.jetbrains.annotations.NotNull
    org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeCollectionRepository attributeRepository, @org.jetbrains.annotations.NotNull
    org.hisp.dhis.android.core.dataelement.DataElementCollectionRepository dataElementRepository, @org.jetbrains.annotations.NotNull
    org.hisp.dhis.android.core.option.OptionCollectionRepository optionRepository) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final java.util.List<org.hisp.dhis.rules.Option> getOptions(boolean useCodeForOptionSet, @org.jetbrains.annotations.Nullable
    java.lang.String dataElementUid, @org.jetbrains.annotations.Nullable
    java.lang.String trackedEntityAttributeUid, @org.jetbrains.annotations.NotNull
    org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeCollectionRepository attributeRepository, @org.jetbrains.annotations.NotNull
    org.hisp.dhis.android.core.dataelement.DataElementCollectionRepository dataElementRepository, @org.jetbrains.annotations.NotNull
    org.hisp.dhis.android.core.option.OptionCollectionRepository optionRepository) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final org.hisp.dhis.rules.models.RuleValueType toRuleValueType(@org.jetbrains.annotations.NotNull
    org.hisp.dhis.android.core.common.ValueType $this$toRuleValueType) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public static final java.util.List<org.hisp.dhis.rules.models.RuleDataValue> toRuleDataValue(@org.jetbrains.annotations.NotNull
    java.util.List<? extends org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue> $this$toRuleDataValue, @org.jetbrains.annotations.NotNull
    org.hisp.dhis.android.core.event.Event event, @org.jetbrains.annotations.NotNull
    org.hisp.dhis.android.core.dataelement.DataElementCollectionRepository dataElementRepository, @org.jetbrains.annotations.NotNull
    org.hisp.dhis.android.core.program.ProgramRuleVariableCollectionRepository ruleVariableRepository, @org.jetbrains.annotations.NotNull
    org.hisp.dhis.android.core.option.OptionCollectionRepository optionRepository) {
        return null;
    }
}