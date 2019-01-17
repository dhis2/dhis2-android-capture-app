package org.dhis2.data.forms.dataentry;

import androidx.annotation.NonNull;

import org.dhis2.utils.Result;

import org.hisp.dhis.rules.models.RuleEffect;

import io.reactivex.Flowable;

public interface RuleEngineRepository {

    @NonNull
    Flowable<Result<RuleEffect>> calculate();

}
