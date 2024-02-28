package org.dhis2.data.forms;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.commons.rules.RuleEngineContextData;
import org.dhis2.form.data.RulesRepository;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.rules.api.RuleEngineContext;

import io.reactivex.Flowable;
import io.reactivex.Single;

public class EventRepository implements FormRepository {

    private final String programUid;
    private final String orgUnit;

    @NonNull
    private Flowable<RuleEngineContextData> cachedRuleEngineFlowable;

    private RuleEngineContextData ruleEngineContextData = null;

    @Nullable
    private final String eventUid;
    private final RulesRepository rulesRepository;

    public EventRepository(
            @NonNull RulesRepository rulesRepository,
            @Nullable String eventUid,
            @NonNull D2 d2) {
        this.eventUid = eventUid != null ? eventUid : "";
        this.rulesRepository = rulesRepository;
        this.programUid = eventUid != null ? d2.eventModule().events().uid(eventUid).blockingGet().program() : "";
        this.orgUnit = !this.eventUid.isEmpty() ? d2.eventModule().events().uid(eventUid).blockingGet().organisationUnit() : "";
        // We don't want to rebuild RuleEngine on each request, since metadata of
        // the event is not changing throughout lifecycle of FormComponent.
        this.cachedRuleEngineFlowable = Single.zip(
                        rulesRepository.rulesNew(programUid, eventUid),
                        rulesRepository.ruleVariables(programUid),
                        rulesRepository.otherEvents(this.eventUid),
                        rulesRepository.enrollment(this.eventUid),
                        rulesRepository.queryConstants(),
                        rulesRepository.supplementaryData(orgUnit),
                        (rules, variables, events, enrollment, constants, supplementaryData) -> {
                            RuleEngineContext ruleEngineContext = new RuleEngineContext(
                                    rules,
                                    variables,
                                    supplementaryData,
                                    constants
                            );

                            return new RuleEngineContextData(
                                    ruleEngineContext,
                                    enrollment.getEnrollment().isEmpty() ? null : enrollment,
                                    events
                            );
                        })
                .doOnSuccess(contextData -> this.ruleEngineContextData = contextData)
                .toFlowable()
                .cacheWithInitialCapacity(1);
    }


    @Override
    public Flowable<RuleEngineContextData> restartRuleEngine() {
        return this.cachedRuleEngineFlowable = Single.zip(
                        rulesRepository.rulesNew(programUid, eventUid),
                        rulesRepository.ruleVariables(programUid),
                        rulesRepository.otherEvents(this.eventUid),
                        rulesRepository.enrollment(this.eventUid),
                        rulesRepository.queryConstants(),
                        rulesRepository.supplementaryData(orgUnit),
                        (rules, variables, events, enrollment, constants, supplementaryData) -> {
                            RuleEngineContext ruleEngineContext = new RuleEngineContext(
                                    rules,
                                    variables,
                                    supplementaryData,
                                    constants
                            );

                            return new RuleEngineContextData(
                                    ruleEngineContext,
                                    enrollment.getEnrollment().isEmpty() ? null : enrollment,
                                    events
                            );
                        })
                .doOnSuccess(contextData -> this.ruleEngineContextData = contextData)
                .toFlowable()
                .cacheWithInitialCapacity(1);
    }

    @NonNull
    @Override
    public Flowable<RuleEngineContextData> ruleEngine() {
        return ruleEngineContextData != null ? Flowable.just(ruleEngineContextData) : cachedRuleEngineFlowable;
    }
}