package org.dhis2.data.forms.dataentry;

import static android.text.TextUtils.isEmpty;

import android.database.Cursor;

import org.dhis2.data.forms.FormRepository;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.rules.RuleEngine;
import org.hisp.dhis.rules.models.RuleDataValue;
import org.hisp.dhis.rules.models.RuleEffect;
import org.hisp.dhis.rules.models.RuleEvent;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nonnull;

import androidx.annotation.NonNull;
import io.reactivex.Flowable;

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
            " AND Event.deleted != 1\n" +
            "LIMIT 1;";

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
    private final D2 d2;

    @NonNull
    private final FormRepository formRepository;

    @NonNull
    private final String eventUid;

    public EventsRuleEngineRepository(@NonNull D2 d2,
                                      @NonNull FormRepository formRepository, @NonNull String eventUid) {
        this.d2 = d2;
        this.formRepository = formRepository;
        this.eventUid = eventUid;
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
        RuleEvent ruleEvent = null;

        Cursor cursor = d2.databaseAdapter().rawQuery(QUERY_EVENT, eventUid == null ? "" : eventUid);

        if (cursor != null && cursor.moveToFirst()) {

                String eventUid = cursor.getString(0);
                String programStageUid = cursor.getString(1);
                RuleEvent.Status status = RuleEvent.Status.valueOf(cursor.getString(2));
            Date eventDate = null;
            try {
                eventDate = cursor.isNull(3) ? null : parseDate(cursor.getString(3));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date dueDate = null;
            try {
                dueDate = cursor.isNull(4) ? eventDate : parseDate(cursor.getString(4));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            String orgUnit = cursor.getString(5);
                String orgUnitCode = getOrgUnitCode(orgUnit);
                String programStageName = cursor.getString(6);

                ruleEvent = RuleEvent.builder()
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

            cursor.close();
        }

        return Flowable.just(ruleEvent);
    }

    @Nonnull
    private String getOrgUnitCode(String orgUnitUid) {
        String ouCode = "";
        try (Cursor cursor = d2.databaseAdapter().rawQuery("SELECT code FROM OrganisationUnit WHERE uid = ? LIMIT 1", orgUnitUid)) {
            if (cursor != null && cursor.moveToFirst() && cursor.getString(0) != null) {
                ouCode = cursor.getString(0);
            }
        }
        return ouCode;
    }

    @NonNull
    private Flowable<List<RuleDataValue>> queryDataValues() {
        List<RuleDataValue> dataValues = new ArrayList<>();

        Cursor cursor = d2.databaseAdapter().rawQuery(QUERY_VALUES, eventUid == null ? "" : eventUid);

        if (cursor != null && cursor.moveToFirst()) {
            //add row to list
            while (cursor.moveToNext()) {
                Date eventDate = null;
                try {
                    eventDate = BaseIdentifiableObject.DATE_FORMAT.parse(cursor.getString(0));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String programStage = cursor.getString(1);
                String dataElement = cursor.getString(2);
                String value = cursor.getString(3) != null ? cursor.getString(3) : "";
                Boolean useCode = cursor.getInt(4) == 1;
                String optionCode = cursor.getString(5);
                String optionName = cursor.getString(6);
                if (!isEmpty(optionCode) && !isEmpty(optionName))
                    value = useCode ? optionCode : optionName; //If de has optionSet then check if value should be code or name for program rules
                dataValues.add(RuleDataValue.create(eventDate, programStage, dataElement, value));
            }

            cursor.close();
        }

        return Flowable.fromIterable(dataValues).toList().toFlowable();
    }

    @NonNull
    private static Date parseDate(@NonNull String date) throws ParseException {
        return BaseIdentifiableObject.DATE_FORMAT.parse(date);
    }
}
