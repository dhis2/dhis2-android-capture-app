package org.dhis2.usescases.programStageSelection;

import android.database.Cursor;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.forms.RulesRepository;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.EventCreationType;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
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
import java.util.Objects;

import javax.annotation.Nonnull;

import androidx.annotation.NonNull;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;

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

    private static final String QUERY_EVENT = "SELECT Event.uid,\n" +
            "  Event.programStage,\n" +
            "  Event.status,\n" +
            "  Event.eventDate,\n" +
            "  Event.dueDate,\n" +
            "  Event.organisationUnit,\n" +
            "  ProgramStage.displayName\n" +
            "FROM Event\n" +
            "JOIN ProgramStage ON ProgramStage.uid = Event.programStage\n" +
            "WHERE Event.enrollment = ?\n" +
            " AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'" +
            " AND " + EventModel.TABLE + "." + EventModel.Columns.STATUS + " IN ( '" + EventStatus.ACTIVE.name() + ", " + EventStatus.COMPLETED.name() + "' )";


    private static final String QUERY_VALUES = "SELECT " +
            "  Event.eventDate," +
            "  Event.programStage," +
            "  TrackedEntityDataValue.dataElement," +
            "  TrackedEntityDataValue.value," +
            "  ProgramRuleVariable.useCodeForOptionSet," +
            "  Option.code," +
            "  Option.name" +
            " FROM TrackedEntityDataValue " +
            "  INNER JOIN Event ON TrackedEntityDataValue.event = Event.uid " +
            "  INNER JOIN DataElement ON DataElement.uid = TrackedEntityDataValue.dataElement " +
            "  LEFT JOIN ProgramRuleVariable ON ProgramRuleVariable.dataElement = DataElement.uid " +
            "  LEFT JOIN Option ON (Option.optionSet = DataElement.optionSet AND Option.code = TrackedEntityDataValue.value) " +
            " WHERE Event.uid = ? AND value IS NOT NULL AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "';";

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
        this.cachedRuleEngineFlowable =
                Flowable.zip(
                        rulesRepository.rulesNew(programUid),
                        rulesRepository.ruleVariablesProgramStages(programUid),
                        ruleEvents(enrollmentUid),
                        (rules, variables, ruleEvents) -> {
                            RuleEngine.Builder builder = RuleEngineContext.builder(evaluator)
                                    .rules(rules)
                                    .ruleVariables(variables)
                                    .calculatedValueMap(new HashMap<>())
                                    .supplementaryData(new HashMap<>())
                                    .build().toEngineBuilder();
                            return builder.events(ruleEvents)
                                    .triggerEnvironment(TriggerEnvironment.ANDROIDCLIENT)
                                    .build();
                        })
                        .cacheWithInitialCapacity(1);
    }

    private Flowable<List<RuleEvent>> ruleEvents(String enrollmentUid) {
        return briteDatabase.createQuery(EventModel.TABLE, QUERY_EVENT, enrollmentUid == null ? "" : enrollmentUid)
                .mapToList(cursor -> {
                    List<RuleDataValue> dataValues = new ArrayList<>();
                    String eventUid = cursor.getString(0);
                    String programStageUid = cursor.getString(1);
                    Date eventDate = DateUtils.databaseDateFormat().parse(cursor.getString(3));
                    Date dueDate = cursor.isNull(4) ? eventDate : DateUtils.databaseDateFormat().parse(cursor.getString(4));
                    String orgUnit = cursor.getString(5);
                    String orgUnitCode = getOrgUnitCode(orgUnit);
                    String programStageName = cursor.getString(6);
                    String eventStatus;
                    if (cursor.getString(2).equals(EventStatus.VISITED.name()))
                        eventStatus = EventStatus.ACTIVE.name();
                    else
                        eventStatus = cursor.getString(2);

                    RuleEvent.Status status = RuleEvent.Status.valueOf(eventStatus);

                    try (Cursor dataValueCursor = briteDatabase.query(QUERY_VALUES, eventUid == null ? "" : eventUid)) {
                        if (dataValueCursor != null && dataValueCursor.moveToFirst()) {
                            for (int i = 0; i < dataValueCursor.getCount(); i++) {
                                Date eventDateV = DateUtils.databaseDateFormat().parse(cursor.getString(0));
                                String programStage = cursor.getString(1);
                                String dataElement = cursor.getString(2);
                                String value = cursor.getString(3) != null ? cursor.getString(3) : "";
                                Boolean useCode = cursor.getInt(4) == 1;
                                String optionCode = cursor.getString(5);
                                String optionName = cursor.getString(6);
                                if (!isEmpty(optionCode) && !isEmpty(optionName))
                                    value = useCode ? optionCode : optionName; //If de has optionSet then check if value should be code or name for program rules
                                dataValues.add(RuleDataValue.create(eventDateV, programStage,
                                        dataElement, value));
                                dataValueCursor.moveToNext();
                            }
                        }
                    }

                    return RuleEvent.builder()
                            .event(eventUid)
                            .programStage(programStageUid)
                            .programStageName(programStageName)
                            .status(status)
                            .eventDate(eventDate)
                            .dueDate(dueDate)
                            .organisationUnit(orgUnit)
                            .organisationUnitCode(orgUnitCode)
                            .dataValues(dataValues)
                            .build();

                }).toFlowable(BackpressureStrategy.LATEST);
    }

    private Flowable<RuleEnrollment> ruleEnrollment(String enrollmentUid) {
        return briteDatabase.createQuery(Arrays.asList(EnrollmentModel.TABLE,
                TrackedEntityAttributeValueModel.TABLE), QUERY_ATTRIBUTE_VALUES, enrollmentUid == null ? "" : enrollmentUid)
                .mapToList(cursor -> RuleAttributeValue.create(
                        cursor.getString(0), cursor.getString(1))
                ).toFlowable(BackpressureStrategy.LATEST)
                .flatMap(attributeValues -> {

                    Enrollment enrollment = d2.enrollmentModule().enrollments.byUid().eq(enrollmentUid == null ? "" : enrollmentUid).one().get();
                    String programName = d2.programModule().programs.byUid().eq(enrollment.program()).one().get().displayName();
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
        String ouCode = d2.organisationUnitModule().organisationUnits.byUid().eq(orgUnitUid).one().get().code();
        return ouCode == null ? "" : ouCode;
    }

    @NonNull
    @Override
    public Flowable<List<ProgramStage>> enrollmentProgramStages(String programId, String enrollmentUid) {
        List<String> currentProgramStages = new ArrayList<>();
        List<ProgramStage> selectableStages = new ArrayList<>();
        List<Event> events = d2.eventModule().events.byEnrollmentUid().eq(enrollmentUid == null ? "" : enrollmentUid).byState().neq(State.TO_DELETE).get();
        for (Event event : events)
            currentProgramStages.add(event.programStage());

        return Observable.just(!Objects.equals(eventCreationType, EventCreationType.SCHEDULE.name()) ?
                d2.programModule().programStages.byProgramUid().eq(programId).withStyle().get() :
                d2.programModule().programStages.byProgramUid().eq(programId).withStyle().byHideDueDate().eq(false).get())
                .map(programStages -> {
                    boolean isSelectable;
                    for (ProgramStage programStage : programStages) {
                        isSelectable = true;
                        for (String enrollmentStage : currentProgramStages)
                            if (enrollmentStage.equals(programStage.uid()))
                                isSelectable = programStage.repeatable();
                        if (isSelectable)
                            selectableStages.add(programStage);
                    }
                    return selectableStages;
                })
                .toFlowable(BackpressureStrategy.LATEST);
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
}
