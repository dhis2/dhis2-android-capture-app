package org.dhis2.usescases.programStageSelection;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.forms.RulesRepository;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;
import org.hisp.dhis.rules.RuleEngine;
import org.hisp.dhis.rules.RuleEngineContext;
import org.hisp.dhis.rules.RuleExpressionEvaluator;
import org.hisp.dhis.rules.models.RuleAttributeValue;
import org.hisp.dhis.rules.models.RuleDataValue;
import org.hisp.dhis.rules.models.RuleEffect;
import org.hisp.dhis.rules.models.RuleEnrollment;
import org.hisp.dhis.rules.models.RuleEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class ProgramStageSelectionRepositoryImpl implements ProgramStageSelectionRepository {

    private final String PROGRAM_STAGE_QUERY = String.format("SELECT * FROM %s WHERE %s.%s = ? ORDER BY %s.%s ASC",
            ProgramStageModel.TABLE, ProgramStageModel.TABLE, ProgramStageModel.Columns.PROGRAM,
            ProgramStageModel.TABLE, ProgramStageModel.Columns.SORT_ORDER);

    private final String ENROLLMENT_PROGRAM_STAGES = "SELECT ProgramStage.* FROM ProgramStage " +
            "JOIN Program ON Program.uid = ProgramStage.program " +
            "JOIN Enrollment ON Enrollment.program = Program.uid " +
            "WHERE Enrollment.uid = ?";

    private final String CURRENT_PROGRAM_STAGES = "SELECT ProgramStage.* FROM ProgramStage WHERE ProgramStage.uid IN " +
            "(SELECT DISTINCT Event.programStage FROM Event WHERE Event.enrollment = ?) ORDER BY ProgramStage.sortOrder ASC";

    private static final String QUERY_ENROLLMENT = "SELECT\n" +
            "  Enrollment.uid,\n" +
            "  Enrollment.incidentDate,\n" +
            "  Enrollment.enrollmentDate,\n" +
            "  Enrollment.status,\n" +
            "  Enrollment.organisationUnit,\n" +
            "  Program.displayName\n" +
            "FROM Enrollment\n" +
            "JOIN Program ON Program.uid = Enrollment.program\n" +
            "WHERE Enrollment.uid = ? \n" +
            "LIMIT 1;";

    private static final String QUERY_ATTRIBUTE_VALUES = "SELECT\n" +
            "  Field.id,\n" +
            "  Value.value\n" +
            "FROM (Enrollment INNER JOIN Program ON Program.uid = Enrollment.program)\n" +
            "  INNER JOIN (\n" +
            "      SELECT\n" +
            "        TrackedEntityAttribute.uid AS id,\n" +
            "        ProgramTrackedEntityAttribute.program AS program\n" +
            "      FROM ProgramTrackedEntityAttribute INNER JOIN TrackedEntityAttribute\n" +
            "          ON TrackedEntityAttribute.uid = ProgramTrackedEntityAttribute.trackedEntityAttribute\n" +
            "    ) AS Field ON Field.program = Program.uid\n" +
            "  INNER JOIN TrackedEntityAttributeValue AS Value ON (\n" +
            "    Value.trackedEntityAttribute = Field.id\n" +
            "        AND Value.trackedEntityInstance = Enrollment.trackedEntityInstance)\n" +
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
            " AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'";

    private static final String QUERY_VALUES = "SELECT " +
            "  eventDate," +
            "  programStage," +
            "  dataElement," +
            "  value" +
            " FROM TrackedEntityDataValue " +
            "  INNER JOIN Event ON TrackedEntityDataValue.event = Event.uid " +
            " WHERE event = ? AND value IS NOT NULL AND " + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'";

    private final BriteDatabase briteDatabase;
    private final Flowable<RuleEngine> cachedRuleEngineFlowable;
    private final String enrollmentUid;

    ProgramStageSelectionRepositoryImpl(BriteDatabase briteDatabase, RuleExpressionEvaluator evaluator, RulesRepository rulesRepository, String programUid, String enrollmentUid) {
        this.briteDatabase = briteDatabase;
        this.enrollmentUid = enrollmentUid;
        this.cachedRuleEngineFlowable =
                Flowable.zip(
                        rulesRepository.rulesNew(programUid),
                        rulesRepository.ruleVariablesProgramStages(programUid),
                        ruleEvents(enrollmentUid),
                        (rules, variables, ruleEvents) ->
                                RuleEngineContext.builder(evaluator)
                                        .rules(rules)
                                        .ruleVariables(variables)
                                        .calculatedValueMap(new HashMap<>())
                                        .supplementaryData(new HashMap<>())
                                        .build().toEngineBuilder()
                                        .events(ruleEvents)
                                        .build())
                        .cacheWithInitialCapacity(1);
    }

    private Flowable<List<RuleEvent>> ruleEvents(String enrollmentUid) {
        return briteDatabase.createQuery(EventModel.TABLE, QUERY_EVENT, enrollmentUid == null ? "" : enrollmentUid)
                .mapToList(cursor -> {
                    List<RuleDataValue> dataValues = new ArrayList<>();
                    String eventUid = cursor.getString(0);
                    Date eventDate = DateUtils.databaseDateFormat().parse(cursor.getString(3));
                    Date dueDate = cursor.isNull(4) ? eventDate : DateUtils.databaseDateFormat().parse(cursor.getString(4));
                    String orgUnit = cursor.getString(5);
                    String programStage = cursor.getString(6);
                    String eventStatus;
                    if (cursor.getString(2).equals(EventStatus.VISITED.name()))
                        eventStatus = EventStatus.ACTIVE.name();
                    else
                        eventStatus = cursor.getString(2);

                    RuleEvent.Status status = RuleEvent.Status.valueOf(eventStatus);

                    Cursor dataValueCursor = briteDatabase.query(QUERY_VALUES, eventUid == null ? "" : eventUid);
                    if (dataValueCursor != null && dataValueCursor.moveToFirst()) {
                        for (int i = 0; i < dataValueCursor.getCount(); i++) {
                            Date eventDateV = DateUtils.databaseDateFormat().parse(dataValueCursor.getString(0));
                            String value = cursor.getString(3) != null ? dataValueCursor.getString(3) : "";
                            dataValues.add(RuleDataValue.create(eventDateV, dataValueCursor.getString(1),
                                    dataValueCursor.getString(2), value));
                        }
                    }

                    return RuleEvent.create(eventUid, cursor.getString(1),
                            status, eventDate, dueDate, orgUnit, dataValues,programStage);
                }).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    private Flowable<List<RuleDataValue>> queryDataValues(String eventUid) {
        return briteDatabase.createQuery(Arrays.asList(EventModel.TABLE,
                TrackedEntityDataValueModel.TABLE), QUERY_VALUES, eventUid == null ? "" : eventUid)
                .mapToList(cursor -> {
                    Date eventDate = DateUtils.databaseDateFormat().parse(cursor.getString(0));
                    String value = cursor.getString(3) != null ? cursor.getString(3) : "";
                    return RuleDataValue.create(eventDate, cursor.getString(1),
                            cursor.getString(2), value);
                }).toFlowable(BackpressureStrategy.LATEST);
    }

    private Flowable<RuleEnrollment> ruleEnrollemt(String enrollmentUid) {
        return briteDatabase.createQuery(Arrays.asList(EnrollmentModel.TABLE,
                TrackedEntityAttributeValueModel.TABLE), QUERY_ATTRIBUTE_VALUES, enrollmentUid == null ? "" : enrollmentUid)
                .mapToList(cursor -> RuleAttributeValue.create(
                        cursor.getString(0), cursor.getString(1))
                ).toFlowable(BackpressureStrategy.LATEST)
                .flatMap(attributeValues ->

                        briteDatabase.createQuery(EnrollmentModel.TABLE, QUERY_ENROLLMENT, enrollmentUid == null ? "" : enrollmentUid)
                                .mapToOne(cursor -> {
                                    Date enrollmentDate = DateUtils.databaseDateFormat().parse(cursor.getString(2));
                                    Date incidentDate = cursor.isNull(1) ?
                                            enrollmentDate : DateUtils.databaseDateFormat().parse(cursor.getString(1));
                                    RuleEnrollment.Status status = RuleEnrollment.Status
                                            .valueOf(cursor.getString(3));
                                    String orgUnit = cursor.getString(4);
                                    String programName = cursor.getString(5);

                                    return RuleEnrollment.create(cursor.getString(0),
                                            incidentDate, enrollmentDate, status, orgUnit, attributeValues, programName);
                                }).toFlowable(BackpressureStrategy.LATEST)
                );
    }

    @NonNull
    @Override
    public Observable<List<ProgramStageModel>> getProgramStages(String programUid) {
        return briteDatabase.createQuery(ProgramStageModel.TABLE, PROGRAM_STAGE_QUERY, programUid == null ? "" : programUid)
                .mapToList(ProgramStageModel::create);
    }

    @NonNull
    @Override
    public Flowable<List<ProgramStageModel>> enrollmentProgramStages(String programId, String enrollmentUid) {
        List<ProgramStageModel> enrollmentStages = new ArrayList<>();
        List<ProgramStageModel> selectableStages = new ArrayList<>();
        return briteDatabase.createQuery(ProgramStageModel.TABLE, /*ENROLLMENT_PROGRAM_STAGES*/CURRENT_PROGRAM_STAGES, enrollmentUid == null ? "" : enrollmentUid)
                .mapToList(ProgramStageModel::create)
                .flatMap(data -> {
                    enrollmentStages.addAll(data);
                    return briteDatabase.createQuery(ProgramStageModel.TABLE, PROGRAM_STAGE_QUERY, programId == null ? "" : programId)
                            .mapToList(ProgramStageModel::create);
                })
                .map(data -> {
                    boolean isSelectable;
                    for (ProgramStageModel programStage : data) {
                        isSelectable = true;
                        for (ProgramStageModel enrollmentStage : enrollmentStages) {
                            if (enrollmentStage.uid().equals(programStage.uid())) {
                                isSelectable = programStage.repeatable();
                            }
                        }

                        if (isSelectable)
                            selectableStages.add(programStage);
                    }
                    return selectableStages;
                }).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<Result<RuleEffect>> calculate() {

        return ruleEnrollemt(enrollmentUid)
                .flatMap(enrollment ->
                        cachedRuleEngineFlowable
                                .switchMap(ruleEngine -> Flowable.fromCallable(ruleEngine.evaluate(enrollment))
                                        .map(Result::success)
                                        .onErrorReturn(error -> Result.failure(new Exception(error)))
                                )
                );
    }


}
