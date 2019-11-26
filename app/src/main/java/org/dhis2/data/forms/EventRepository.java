package org.dhis2.data.forms;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.rules.RuleEngine;
import org.hisp.dhis.rules.RuleEngineContext;
import org.hisp.dhis.rules.RuleExpressionEvaluator;
import org.hisp.dhis.rules.models.TriggerEnvironment;

import java.util.HashMap;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

@SuppressWarnings({
        "PMD.AvoidDuplicateLiterals"
})
public class EventRepository implements FormRepository {

    private final String programUid;

    @NonNull
    private Flowable<RuleEngine> cachedRuleEngineFlowable;

    private RuleEngine ruleEngine = null;

    @Nullable
    private final String eventUid;
    private final RulesRepository rulesRepository;
    private final RuleExpressionEvaluator evaluator;

    private final String TAG = "ROGRAMRULEREPOSITORY";

    public EventRepository(
            @NonNull RuleExpressionEvaluator evaluator,
            @NonNull RulesRepository rulesRepository,
            @Nullable String eventUid,
            @NonNull D2 d2) {
        this.eventUid = eventUid != null ? eventUid : "";
        this.rulesRepository = rulesRepository;
        this.evaluator = evaluator;
        this.programUid = eventUid != null ? d2.eventModule().events().uid(eventUid).blockingGet().program() : "";

        // We don't want to rebuild RuleEngine on each request, since metadata of
        // the event is not changing throughout lifecycle of FormComponent.
        this.cachedRuleEngineFlowable = Single.zip(
                rulesRepository.rulesNew(programUid),
                rulesRepository.ruleVariables(programUid),
                rulesRepository.otherEvents(this.eventUid),
                rulesRepository.enrollment(this.eventUid),
                rulesRepository.queryConstants(),
                rulesRepository.supplementaryData(),
                (rules, variables, events, enrollment, constants, supplementaryData) -> {

                    RuleEngine.Builder builder = RuleEngineContext.builder(evaluator)
                            .rules(rules)
                            .ruleVariables(variables)
                            .constantsValue(constants)
                            .calculatedValueMap(new HashMap<>())
                            .supplementaryData(supplementaryData)
                            .build().toEngineBuilder();
                    builder.triggerEnvironment(TriggerEnvironment.ANDROIDCLIENT);
                    builder.events(events);
                    if (!isEmpty(enrollment.enrollment()))
                        builder.enrollment(enrollment);
                    return builder.build();
                })
                .doOnSuccess(ruleEngine -> {
                    this.ruleEngine = ruleEngine;
                    Timber.tag("ROGRAMRULEREPOSITORY").d("RULE ENGINE READY AT %s", Thread.currentThread().getName());
                }).toFlowable()
                .cacheWithInitialCapacity(1);
    }


    @Override
    public Flowable<RuleEngine> restartRuleEngine() {

        return this.cachedRuleEngineFlowable = Single.zip(
                rulesRepository.rulesNew(programUid).subscribeOn(Schedulers.io()),
                rulesRepository.ruleVariables(programUid).subscribeOn(Schedulers.io()),
                rulesRepository.otherEvents(eventUid).subscribeOn(Schedulers.io()),
                rulesRepository.enrollment(eventUid).subscribeOn(Schedulers.io()),
                rulesRepository.queryConstants().subscribeOn(Schedulers.io()),
                rulesRepository.supplementaryData().subscribeOn(Schedulers.io()),
                (rules, variables, events, enrollment, constants, supplementaryData) -> {

                    RuleEngine.Builder builder = RuleEngineContext.builder(evaluator)
                            .rules(rules)
                            .ruleVariables(variables)
                            .constantsValue(constants)
                            .calculatedValueMap(new HashMap<>())
                            .supplementaryData(supplementaryData)
                            .build().toEngineBuilder();
                    builder.triggerEnvironment(TriggerEnvironment.ANDROIDCLIENT);
                    builder.events(events);
                    if (!isEmpty(enrollment.enrollment()))
                        builder.enrollment(enrollment);
                    return builder.build();
                }).toFlowable()
                .cacheWithInitialCapacity(1);
    }

    @NonNull
    @Override
    public Flowable<RuleEngine> ruleEngine() {
        return ruleEngine != null ? Flowable.just(ruleEngine) : cachedRuleEngineFlowable;
    }
}