package org.dhis2.data.forms.dataentry;

import org.dhis2.utils.Result;
import org.hisp.dhis.rules.RuleEngine;
import org.hisp.dhis.rules.models.RuleEffect;

import androidx.annotation.NonNull;
import io.reactivex.Flowable;

public interface RuleEngineRepository {

    void updateRuleAttributeMap(String uid, String value);

    Flowable<RuleEngine> updateRuleEngine();

    @NonNull
    Flowable<Result<RuleEffect>> calculate();

    @NonNull
    Flowable<Result<RuleEffect>> reCalculate();

}
