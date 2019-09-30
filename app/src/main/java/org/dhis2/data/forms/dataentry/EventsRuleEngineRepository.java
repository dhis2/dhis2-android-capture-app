package org.dhis2.data.forms.dataentry;

import android.database.Cursor;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.forms.FormRepository;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.event.EventTableInfo;
import org.hisp.dhis.rules.RuleEngine;
import org.hisp.dhis.rules.models.RuleDataValue;
import org.hisp.dhis.rules.models.RuleEffect;
import org.hisp.dhis.rules.models.RuleEvent;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

import static android.text.TextUtils.isEmpty;

public final class EventsRuleEngineRepository implements RuleEngineRepository {
    private static final String QUERY_EVENT = "SELECT Event.uid,\n" +
            "  Event.programStage,\n" +
            "  Event.status,\n" +
            "  Event.eventDate,\n" +
            "  Event.dueDate,\n" +
            "  Event.organisationUnit,\n" +
            "  ProgramStage.displayName\n" +
            "FROM Event\n" +
            "JOIN ProgramStage ON ProgramStage.uid = Event.programStage\n" +
            "WHERE Event.uid = ?\n" +
            " AND Event.deleted != 1'" +
            "LIMIT 1;";

    /*private static final String QUERY_VALUES = "SELECT " +
            "  eventDate," +
            "  programStage," +
            "  dataElement," +
            "  value" +
            " FROM TrackedEntityDataValue " +
            "  INNER JOIN Event ON TrackedEntityDataValue.event = Event.uid " +
            " WHERE event = ? AND value IS NOT NULL AND " + Event.Columns.STATE + " != '" + State.TO_DELETE + "'";*/

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
            " WHERE Event.uid = ? AND value IS NOT NULL AND Event.deleted != 1;";

    @NonNull
    private final BriteDatabase briteDatabase;

    @NonNull
    private final FormRepository formRepository;

    @NonNull
    private final String eventUid;

    public EventsRuleEngineRepository(@NonNull BriteDatabase briteDatabase,
                                      @NonNull FormRepository formRepository, @NonNull String eventUid) {
        this.briteDatabase = briteDatabase;
        this.formRepository = formRepository;
        this.eventUid = eventUid;
    }

    @Override
    public void updateRuleAttributeMap(String uid, String value) {

    }

    @Override
    public Flowable<RuleEngine> updateRuleEngine() {
        return formRepository.restartRuleEngine();
    }

    @NonNull
    @Override
    public Flowable<Result<RuleEffect>> calculate() {
        return queryDataValues()
                .switchMap(this::queryEvent)
                .switchMap(event -> formRepository.ruleEngine()
                        .switchMap(ruleEngine -> Flowable.fromCallable(ruleEngine.evaluate(event))
                                .map(Result::success)
                                .onErrorReturn(error -> Result.failure(new Exception(error)))
                        )
                );
    }

    @NonNull
    @Override
    public Flowable<Result<RuleEffect>> reCalculate() {
        return calculate();
    }

    @NonNull
    private Flowable<RuleEvent> queryEvent(@NonNull List<RuleDataValue> dataValues) {
        return briteDatabase.createQuery("Event", QUERY_EVENT, eventUid == null ? "" : eventUid)
                .mapToOne(cursor -> {
                    String eventUid = cursor.getString(0);
                    String programStageUid = cursor.getString(1);
                    RuleEvent.Status status = RuleEvent.Status.valueOf(cursor.getString(2));
                    Date eventDate = cursor.isNull(3) ? null : parseDate(cursor.getString(3));
                    Date dueDate = cursor.isNull(4) ? eventDate : parseDate(cursor.getString(4));
                    String orgUnit = cursor.getString(5);
                    String orgUnitCode = getOrgUnitCode(orgUnit);
                    String programStageName = cursor.getString(6);

                    return RuleEvent.builder()
                            .event(eventUid)
                            .programStage(programStageUid)
                            .programStageName(programStageName)
                            .status(status)
                            .eventDate(eventDate == null ? dueDate : eventDate)
                            .dueDate(dueDate)
                            .organisationUnit(orgUnit)
                            .organisationUnitCode(orgUnitCode)
                            .dataValues(dataValues)
                            .build();

                }).toFlowable(BackpressureStrategy.LATEST);
    }

    @Nonnull
    private String getOrgUnitCode(String orgUnitUid) {
        String ouCode = "";
        try (Cursor cursor = briteDatabase.query("SELECT code FROM OrganisationUnit WHERE uid = ? LIMIT 1", orgUnitUid)) {
            if (cursor != null && cursor.moveToFirst() && cursor.getString(0) != null) {
                ouCode = cursor.getString(0);
            }
        }
        return ouCode;
    }

    @NonNull
    private Flowable<List<RuleDataValue>> queryDataValues() {
        return briteDatabase.createQuery(Arrays.asList("Event",
                "TrackedEntityDataValue"), QUERY_VALUES, eventUid == null ? "" : eventUid)
                .mapToList(cursor -> {
                    Date eventDate = DateUtils.databaseDateFormat().parse(cursor.getString(0));
                    String programStage = cursor.getString(1);
                    String dataElement = cursor.getString(2);
                    String value = cursor.getString(3) != null ? cursor.getString(3) : "";
                    Boolean useCode = cursor.getInt(4) == 1;
                    String optionCode = cursor.getString(5);
                    String optionName = cursor.getString(6);
                    if (!isEmpty(optionCode) && !isEmpty(optionName))
                        value = useCode ? optionCode : optionName; //If de has optionSet then check if value should be code or name for program rules
                    return RuleDataValue.create(eventDate, programStage, dataElement, value);
                }).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    private static Date parseDate(@NonNull String date) throws ParseException {
        return BaseIdentifiableObject.DATE_FORMAT.parse(date);
    }
}
