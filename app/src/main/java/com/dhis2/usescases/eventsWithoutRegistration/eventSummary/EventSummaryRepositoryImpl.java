package com.dhis2.usescases.eventsWithoutRegistration.eventSummary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dhis2.R;
import com.dhis2.data.forms.FormRepository;
import com.dhis2.data.forms.FormSectionViewModel;
import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.dhis2.data.forms.dataentry.fields.FieldViewModelFactory;
import com.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl;
import com.dhis2.utils.DateUtils;
import com.dhis2.utils.Result;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;
import org.hisp.dhis.rules.models.RuleDataValue;
import org.hisp.dhis.rules.models.RuleEffect;
import org.hisp.dhis.rules.models.RuleEvent;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;

/**
 * Created by Cristian on 22/03/2018.
 */

public class EventSummaryRepositoryImpl implements EventSummaryRepository {


    private final FieldViewModelFactory fieldFactory;

    @NonNull
    private final BriteDatabase briteDatabase;

    @NonNull
    private final FormRepository formRepository;

    @Nullable
    private final String eventUid;


    private static final List<String> SECTION_TABLES = Arrays.asList(
            EventModel.TABLE, ProgramModel.TABLE, ProgramStageModel.TABLE, ProgramStageSectionModel.TABLE);
    private static final String SELECT_SECTIONS = "SELECT\n" +
            "  Program.uid AS programUid,\n" +
            "  ProgramStage.uid AS programStageUid,\n" +
            "  ProgramStageSection.uid AS programStageSectionUid,\n" +
            "  ProgramStageSection.displayName AS programStageSectionDisplayName,\n" +
            "  ProgramStage.displayName AS programStageDisplayName,\n" +
            "  ProgramStageSection.mobileRenderType AS renderType\n" +
            "FROM Event\n" +
            "  JOIN Program ON Event.program = Program.uid\n" +
            "  JOIN ProgramStage ON Event.programStage = ProgramStage.uid\n" +
            "  LEFT OUTER JOIN ProgramStageSection ON ProgramStageSection.programStage = Event.programStage\n" +
            "WHERE Event.uid = ?\n" +
            "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'";

    private static final String QUERY = "SELECT\n" +
            "  Field.id,\n" +
            "  Field.label,\n" +
            "  Field.type,\n" +
            "  Field.mandatory,\n" +
            "  Field.optionSet,\n" +
            "  Value.value,\n" +
            "  Option.name,\n" +
            "  Field.section,\n" +
            "  Field.allowFutureDate,\n" +
            "  Event.status,\n" +
            "  Field.formLabel\n" +
            "FROM Event\n" +
            "  LEFT OUTER JOIN (\n" +
            "      SELECT\n" +
            "        DataElement.displayName AS label,\n" +
            "        DataElement.formName AS formLabel,\n" +
            "        DataElement.valueType AS type,\n" +
            "        DataElement.uid AS id,\n" +
            "        DataElement.optionSet AS optionSet,\n" +
            "        ProgramStageDataElement.sortOrder AS formOrder,\n" +
            "        ProgramStageDataElement.programStage AS stage,\n" +
            "        ProgramStageDataElement.compulsory AS mandatory,\n" +
            "        ProgramStageDataElement.programStageSection AS section,\n" +
            "        ProgramStageDataElement.allowFutureDate AS allowFutureDate\n" +
            "      FROM ProgramStageDataElement\n" +
            "        INNER JOIN DataElement ON DataElement.uid = ProgramStageDataElement.dataElement\n" +
            "    ) AS Field ON (Field.stage = Event.programStage)\n" +
            "  LEFT OUTER JOIN TrackedEntityDataValue AS Value ON (\n" +
            "    Value.event = Event.uid AND Value.dataElement = Field.id\n" +
            "  )\n" +
            "  LEFT OUTER JOIN Option ON (\n" +
            "    Field.optionSet = Option.optionSet AND Value.value = Option.code\n" +
            "  )\n" +
            " %s  " +
            "ORDER BY Field.formOrder ASC;";


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
            "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'\n" +
            "LIMIT 1;";

    private static final String QUERY_VALUES = "SELECT " +
            "  eventDate," +
            "  programStage," +
            "  dataElement," +
            "  value" +
            " FROM TrackedEntityDataValue " +
            "  INNER JOIN Event ON TrackedEntityDataValue.event = Event.uid " +
            " WHERE event = ? AND value IS NOT NULL AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "';";

