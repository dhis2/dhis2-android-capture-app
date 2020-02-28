package org.dhis2.usescases.programStageSelection;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.forms.RulesRepository;
import org.dhis2.utils.EventCreationType;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentTableInfo;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageCollectionRepository;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueTableInfo;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.rules.RuleEngine;
import org.hisp.dhis.rules.RuleEngineContext;
import org.hisp.dhis.rules.RuleExpressionEvaluator;
import org.hisp.dhis.rules.models.RuleAttributeValue;
import org.hisp.dhis.rules.models.RuleDataValue;
import org.hisp.dhis.rules.models.RuleEffect;
import org.hisp.dhis.rules.models.RuleEnrollment;
import org.hisp.dhis.rules.models.RuleEvent;
import org.hisp.dhis.rules.models.TriggerEnvironment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class ProgramStageSelectionRepositoryImpl implements ProgramStageSelectionRepository {

    private static final String QUERY_ATTRIBUTE_VALUES = "SELECT\n" +
            "  Field.id,\n" +
            "  Value.value,\n" +
            "  ProgramRuleVariable.useCodeForOptionSet,\n" +
            "  Option.code,\n" +
            "  Option.name\n" +
            "FROM (Enrollment INNER JOIN Program ON Program.uid = Enrollment.program)\n" +
            "  INNER JOIN (\n" +
            "      SELECT\n" +
            "        TrackedEntityAttribute.uid AS id,\n" +
            "        TrackedEntityAttribute.optionSet AS optionSet,\n" +
            "        ProgramTrackedEntityAttribute.program AS program\n" +
            "      FROM ProgramTrackedEntityAttribute INNER JOIN TrackedEntityAttribute\n" +
            "          ON TrackedEntityAttribute.uid = ProgramTrackedEntityAttribute.trackedEntityAttribute\n" +
            "    ) AS Field ON Field.program = Program.uid\n" +
            "  INNER JOIN TrackedEntityAttributeValue AS Value ON (\n" +
            "    Value.trackedEntityAttribute = Field.id\n" +
            "        AND Value.trackedEntityInstance = Enrollment.trackedEntityInstance)\n" +
            "  LEFT JOIN ProgramRuleVariable ON ProgramRuleVariable.trackedEntityAttribute = Field.id " +
            "  LEFT JOIN Option ON (Option.optionSet = Field.optionSet AND Option.code = Value.value) " +
            "WHERE Enrollment.uid = ? AND Value.value IS NOT NULL;";

    private final BriteDatabase briteDatabase;
    private final Flowable<RuleEngine> cachedRuleEngineFlowable;
    private final String enrollmentUid;
    private final String eventCreationType;
    private final D2 d2;

    ProgramStageSelectionRepositoryImpl(BriteDatabase briteDatabase, RuleExpressionEvaluator evaluator, RulesRepository rulesRepository, String programUid, String enrollmentUid, String eventCreationType, D2 d2) {
        this.briteDatabase = briteDatabase;
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
        return briteDatabase.createQuery(Arrays.asList(EnrollmentTableInfo.TABLE_INFO.name(),
                TrackedEntityAttributeValueTableInfo.TABLE_INFO.name()), QUERY_ATTRIBUTE_VALUES, enrollmentUid == null ? "" : enrollmentUid)
                .mapToList(cursor -> RuleAttributeValue.create(
                        cursor.getString(0), cursor.getString(1))
                ).toFlowable(BackpressureStrategy.LATEST)
                .flatMap(attributeValues -> {

                    Enrollment enrollment = d2.enrollmentModule().enrollments().byUid().eq(enrollmentUid == null ? "" : enrollmentUid).one().blockingGet();
                    String programName = d2.programModule().programs().byUid().eq(enrollment.program()).one().blockingGet().displayName();
                    Date enrollmentDate = enrollment.enrollmentDate();
                    Date incidentDate = enrollment.incidentDate() == null ? enrollmentDate : enrollment.incidentDate();
                    RuleEnrollment.Status status = RuleEnrollment.Status.valueOf(enrollment.status().name());

                    return Flowable.just(RuleEnrollment.create(
                            enrollment.uid(),
                            incidentDate,
                            enrollmentDate,
                            status,
                            enrollment.organisationUnit(),
                            getOrgUnitCode(enrollment.organisationUnit()),
                            attributeValues,
                            programName)
                    );
                });
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
