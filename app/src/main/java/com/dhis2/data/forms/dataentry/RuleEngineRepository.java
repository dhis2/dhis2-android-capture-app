package com.dhis2.data.forms.dataentry;

import android.support.annotation.NonNull;

import com.dhis2.utils.Result;

import org.hisp.dhis.rules.models.RuleEffect;

import io.reactivex.Flowable;

interface RuleEngineRepository {

    @NonNull
    Flowable<Result<RuleEffect>> calculate();
}
