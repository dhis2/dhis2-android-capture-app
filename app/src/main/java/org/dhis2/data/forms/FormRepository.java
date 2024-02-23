package org.dhis2.data.forms;

import androidx.annotation.NonNull;

import org.dhis2.commons.rules.RuleEngineContextData;

import io.reactivex.Flowable;

public interface FormRepository {

    Flowable<RuleEngineContextData> restartRuleEngine();

    @NonNull
    Flowable<RuleEngineContextData> ruleEngine();

}