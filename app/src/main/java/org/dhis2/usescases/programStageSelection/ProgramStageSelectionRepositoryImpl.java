package org.dhis2.usescases.programStageSelection;

import androidx.annotation.NonNull;

import org.dhis2.Bindings.RuleExtensionsKt;
import org.dhis2.data.forms.RulesRepository;
import org.dhis2.utils.EventCreationType;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageCollectionRepository;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.rules.RuleEngine;
import org.hisp.dhis.rules.RuleEngineContext;
import org.hisp.dhis.rules.RuleExpressionEvaluator;
import org.hisp.dhis.rules.models.RuleEffect;
import org.hisp.dhis.rules.models.RuleEnrollment;
import org.hisp.dhis.rules.models.TriggerEnvironment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class ProgramStageSelectionRepositoryImpl implements ProgramStageSelectionRepository {

    private final Flowable<RuleEngine> cachedRuleEngineFlowable;
    private final String enrollmentUid;
    private final String eventCreationType;
    private final D2 d2;

    ProgramStageSelectionRepositoryImpl(RuleExpressionEvaluator evaluator, RulesRepository rulesRepository, String programUid, String enrollmentUid, String eventCreationType, D2 d2) {
        this.enrollmentUid = enrollmentUid;
        this.eventCreationType = eventCreationType;
        this.d2 = d2;
        String orgUnitUid = d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet().organisationUnit();
        this.cachedRuleEngineFlowable =
                Single.zip(
                        rulesRepository.rulesNew(programUid),
                        rulesRepository.ruleVariablesProgramStages(programUid),
                        rulesRepository.enrollmentEvents(enrollmentUid),
                        rulesRepository.supplementaryData(orgUnitUid),
                        rulesRepository.queryConstants(),
                        (rules, variables, ruleEvents, supplementaryData, constants) -> {
                            RuleEngine.Builder builder = RuleEngineContext.builder(evaluator)
                                    .rules(rules)
                                    .ruleVariables(variables)
                                    .calculatedValueMap(new HashMap<>())
                                    .constantsValue(constants)
                                    .supplementaryData(supplementaryData)
                                    .build().toEngineBuilder();
                            return builder.events(ruleEvents)
                                    .triggerEnvironment(TriggerEnvironment.ANDROIDCLIENT)
                                    .build();
                        }).toFlowable()
                        .cacheWithInitialCapacity(1);
    }

    private Flowable<RuleEnrollment> ruleEnrollment(String enrollmentUid) {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid)
                .get()
                .flatMap(enrollment ->
                        d2.programModule().programTrackedEntityAttributes()
                                .byProgram().eq(enrollment.program())
                                .get()
                                .map(programTrackedEntityAttributes -> {
                                    List<String> attibuteUids = new ArrayList<>();
                                    for (ProgramTrackedEntityAttribute programTrackedEntityAttribute : programTrackedEntityAttributes) {
                                        attibuteUids.add(programTrackedEntityAttribute.trackedEntityAttribute().uid());
                                    }
                                    return attibuteUids;
                                })
                                .flatMap(attributes ->
                                        d2.trackedEntityModule().trackedEntityAttributeValues()
                                                .byTrackedEntityInstance().eq(enrollment.trackedEntityInstance())
                                                .byTrackedEntityAttribute().in(attributes)
                                                .get())
                                .map(attributeValues -> RuleEnrollment.create(
                                        enrollment.uid(),
                                        enrollment.incidentDate() == null ? enrollment.enrollmentDate() : enrollment.incidentDate(),
                                        enrollment.enrollmentDate(),
                                        RuleEnrollment.Status.valueOf(enrollment.status().name()),
                                        enrollment.organisationUnit(),
                                        getOrgUnitCode(enrollment.organisationUnit()),
                                        RuleExtensionsKt.toRuleAttributeValue(attributeValues, d2, enrollment.program()),
                                        d2.programModule().programs().uid(enrollment.program()).blockingGet().name()


                                ))).toFlowable();
    }

    @Nonnull
    private String getOrgUnitCode(String orgUnitUid) {
        String ouCode = d2.organisationUnitModule().organisationUnits().byUid().eq(orgUnitUid).one().blockingGet().code();
        return ouCode == null ? "" : ouCode;
    }

    @NonNull
    @Override
    public Flowable<List<ProgramStage>> enrollmentProgramStages(String programId, String enrollmentUid) {
        return d2.eventModule().events().byEnrollmentUid().eq(enrollmentUid == null ? "" : enrollmentUid).byDeleted().isFalse().get()
                .toFlowable().flatMapIterable(events -> events)
                .map(event -> event.programStage())
                .toList()
                .flatMap(currentProgramStagesUids -> {
                    ProgramStageCollectionRepository repository = d2.programModule().programStages().byProgramUid().eq(programId);
                    if (eventCreationType.equals(EventCreationType.SCHEDULE.name()))
                        repository = repository.byHideDueDate().eq(false);

                    return repository.get().toFlowable().flatMapIterable(stages -> stages)
                            .filter(programStage ->
                                    !currentProgramStagesUids.contains(programStage.uid()) ||
                                            programStage.repeatable())
                            .toList();

                }).toFlowable();
    }

    @Override
    public Flowable<Result<RuleEffect>> calculate() {
        return ruleEnrollment(enrollmentUid)
                .flatMap(enrollment ->
                        cachedRuleEngineFlowable
                                .switchMap(ruleEngine -> Flowable.fromCallable(ruleEngine.evaluate(enrollment))
                                        .map(Result::success)
                                        .onErrorReturn(error -> Result.failure(new Exception(error)))
                                )
                );
    }

    @Override
    public ProgramStage getStage(String programStageUid) {
        return d2.programModule().programStages().uid(programStageUid).blockingGet();
    }
}