    private static final String EVENT_QUERY = "SELECT * FROM Event WHERE Event.uid = ? LIMIT 1";
    private static final String PROGRAM_QUERY = "SELECT * FROM Program JOIN ProgramStage ON " +
            "ProgramStage.program = Program.uid JOIN Event On Event.programStage = ProgramStage.uid WHERE Event.uid = ?";

    private static final String ACCESS_QUERY = "SELECT ProgramStage.accessDataWrite FROM ProgramStage JOIN Event ON Event.programStage = ProgramStage.uid WHERE Event.uid = ? LIMIT 1";


    public EventSummaryRepositoryImpl(@NonNull Context context,
                                      @NonNull BriteDatabase briteDatabase,
                                      @NonNull FormRepository formRepository,
                                      @Nullable String eventUid) {
        this.briteDatabase = briteDatabase;
        this.formRepository = formRepository;
        this.eventUid = eventUid;
        fieldFactory = new FieldViewModelFactoryImpl(
                context.getString(R.string.enter_text),
                context.getString(R.string.enter_long_text),
                context.getString(R.string.enter_number),
                context.getString(R.string.enter_integer),
                context.getString(R.string.enter_positive_integer),
                context.getString(R.string.enter_negative_integer),
                context.getString(R.string.enter_positive_integer_or_zero),
                context.getString(R.string.filter_options),
                context.getString(R.string.choose_date));
    }

