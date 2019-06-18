package org.dhis2.usescases.eventsWithoutRegistration.eventSummary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.R;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRenderingModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
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

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by Cristian on 22/03/2018.
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
            "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "' ORDER BY ProgramStageSection.sortOrder";

    private static final String QUERY = "SELECT\n" +
            "  Field.id,\n" +
            "  Field.label,\n" +
            "  Field.type,\n" +
            "  Field.mandatory,\n" +
            "  Field.optionSet,\n" +
            "  Value.value,\n" +
            "  Option.displayName,\n" +
            "  Field.section,\n" +
            "  Field.allowFutureDate,\n" +
            "  Event.status,\n" +
            "  Field.formLabel,\n" +
            "  Field.displayDescription,\n" +
            "  Field.formOrder,\n" +
            "  Field.sectionOrder\n" +
            "FROM Event\n" +
            "  LEFT OUTER JOIN (\n" +
            "      SELECT\n" +
            "        DataElement.displayName AS label,\n" +
            "        DataElement.displayFormName AS formLabel,\n" +
            "        DataElement.valueType AS type,\n" +
            "        DataElement.uid AS id,\n" +
            "        DataElement.optionSet AS optionSet,\n" +
            "        ProgramStageDataElement.sortOrder AS formOrder,\n" +
            "        ProgramStageDataElement.programStage AS stage,\n" +
            "        ProgramStageDataElement.compulsory AS mandatory,\n" +
            "        ProgramStageSectionDataElementLink.programStageSection AS section,\n" +
            "        ProgramStageDataElement.allowFutureDate AS allowFutureDate,\n" +
            "        DataElement.displayDescription AS displayDescription,\n" +
            "        ProgramStageSectionDataElementLink.sortOrder AS sectionOrder\n" +
            "      FROM ProgramStageDataElement\n" +
            "        INNER JOIN DataElement ON DataElement.uid = ProgramStageDataElement.dataElement\n" +
            "        LEFT JOIN ProgramStageSection ON ProgramStageSection.programStage = ProgramStageDataElement.programStage\n" +
            "        LEFT JOIN ProgramStageSectionDataElementLink ON ProgramStageSectionDataElementLink.programStageSection = ProgramStageSection.uid AND ProgramStageSectionDataElementLink.dataElement = DataElement.uid\n" + "    ) AS Field ON (Field.stage = Event.programStage)\n" +
            "  LEFT OUTER JOIN TrackedEntityDataValue AS Value ON (\n" +
            "    Value.event = Event.uid AND Value.dataElement = Field.id\n" +
            "  )\n" +
            "  LEFT OUTER JOIN Option ON (\n" +
            "    Field.optionSet = Option.optionSet AND Value.value = Option.code\n" +
            "  )\n" +
            " %s  " +
            "ORDER BY CASE" +
            " WHEN Field.sectionOrder IS NULL THEN Field.formOrder" +
            " WHEN Field.sectionOrder IS NOT NULL THEN Field.sectionOrder" +
            " END ASC;";


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

   /* private static final String QUERY_VALUES = "SELECT " +
            "  eventDate," +
            "  programStage," +
            "  dataElement," +
            "  value" +
            " FROM TrackedEntityDataValue " +
            "  INNER JOIN Event ON TrackedEntityDataValue.event = Event.uid " +
            " WHERE event = ? AND value IS NOT NULL AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "';";*/

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

    private static final String EVENT_QUERY = "SELECT * FROM Event WHERE Event.uid = ? LIMIT 1";
    private static final String PROGRAM_QUERY = "SELECT * FROM Program JOIN ProgramStage ON " +
            "ProgramStage.program = Program.uid JOIN Event On Event.programStage = ProgramStage.uid WHERE Event.uid = ?";

    private static final String ACCESS_QUERY = "SELECT ProgramStage.accessDataWrite FROM ProgramStage JOIN Event ON Event.programStage = ProgramStage.uid WHERE Event.uid = ? LIMIT 1";
    private static final String PROGRAM_ACCESS_QUERY = "SELECT Program.accessDataWrite FROM Program JOIN Event ON Event.program = Program.uid WHERE Event.uid = ? LIMIT 1";
    private final D2 d2;


    public EventSummaryRepositoryImpl(@NonNull Context context,
                                      @NonNull BriteDatabase briteDatabase,
                                      @NonNull FormRepository formRepository,
                                      @Nullable String eventUid,
                                      @NonNull D2 d2) {
        this.briteDatabase = briteDatabase;
        this.formRepository = formRepository;
        this.eventUid = eventUid;
        this.d2 = d2;
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

    @Override
    public boolean isEnrollmentOpen() {
        boolean isEnrollmentOpen = true;
        try (Cursor enrollmentCursor = briteDatabase.query("SELECT Enrollment.* FROM Enrollment JOIN Event ON Event.enrollment = Enrollment.uid WHERE Event.uid = ?", eventUid)) {
            if (enrollmentCursor != null && enrollmentCursor.moveToFirst()) {
                EnrollmentModel enrollment = EnrollmentModel.create(enrollmentCursor);
                isEnrollmentOpen = enrollment.enrollmentStatus() == EnrollmentStatus.ACTIVE;
            }
        }
        return isEnrollmentOpen;
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
        String uid = cursor.getString(0);
        ValueType valueType = ValueType.valueOf(cursor.getString(2));
        String dataValue = cursor.getString(5);
        String optionCodeName = cursor.getString(6);
        EventStatus eventStatus = EventStatus.valueOf(cursor.getString(9));
        String formName = cursor.getString(10);
        String description = cursor.getString(11);
        String optionSet = cursor.getString(4);
        if (!isEmpty(optionCodeName)) {
            dataValue = optionCodeName;
        }

        int optionCount = 0;
        if (optionSet != null)
            try (Cursor countCursor = briteDatabase.query("SELECT COUNT (uid) FROM Option WHERE optionSet = ?", optionSet)) {
                if (countCursor != null && countCursor.moveToFirst())
                    optionCount = countCursor.getInt(0);
            } catch (Exception e) {
                Timber.e(e);
            }

        ValueTypeDeviceRenderingModel fieldRendering = null;
        try (Cursor rendering = briteDatabase.query("SELECT * FROM ValueTypeDeviceRendering WHERE uid = ?", uid)) {
            if (rendering != null && rendering.moveToFirst()) {
                fieldRendering = ValueTypeDeviceRenderingModel.create(rendering);
            }
        }

        ObjectStyleModel objectStyle = ObjectStyleModel.builder().build();
        try (Cursor objStyleCursor = briteDatabase.query("SELECT * FROM ObjectStyle WHERE uid = ?", uid)) {
            if (objStyleCursor != null && objStyleCursor.moveToFirst())
                objectStyle = ObjectStyleModel.create(objStyleCursor);
        }


        if (valueType == ValueType.ORGANISATION_UNIT && !isEmpty(dataValue)) {
            dataValue = dataValue + "_ou_" + d2.organisationUnitModule().organisationUnits.uid(dataValue).get().displayName();
        }

        return fieldFactory.create(uid, formName == null ? cursor.getString(1) : formName,
                valueType, cursor.getInt(3) == 1,
                optionSet, dataValue, cursor.getString(7), cursor.getInt(8) == 1,
                eventStatus == EventStatus.ACTIVE, null, description, fieldRendering, optionCount, objectStyle);
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
        try (Cursor cursor = briteDatabase.query(EVENT_QUERY, eventUid)) {
            if (cursor != null && cursor.moveToNext()) {

                EventModel event = EventModel.create(cursor);
                cursor.close();

                ContentValues values = event.toContentValues();
                switch (event.status()) {
                    case ACTIVE:
                        values.put(EventModel.Columns.STATUS, EventStatus.COMPLETED.name());
                        values.put(EventModel.Columns.COMPLETE_DATE, lastUpdated);
                        break;
                    case SKIPPED:
                        values.put(EventModel.Columns.STATUS, EventStatus.COMPLETED.name()); //TODO: Can this happen?
                        values.put(EventModel.Columns.COMPLETE_DATE, lastUpdated);
                        break;
                    case VISITED:
                        values.put(EventModel.Columns.STATUS, EventStatus.COMPLETED.name()); //TODO: Can this happen?
                        values.put(EventModel.Columns.COMPLETE_DATE, lastUpdated);
                        break;
                    case SCHEDULE:
                        values.put(EventModel.Columns.STATUS, EventStatus.COMPLETED.name()); //TODO: Can this happen?
                        values.put(EventModel.Columns.COMPLETE_DATE, lastUpdated);
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

                try (Cursor programCursor = briteDatabase.query(PROGRAM_QUERY, eventUid == null ? "" : eventUid)) {
                    if (programCursor != null && cursor.moveToNext()) {
                        ProgramModel program = ProgramModel.create(programCursor);
                        ContentValues programValues = program.toContentValues();
                        values.put(ProgramModel.Columns.LAST_UPDATED, lastUpdated);
                        if (briteDatabase.update(ProgramModel.TABLE, programValues,
                                ProgramModel.Columns.UID + " = ?", program.uid() == null ? "" : program.uid()) <= 0) {

                            throw new IllegalStateException(String.format(Locale.US, "Program=[%s] " +
                                    "has not been successfully updated", event.uid()));
                        }
                    }
                }
                return Observable.just(event);
            } else
                return null;
        }
    }

    @Override
    public Flowable<EventModel> getEvent(String eventId) {
        return briteDatabase.createQuery(EventModel.TABLE, EVENT_QUERY, eventId == null ? "" : eventId)
                .mapToOne(EventModel::create).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Observable<Boolean> accessDataWrite(String eventId) {
        return briteDatabase.createQuery(ProgramStageModel.TABLE, ACCESS_QUERY, eventId == null ? "" : eventId)
                .mapToOne(cursor -> cursor.getInt(0) == 1)
                .flatMap(programStageAccessDataWrite -> briteDatabase.createQuery(ProgramModel.TABLE, PROGRAM_ACCESS_QUERY, eventId == null ? "" : eventId)
                        .mapToOne(cursor -> (cursor.getInt(0) == 1) && programStageAccessDataWrite));
    }

    @NonNull
    private Flowable<RuleEvent> queryEvent(@NonNull List<RuleDataValue> dataValues) {
        return briteDatabase.createQuery(EventModel.TABLE, QUERY_EVENT, eventUid == null ? "" : eventUid)
                .mapToOne(cursor -> {
                    String eventUid = cursor.getString(0);
                    String programStageUid = cursor.getString(1);
                    Date eventDate = parseDate(cursor.getString(3));
                    Date dueDate = cursor.isNull(4) ? eventDate : parseDate(cursor.getString(4));
                    String orgUnit = cursor.getString(5);
                    String orgUnitCode = getOrgUnitCode(orgUnit);
                    String programStageName = cursor.getString(6);
                    RuleEvent.Status status = RuleEvent.Status.valueOf(cursor.getString(2));

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
    private Flowable<List<RuleDataValue>> queryDataValues(String eventUid) {
        return briteDatabase.createQuery(Arrays.asList(EventModel.TABLE,
                TrackedEntityDataValueModel.TABLE), QUERY_VALUES, eventUid == null ? "" : eventUid)
                .mapToList(cursor -> {
                    Date eventDate = DateUtils.databaseDateFormat().parse(cursor.getString(0));
                    String programStage = cursor.getString(1);
                    String dataElement = cursor.getString(2);
                    String value = cursor.getString(3) != null ? cursor.getString(3) : "";
                    boolean useCode = cursor.getInt(4) == 1;
                    String optionCode = cursor.getString(5);
                    String optionName = cursor.getString(6);
                    if (!isEmpty(optionCode) && !isEmpty(optionName))
                        value = useCode ? optionCode : optionName; //If de has optionSet then check if value should be code or name for program rules
                    return RuleDataValue.create(eventDate, programStage, dataElement, value);
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