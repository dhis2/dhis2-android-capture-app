package org.dhis2.data.forms;

import androidx.annotation.NonNull;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.rules.RuleEngine;
import org.hisp.dhis.rules.RuleEngineContext;
import org.hisp.dhis.rules.RuleExpressionEvaluator;
import org.hisp.dhis.rules.models.TriggerEnvironment;

import java.util.HashMap;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

@SuppressWarnings({
        "PMD.AvoidDuplicateLiterals"
})
public class EnrollmentFormRepository implements FormRepository {

    @NonNull
    private Flowable<RuleEngine> cachedRuleEngineFlowable;

    @NonNull
    private final String enrollmentUid;
    private final D2 d2;
    private final RulesRepository rulesRepository;
    private final RuleExpressionEvaluator expressionEvaluator;

    public EnrollmentFormRepository(@NonNull RuleExpressionEvaluator expressionEvaluator,
                                    @NonNull RulesRepository rulesRepository,
                                    @NonNull String enrollmentUid,
                                    @NonNull D2 d2) {
        this.d2 = d2;
        this.enrollmentUid = enrollmentUid;
        this.rulesRepository = rulesRepository;
        this.expressionEvaluator = expressionEvaluator;
        String orgUnit = d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet().organisationUnit();
        // We don't want to rebuild RuleEngine on each request, since metadata of
        // the event is not changing throughout lifecycle of FormComponent.
        this.cachedRuleEngineFlowable = enrollmentProgram()
                .switchMap(program -> Single.zip(
                        rulesRepository.rulesNew(program).subscribeOn(Schedulers.io()),
                        rulesRepository.ruleVariables(program).subscribeOn(Schedulers.io()),
                        rulesRepository.enrollmentEvents(enrollmentUid).subscribeOn(Schedulers.io()),
                        rulesRepository.queryConstants().subscribeOn(Schedulers.io()),
                        rulesRepository.supplementaryData(orgUnit).subscribeOn(Schedulers.io()),
                        (rules, variables, events, constants, supplementaryData) -> {
                            RuleEngine.Builder builder = RuleEngineContext.builder(expressionEvaluator)
                                    .rules(rules)
                                    .ruleVariables(variables)
                                    .calculatedValueMap(new HashMap<>())
                                    .supplementaryData(supplementaryData)
                                    .constantsValue(constants)
                                    .build().toEngineBuilder();
                            builder.triggerEnvironment(TriggerEnvironment.ANDROIDCLIENT);
                            builder.events(events);
                            return builder.build();
                        }).toFlowable())
                .cacheWithInitialCapacity(1);
    }

    @Override
    public Flowable<RuleEngine> restartRuleEngine() {
        String orgUnit = d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet().organisationUnit();
        return this.cachedRuleEngineFlowable = enrollmentProgram()
                .switchMap(program -> Single.zip(
                        rulesRepository.rulesNew(program),
                        rulesRepository.ruleVariables(program),
                        rulesRepository.enrollmentEvents(enrollmentUid),
                        rulesRepository.queryConstants(),
                        rulesRepository.supplementaryData(orgUnit),
                        (rules, variables, events, constants, supplementaryData) -> {
                            RuleEngine.Builder builder = RuleEngineContext.builder(expressionEvaluator)
                                    .rules(rules)
                                    .ruleVariables(variables)
                                    .calculatedValueMap(new HashMap<>())
                                    .supplementaryData(supplementaryData)
                                    .constantsValue(constants)
                                    .build().toEngineBuilder();
                            builder.triggerEnvironment(TriggerEnvironment.ANDROIDCLIENT);
                            builder.events(events);
                            return builder.build();
                        }).toFlowable())
                .cacheWithInitialCapacity(1);
    }

    @NonNull
    @Override
    public Flowable<RuleEngine> ruleEngine() {
        return cachedRuleEngineFlowable;
    }

    @NonNull
    private Flowable<String> enrollmentProgram() {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
                .map(Enrollment::program)
                .toFlowable();
    }
}