    @NonNull
    @Override
    public Flowable<List<FormSectionViewModel>> programStageSections(String eventUid) {
        return briteDatabase
                .createQuery(SECTION_TABLES, SELECT_SECTIONS, eventUid == null ? "" : eventUid)
                .mapToList(cursor -> mapToFormSectionViewModels(eventUid, cursor))
                .distinctUntilChanged().toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    private FormSectionViewModel mapToFormSectionViewModels(@NonNull String eventUid, @NonNull Cursor cursor) {
        // GET PROGRAMSTAGE DISPLAYNAME IN CASE THERE ARE NO SECTIONS
        if (cursor.getString(2) == null) {
            // This programstage has no sections
            return FormSectionViewModel.createForProgramStageWithLabel(eventUid, cursor.getString(4), cursor.getString(1));
        } else {
            // This programstage has sections
            return FormSectionViewModel.createForSection(eventUid, cursor.getString(2), cursor.getString(3), cursor.getString(5));
        }
    }

    @NonNull
    @Override
    public Flowable<List<FieldViewModel>> list(String sectionUid, String eventUid) {
        return briteDatabase
                .createQuery(TrackedEntityDataValueModel.TABLE, prepareStatement(sectionUid, eventUid))
                .mapToList(this::transform)
                .toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    @SuppressFBWarnings("VA_FORMAT_STRING_USES_NEWLINE")
    private String prepareStatement(String sectionUid, String eventUid) {
        String where;
        if (isEmpty(sectionUid)) {
            where = String.format(Locale.US, "WHERE Event.uid = '%s'", eventUid == null ? "" : eventUid);
        } else {
            where = String.format(Locale.US, "WHERE Event.uid = '%s' AND " +
                    "Field.section = '%s'", eventUid == null ? "" : eventUid, sectionUid == null ? "" : sectionUid);
        }

        return String.format(Locale.US, QUERY, where);
    }

    @NonNull
    private FieldViewModel transform(@NonNull Cursor cursor) {
        String dataValue = cursor.getString(5);
        String optionCodeName = cursor.getString(6);
        EventStatus eventStatus = EventStatus.valueOf(cursor.getString(9));
        String formName = cursor.getString(10);
        if (!isEmpty(optionCodeName)) {
            dataValue = optionCodeName;
        }

        return fieldFactory.create(cursor.getString(0), formName == null ? cursor.getString(1) : formName,
                ValueType.valueOf(cursor.getString(2)), cursor.getInt(3) == 1,
                cursor.getString(4), dataValue, cursor.getString(7), cursor.getInt(8) == 1,
                eventStatus == EventStatus.ACTIVE, null);
    }

    @NonNull
    @Override
    public Flowable<Result<RuleEffect>> calculate() {
        return queryDataValues(eventUid)
                .switchMap(this::queryEvent)
                .switchMap(
                        event -> formRepository.ruleEngine()
                                .switchMap(ruleEngine -> Flowable.fromCallable(ruleEngine.evaluate(event))
                                        .map(Result::success)
                                        .onErrorReturn(error -> Result.failure(new Exception(error)))
                                )
                );
    }

    @Override
    public Observable<EventModel> changeStatus(String eventUid) {
        String lastUpdated = DateUtils.databaseDateFormat().format(DateUtils.getInstance().getToday());
        Cursor cursor = briteDatabase.query(EVENT_QUERY, eventUid);
        if (cursor != null && cursor.moveToNext()) {

            EventModel event = EventModel.create(cursor);
            cursor.close();

            ContentValues values = event.toContentValues();
            switch (event.status()) {
                case ACTIVE:
                    values.put(EventModel.Columns.STATUS, EventStatus.COMPLETED.name());
                    break;
                case SKIPPED:
                    values.put(EventModel.Columns.STATUS, EventStatus.COMPLETED.name()); //TODO: Can this happen?
                    break;
                case VISITED:
                    values.put(EventModel.Columns.STATUS, EventStatus.COMPLETED.name()); //TODO: Can this happen?
                    break;
                case SCHEDULE:
                    values.put(EventModel.Columns.STATUS, EventStatus.COMPLETED.name()); //TODO: Can this happen?
                    break;
                case COMPLETED:
                    values.put(EventModel.Columns.STATUS, EventStatus.ACTIVE.name()); //TODO: This should check dates?
                    break;
            }


            values.put(EventModel.Columns.STATE, State.TO_UPDATE.toString());
            values.put(EventModel.Columns.LAST_UPDATED, lastUpdated);

            if (briteDatabase.update(EventModel.TABLE, values,
                    EventModel.Columns.UID + " = ?", eventUid == null ? "" : eventUid) <= 0) {

                throw new IllegalStateException(String.format(Locale.US, "Event=[%s] " +
                        "has not been successfully updated", event.uid()));
            }

            Cursor programCursor = briteDatabase.query(PROGRAM_QUERY, eventUid == null ? "" : eventUid);
            if (programCursor != null && cursor.moveToNext()) {
                ProgramModel program = ProgramModel.create(programCursor);
                programCursor.close();
                ContentValues programValues = program.toContentValues();
                values.put(ProgramModel.Columns.LAST_UPDATED, lastUpdated);
                if (briteDatabase.update(ProgramModel.TABLE, programValues,
                        ProgramModel.Columns.UID + " = ?", program.uid() == null ? "" : program.uid()) <= 0) {

                    throw new IllegalStateException(String.format(Locale.US, "Program=[%s] " +
                            "has not been successfully updated", event.uid()));
                }
            }
            return Observable.just(event);
        } else
            return null;
    }

    @Override
    public Flowable<EventModel> getEvent(String eventId) {
        return briteDatabase.createQuery(EventModel.TABLE, EVENT_QUERY, eventId == null ? "" : eventId)
                .mapToOne(EventModel::create).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Observable<Boolean> accessDataWrite(String eventId) {
        return briteDatabase.createQuery(ProgramStageModel.TABLE, ACCESS_QUERY, eventId == null ? "" : eventId)
                .mapToOne(cursor -> cursor.getInt(0) == 1);
    }

    @NonNull
    private Flowable<RuleEvent> queryEvent(@NonNull List<RuleDataValue> dataValues) {
        return briteDatabase.createQuery(EventModel.TABLE, QUERY_EVENT, eventUid == null ? "" : eventUid)
                .mapToOne(cursor -> {
                    Date eventDate = parseDate(cursor.getString(3));
                    Date dueDate = cursor.isNull(4) ? eventDate : parseDate(cursor.getString(4));
                    String orgUnit = cursor.getString(5);
                    String programStage = cursor.getString(6);
                    RuleEvent.Status status = RuleEvent.Status.valueOf(cursor.getString(2));
                    return RuleEvent.create(cursor.getString(0), cursor.getString(1),
                            status, eventDate, dueDate, dataValues);
                }).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    private Flowable<List<RuleDataValue>> queryDataValues(String eventUid) {
        return briteDatabase.createQuery(Arrays.asList(EventModel.TABLE,
                TrackedEntityDataValueModel.TABLE), QUERY_VALUES, eventUid == null ? "" : eventUid)
                .mapToList(cursor -> {
                    Date eventDate = parseDate(cursor.getString(0));
                    return RuleDataValue.create(eventDate, cursor.getString(1),
                            cursor.getString(2), cursor.getString(3));
                }).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    private static Date parseDate(@NonNull String date) {
        try {
            return BaseIdentifiableObject.DATE_FORMAT.parse(date);
        } catch (ParseException parseException) {
            throw new RuntimeException(parseException);
        }
    }
}