package org.dhis2.data.forms;

import androidx.annotation.NonNull;

import org.hisp.dhis.rules.api.RuleEngine;

import io.reactivex.Flowable;

public interface FormRepository {

    Flowable<RuleEngineContextData> restartRuleEngine();

    @NonNull
    Flowable<RuleEngineContextData> ruleEngine();

}