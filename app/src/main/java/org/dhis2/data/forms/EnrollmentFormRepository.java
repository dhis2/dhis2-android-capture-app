package org.dhis2.data.forms;

import android.content.ContentValues;
import android.database.Cursor;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.CodeGenerator;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.hisp.dhis.rules.RuleEngine;
import org.hisp.dhis.rules.RuleEngineContext;
import org.hisp.dhis.rules.RuleExpressionEvaluator;
import org.hisp.dhis.rules.models.TriggerEnvironment;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

import static org.dhis2.data.database.SqlConstants.ALL;
import static org.dhis2.data.database.SqlConstants.AND;
import static org.dhis2.data.database.SqlConstants.COMMA;
import static org.dhis2.data.database.SqlConstants.EQUAL;
import static org.dhis2.data.database.SqlConstants.FROM;
import static org.dhis2.data.database.SqlConstants.JOIN;
import static org.dhis2.data.database.SqlConstants.LIMIT_1;
import static org.dhis2.data.database.SqlConstants.ON;
import static org.dhis2.data.database.SqlConstants.ORDER_BY;
import static org.dhis2.data.database.SqlConstants.POINT;
import static org.dhis2.data.database.SqlConstants.PROGRAM_STAGE_AUTO_GENERATE_EVENT;
import static org.dhis2.data.database.SqlConstants.PROGRAM_STAGE_MIN_DAYS_FROM_START;
import static org.dhis2.data.database.SqlConstants.PROGRAM_STAGE_PERIOD_TYPE;
import static org.dhis2.data.database.SqlConstants.PROGRAM_STAGE_PROGRAM;
import static org.dhis2.data.database.SqlConstants.PROGRAM_STAGE_REPORT_DATE_TO_USE;
import static org.dhis2.data.database.SqlConstants.PROGRAM_STAGE_SORT_ORDER;
import static org.dhis2.data.database.SqlConstants.PROGRAM_STAGE_TABLE;
import static org.dhis2.data.database.SqlConstants.PROGRAM_STAGE_UID;
import static org.dhis2.data.database.SqlConstants.QUESTION_MARK;
import static org.dhis2.data.database.SqlConstants.SELECT;
import static org.dhis2.data.database.SqlConstants.SELECT_DISTINCT;
import static org.dhis2.data.database.SqlConstants.TABLE_POINT_FIELD_EQUALS;
import static org.dhis2.data.database.SqlConstants.WHERE;

@SuppressWarnings({
        "PMD.AvoidDuplicateLiterals"
})
class EnrollmentFormRepository implements FormRepository {
    private static final List<String> TITLE_TABLES = Arrays.asList(
            EnrollmentModel.TABLE, ProgramModel.TABLE);

    private static final String SELECT_TITLE =
            SELECT + ProgramModel.TABLE + POINT + ProgramModel.Columns.DISPLAY_NAME +
                    FROM + EnrollmentModel.TABLE +
                    JOIN + ProgramModel.TABLE +
                    ON + ProgramModel.TABLE + POINT + EnrollmentModel.Columns.PROGRAM +
                    EQUAL +
                    ProgramModel.TABLE + POINT + ProgramModel.Columns.UID +
                    WHERE + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.UID +
                    EQUAL + QUESTION_MARK +
                    LIMIT_1;

    private static final String SELECT_ENROLLMENT_UID =
            SELECT + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.UID +
                    FROM + EnrollmentModel.TABLE +
                    WHERE + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.UID +
                    EQUAL + QUESTION_MARK;

    private static final String SELECT_ENROLLMENT_STATUS =
            SELECT + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.STATE +
                    FROM + EnrollmentModel.TABLE +
                    WHERE + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.UID +
                    EQUAL + QUESTION_MARK +
                    LIMIT_1;

    private static final String SELECT_ENROLLMENT_DATE =
            SELECT + EnrollmentModel.TABLE + POINT + ALL +
                    FROM + EnrollmentModel.TABLE +
                    WHERE + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.UID +
                    EQUAL + QUESTION_MARK +
                    LIMIT_1;

    private static final String SELECT_ENROLLMENT_PROGRAM =
            SELECT + ProgramModel.TABLE + POINT + ALL +
                    FROM + ProgramModel.TABLE +
                    JOIN + EnrollmentModel.TABLE +
                    ON + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.PROGRAM +
                    EQUAL + QUESTION_MARK +
                    LIMIT_1;

    private static final String SELECT_INCIDENT_DATE =
            SELECT + EnrollmentModel.TABLE + POINT + ALL +
                    FROM + EnrollmentModel.TABLE +
                    WHERE + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.UID +
                    EQUAL + QUESTION_MARK +
                    LIMIT_1;

    private static final String SELECT_AUTO_GENERATE_PROGRAM_STAGE =
            SELECT + PROGRAM_STAGE_TABLE + POINT + PROGRAM_STAGE_UID + COMMA +
                    ProgramModel.TABLE + POINT + ProgramModel.Columns.UID + COMMA +
                    EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.ORGANISATION_UNIT + COMMA +
                    PROGRAM_STAGE_TABLE + POINT + PROGRAM_STAGE_MIN_DAYS_FROM_START + COMMA +
                    PROGRAM_STAGE_TABLE + POINT + PROGRAM_STAGE_REPORT_DATE_TO_USE + COMMA +
                    EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.INCIDENT_DATE + COMMA +
                    EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.ENROLLMENT_DATE + COMMA +
                    PROGRAM_STAGE_TABLE + POINT + PROGRAM_STAGE_PERIOD_TYPE +
                    FROM + EnrollmentModel.TABLE +
                    JOIN + ProgramModel.TABLE + ON + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.PROGRAM +
                    EQUAL + ProgramModel.TABLE + POINT + ProgramModel.Columns.UID +
                    JOIN + PROGRAM_STAGE_TABLE + ON + ProgramModel.TABLE + POINT + ProgramModel.Columns.UID +
                    EQUAL + PROGRAM_STAGE_TABLE + POINT + PROGRAM_STAGE_PROGRAM +
                    WHERE + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.UID +
                    EQUAL + QUESTION_MARK +
                    AND + PROGRAM_STAGE_TABLE + POINT + PROGRAM_STAGE_AUTO_GENERATE_EVENT +
                    EQUAL + "1";

    private static final String SELECT_PROGRAM =
            SELECT + EnrollmentModel.Columns.PROGRAM +
                    FROM + EnrollmentModel.TABLE +
                    WHERE + EnrollmentModel.Columns.UID + EQUAL + QUESTION_MARK +
                    LIMIT_1;

    private static final String SELECT_TE_TYPE =
            SELECT + ProgramModel.TABLE + POINT + ProgramModel.Columns.UID + COMMA +
                    EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE +
                    FROM + ProgramModel.TABLE +
                    JOIN + EnrollmentModel.TABLE +
                    ON + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.PROGRAM +
                    EQUAL + ProgramModel.TABLE + POINT + ProgramModel.Columns.UID +
                    WHERE + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.UID +
                    EQUAL + QUESTION_MARK +
                    LIMIT_1;

    private static final String QUERY = "SELECT \n" +
            "  Field.id,\n" +
            "  Field.label,\n" +
            "  Field.type,\n" +
            "  Field.mandatory,\n" +
            "  Field.optionSet,\n" +
            "  Value.VALUE,\n" +
            "  Option.displayName,\n" +
            "  Field.allowFutureDate,\n" +
            "  Field.generated,\n" +
            "  Enrollment.organisationUnit,\n" +
            "  Enrollment.status,\n" +
            "  Field.displayDescription\n" +
            "FROM (Enrollment INNER JOIN Program ON Program.uid = Enrollment.program)\n" +
            "  LEFT OUTER JOIN (\n" +
            "      SELECT\n" +
            "        TrackedEntityAttribute.uid AS id,\n" +
            "        TrackedEntityAttribute.displayName AS label,\n" +
            "        TrackedEntityAttribute.valueType AS type,\n" +
            "        TrackedEntityAttribute.optionSet AS optionSet,\n" +
            "        ProgramTrackedEntityAttribute.program AS program,\n" +
            "        ProgramTrackedEntityAttribute.mandatory AS mandatory,\n" +
            "        ProgramTrackedEntityAttribute.allowFutureDate AS allowFutureDate,\n" +
            "        TrackedEntityAttribute.generated AS generated,\n" +
            "        TrackedEntityAttribute.displayDescription AS displayDescription\n" +
            "      FROM ProgramTrackedEntityAttribute INNER JOIN TrackedEntityAttribute\n" +
            "          ON TrackedEntityAttribute.uid = ProgramTrackedEntityAttribute.trackedEntityAttribute\n" +
            "    ) AS Field ON Field.program = Program.uid\n" +
            "  LEFT OUTER JOIN TrackedEntityAttributeValue AS Value ON (\n" +
            "    Value.trackedEntityAttribute = Field.id\n" +
            "        AND Value.trackedEntityInstance = Enrollment.trackedEntityInstance)\n" +
            "  LEFT OUTER JOIN Option ON (\n" +
            "    Field.optionSet = Option.optionSet AND Value.VALUE = Option.code\n" +
            "  )\n" +
            "WHERE Enrollment.uid = ?";

    private static final String CHECK_STAGE_IS_NOT_CREATED = "SELECT * FROM Event JOIN Enrollment ON Event.enrollment = Enrollment.uid WHERE Enrollment.uid = ? AND Event.programStage = ?";
    @NonNull
    private final BriteDatabase briteDatabase;

    @NonNull
    private final CodeGenerator codeGenerator;

    @NonNull
    private final Flowable<RuleEngine> cachedRuleEngineFlowable;

    @NonNull
    private final String enrollmentUid;

    private String programUid;

    EnrollmentFormRepository(@NonNull BriteDatabase briteDatabase,
                             @NonNull RuleExpressionEvaluator expressionEvaluator,
                             @NonNull RulesRepository rulesRepository,
                             @NonNull CodeGenerator codeGenerator,
                             @NonNull String enrollmentUid) {
        this.briteDatabase = briteDatabase;
        this.codeGenerator = codeGenerator;
        this.enrollmentUid = enrollmentUid;

        // We don't want to rebuild RuleEngine on each request, since metadata of
        // the event is not changing throughout lifecycle of FormComponent.
        this.cachedRuleEngineFlowable = enrollmentProgram()
                .switchMap(program -> Flowable.zip(
                        rulesRepository.rulesNew(program),
                        rulesRepository.ruleVariables(program),
                        rulesRepository.enrollmentEvents(enrollmentUid),
                        (rules, variables, events) -> {
                            RuleEngine.Builder builder = RuleEngineContext.builder(expressionEvaluator)
                                    .rules(rules)
                                    .ruleVariables(variables)
                                    .calculatedValueMap(new HashMap<>())
                                    .supplementaryData(new HashMap<>())
                                    .build().toEngineBuilder();
                            builder.triggerEnvironment(TriggerEnvironment.ANDROIDCLIENT);
                            builder.events(events);
                            return builder.build();
                        }))
                .cacheWithInitialCapacity(1);
    }

    @NonNull
    @Override
    public Flowable<RuleEngine> ruleEngine() {
        return cachedRuleEngineFlowable;
    }

    @NonNull
    @Override
    public Flowable<String> title() {
        return briteDatabase
                .createQuery(TITLE_TABLES, SELECT_TITLE, enrollmentUid)
                .mapToOne(cursor -> cursor.getString(0)).toFlowable(BackpressureStrategy.LATEST)
                .distinctUntilChanged();
    }

    @NonNull
    @Override
    public Flowable<Pair<ProgramModel, String>> reportDate() {
        return briteDatabase.createQuery(ProgramModel.TABLE, SELECT_ENROLLMENT_PROGRAM, enrollmentUid)
                .mapToOne(ProgramModel::create)
                .flatMap(programModel -> briteDatabase.createQuery(EnrollmentModel.TABLE, SELECT_ENROLLMENT_DATE, enrollmentUid)
                        .mapToOne(EnrollmentModel::create)
                        .map(enrollmentModel -> Pair.create(programModel, enrollmentModel.enrollmentDate() != null ?
                                DateUtils.uiDateFormat().format(enrollmentModel.enrollmentDate()) : "")))
                .toFlowable(BackpressureStrategy.LATEST)
                .distinctUntilChanged();
    }

    @NonNull
    @Override
    public Flowable<Pair<ProgramModel, String>> incidentDate() {
        return briteDatabase.createQuery(ProgramModel.TABLE, SELECT_ENROLLMENT_PROGRAM, enrollmentUid)
                .mapToOne(ProgramModel::create)
                .flatMap(programModel -> briteDatabase.createQuery(EnrollmentModel.TABLE, SELECT_INCIDENT_DATE, enrollmentUid)
                        .mapToOne(EnrollmentModel::create)
                        .map(enrollmentModel -> Pair.create(programModel, enrollmentModel.incidentDate() != null ?
                                DateUtils.uiDateFormat().format(enrollmentModel.incidentDate()) : "")))
                .toFlowable(BackpressureStrategy.LATEST)
                .distinctUntilChanged();
    }

    @Override
    public Flowable<ProgramModel> getAllowDatesInFuture() {
        return briteDatabase.createQuery(ProgramModel.TABLE, SELECT_ENROLLMENT_PROGRAM, enrollmentUid)
                .mapToOne(ProgramModel::create)
                .toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    @Override
    public Flowable<ReportStatus> reportStatus() {
        return briteDatabase
                .createQuery(EnrollmentModel.TABLE, SELECT_ENROLLMENT_STATUS, enrollmentUid)
                .mapToOne(cursor ->
                        ReportStatus.fromEnrollmentStatus(EnrollmentStatus.valueOf(cursor.getString(0)))).toFlowable(BackpressureStrategy.LATEST)
                .distinctUntilChanged();
    }

    @NonNull
    @Override
    public Flowable<List<FormSectionViewModel>> sections() {
        return briteDatabase
                .createQuery(EnrollmentModel.TABLE, SELECT_ENROLLMENT_UID, enrollmentUid)
                .mapToList(cursor -> FormSectionViewModel
                        .createForEnrollment(cursor.getString(0))).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    @Override
    public Consumer<String> storeReportDate() {
        return reportDate -> {
            Calendar cal = DateUtils.parseDateToCalendar(reportDate);
            ContentValues enrollment = new ContentValues();
            enrollment.put(EnrollmentModel.Columns.ENROLLMENT_DATE, DateUtils.databaseDateFormat().format(cal.getTime()));
            enrollment.put(EnrollmentModel.Columns.STATE, State.TO_UPDATE.name()); // TODO: Check if state is TO_POST
            // TODO: and if so, keep the TO_POST state

            briteDatabase.update(EnrollmentModel.TABLE, enrollment,
                    EnrollmentModel.Columns.UID + " = ?", enrollmentUid);
        };
    }

    @NonNull
    @Override
    public Consumer<LatLng> storeCoordinates() {
        return latLng -> {
            ContentValues enrollment = new ContentValues();
            enrollment.put(EnrollmentModel.Columns.LATITUDE, latLng.latitude);
            enrollment.put(EnrollmentModel.Columns.LONGITUDE, latLng.longitude); // TODO: Check if state is TO_POST
            // TODO: and if so, keep the TO_POST state

            briteDatabase.update(EnrollmentModel.TABLE, enrollment,
                    EnrollmentModel.Columns.UID + " = ?", enrollmentUid);
        };
    }

    @NonNull
    @Override
    public Consumer<String> storeIncidentDate() {
        return incidentDate -> {
            Calendar cal = Calendar.getInstance();
            Date date = DateUtils.databaseDateFormat().parse(incidentDate);
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            ContentValues enrollment = new ContentValues();
            enrollment.put(EnrollmentModel.Columns.INCIDENT_DATE, DateUtils.databaseDateFormat().format(cal.getTime()));
            enrollment.put(EnrollmentModel.Columns.STATE, State.TO_UPDATE.name()); // TODO: Check if state is TO_POST
            // TODO: and if so, keep the TO_POST state

            briteDatabase.update(EnrollmentModel.TABLE, enrollment,
                    EnrollmentModel.Columns.UID + " = ?", enrollmentUid);
        };
    }

    @NonNull
    @Override
    public Consumer<ReportStatus> storeReportStatus() {
        return reportStatus -> {
            ContentValues enrollment = new ContentValues();
            enrollment.put(EnrollmentModel.Columns.ENROLLMENT_STATUS,
                    ReportStatus.toEnrollmentStatus(reportStatus).name());
            enrollment.put(EnrollmentModel.Columns.STATE, State.TO_UPDATE.name()); // TODO: Check if state is TO_POST
            // TODO: and if so, keep the TO_POST state

            briteDatabase.update(EnrollmentModel.TABLE, enrollment,
                    EnrollmentModel.Columns.UID + " = ?", enrollmentUid);
        };
    }

    @Nullable
    @Override
    public Observable<String> autoGenerateEvents(String enrollmentUid) {
        try {
            Cursor cursor = briteDatabase.query(SELECT_AUTO_GENERATE_PROGRAM_STAGE, enrollmentUid == null ? "" : enrollmentUid);

            if (cursor != null) {

                cursor.moveToFirst();

                for (int i = 0; i < cursor.getCount(); i++) {
                    parseCursor(cursor);
                }
                cursor.close();

            }
        } catch (ParseException e) {
            Timber.e(e);
        }

        return Observable.just(enrollmentUid);
    }

    private void parseCursor(Cursor cursor) throws ParseException {
        Calendar calNow = Calendar.getInstance();
        calNow.set(Calendar.HOUR_OF_DAY, 0);
        calNow.set(Calendar.MINUTE, 0);
        calNow.set(Calendar.SECOND, 0);
        calNow.set(Calendar.MILLISECOND, 0);
        Date now = calNow.getTime();

        String programStage = cursor.getString(0);
        String program = cursor.getString(1);
        String orgUnit = cursor.getString(2);
        int minDaysFromStart = cursor.getInt(3);
        String reportDateToUse = cursor.getString(4) != null ? cursor.getString(4) : "";
        String incidentDateString = cursor.getString(5);
        String reportDateString = cursor.getString(6);
        Date incidentDate = null;
        Date enrollmentDate = null;
        PeriodType periodType = cursor.getString(7) != null ? PeriodType.valueOf(cursor.getString(7)) : null;

        if (incidentDateString != null) {
            incidentDate = DateUtils.databaseDateFormat().parse(incidentDateString);
        }

        if (reportDateString != null) {
            enrollmentDate = DateUtils.databaseDateFormat().parse(reportDateString);
        }

        Date eventDate;
        Calendar cal = DateUtils.getInstance().getCalendar();
        switch (reportDateToUse) {
            case Constants.ENROLLMENT_DATE:
                cal.setTime(enrollmentDate != null ? enrollmentDate : Calendar.getInstance().getTime());
                break;
            case Constants.INCIDENT_DATE:
                cal.setTime(incidentDate != null ? incidentDate : Calendar.getInstance().getTime());
                break;
            default:
                cal.setTime(Calendar.getInstance().getTime());
                break;
        }
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DATE, minDaysFromStart);
        eventDate = cal.getTime();

        if (periodType != null)
            eventDate = DateUtils.getInstance().getNextPeriod(periodType, eventDate, 0); //Sets eventDate to current Period date

        Cursor eventCursor = briteDatabase.query(CHECK_STAGE_IS_NOT_CREATED, enrollmentUid, programStage);

        if (!eventCursor.moveToFirst()) {
            createEvent(program, programStage, orgUnit, eventDate, now);
        } else {
            eventCursor.close();
        }
        cursor.moveToNext();
    }

    private void createEvent(String program, String programStage, String orgUnit, Date eventDate, Date now) {
        EventModel.Builder eventBuilder = EventModel.builder()
                .uid(codeGenerator.generate())
                .created(Calendar.getInstance().getTime())
                .lastUpdated(Calendar.getInstance().getTime())
//                            .eventDate(eventDate)
//                            .dueDate(eventDate)
                .enrollment(enrollmentUid)
                .program(program)
                .programStage(programStage)
                .organisationUnit(orgUnit)
                .status(eventDate.after(now) ? EventStatus.SCHEDULE : EventStatus.ACTIVE)
                .state(State.TO_POST);
        if (eventDate.after(now)) //scheduling
            eventBuilder.dueDate(eventDate);
        else
            eventBuilder.eventDate(eventDate);

        EventModel event = eventBuilder.build();


        if (briteDatabase.insert(EventModel.TABLE, event.toContentValues()) < 0) {
            throw new OnErrorNotImplementedException(new Throwable("Unable to store event:" + event));
        }
    }

    @NonNull
    @Override
    public Observable<List<FieldViewModel>> fieldValues() {
        return briteDatabase
                .createQuery(TrackedEntityAttributeValueModel.TABLE, QUERY, enrollmentUid)
                .mapToList(this::transform);
    }


    @Override
    public void deleteTrackedEntityAttributeValues(@NonNull String trackedEntityInstanceId) {
        String deleteWhereRelationship = String.format(
                TABLE_POINT_FIELD_EQUALS,
                TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE);
        briteDatabase.delete(TrackedEntityAttributeValueModel.TABLE, deleteWhereRelationship + "'" + trackedEntityInstanceId + "'");
    }

    @Override
    public void deleteEnrollment(@NonNull String trackedEntityInstanceId) {
        String deleteWhereRelationship = String.format(
                TABLE_POINT_FIELD_EQUALS,
                EnrollmentModel.TABLE, EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE);
        briteDatabase.delete(EnrollmentModel.TABLE, deleteWhereRelationship + "'" + trackedEntityInstanceId + "'");
    }

    @Override
    public void deleteEvent() {
        // not necessary
    }

    @Override
    public void deleteTrackedEntityInstance(@NonNull String trackedEntityInstanceId) {
        String deleteWhereRelationship = String.format(
                TABLE_POINT_FIELD_EQUALS,
                TrackedEntityInstanceModel.TABLE, TrackedEntityInstanceModel.Columns.UID);
        briteDatabase.delete(TrackedEntityInstanceModel.TABLE, deleteWhereRelationship + "'" + trackedEntityInstanceId + "'");
    }

    @NonNull
    @Override
    public Observable<String> getTrackedEntityInstanceUid() {
        String selectTe = SELECT + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE +
                FROM + EnrollmentModel.TABLE +
                WHERE + EnrollmentModel.Columns.UID + EQUAL + QUESTION_MARK + LIMIT_1;

        return briteDatabase.createQuery(EnrollmentModel.TABLE, selectTe, enrollmentUid).mapToOne(cursor -> cursor.getString(0));
    }

    @Override
    public Observable<Trio<Boolean, CategoryComboModel, List<CategoryOptionComboModel>>> getProgramCategoryCombo() {
        return null;
    }

    @Override
    public void saveCategoryOption(CategoryOptionComboModel selectedOption) {
        // unused
    }

    @Override
    public Observable<Boolean> captureCoodinates() {
        String captureCoordinatesQuery = SELECT + ProgramModel.TABLE + POINT + ProgramModel.Columns.CAPTURE_COORDINATES +
                FROM + ProgramModel.TABLE +
                JOIN + EnrollmentModel.TABLE +
                ON + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.PROGRAM +
                EQUAL + ProgramModel.TABLE + POINT + ProgramModel.Columns.UID +
                WHERE + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.UID +
                EQUAL + QUESTION_MARK;
        return briteDatabase.createQuery(ProgramModel.TABLE, captureCoordinatesQuery, enrollmentUid)
                .mapToOne(cursor -> cursor.getInt(0) == 1);
    }

    @NonNull
    private FieldViewModel transform(@NonNull Cursor cursor) {
        FieldViewModelUtils fieldViewModelUtils = new FieldViewModelUtils(cursor);
        EnrollmentStatus status = EnrollmentStatus.valueOf(cursor.getString(10));

        int optionCount = FieldViewModelUtils.getOptionCount(briteDatabase, fieldViewModelUtils.getOptionSetUid());

        ValueTypeDeviceRendering fieldRendering = FieldViewModelUtils.getValueTypeDeviceRendering(briteDatabase, fieldViewModelUtils.getUid());

        FieldViewModelFactoryImpl fieldFactory = new FieldViewModelFactoryImpl(
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "");

        ObjectStyleModel objectStyle = ObjectStyleModel.builder().build();
        try (Cursor objStyleCursor = briteDatabase.query(SELECT + ALL + FROM + "ObjectStyle WHERE uid = ?", fieldViewModelUtils.getUid())) {
            if (objStyleCursor.moveToFirst())
                objectStyle = ObjectStyleModel.create(objStyleCursor);
        }

        return fieldFactory.create(fieldViewModelUtils.getUid(), fieldViewModelUtils.getLabel(), fieldViewModelUtils.getValueType(),
                fieldViewModelUtils.isMandatory(), fieldViewModelUtils.getOptionSetUid(), fieldViewModelUtils.getDataValue(), fieldViewModelUtils.getSection(),
                fieldViewModelUtils.getAllowFutureDates(), status == EnrollmentStatus.ACTIVE, null,
                fieldViewModelUtils.getDescription(), fieldRendering, optionCount, objectStyle);
    }

    @Nullable
    @Override
    public Observable<Trio<String, String, String>> useFirstStageDuringRegistration() { //enrollment uid, trackedEntityType, event uid

        String selectProgramQuery = SELECT + ALL + FROM + ProgramModel.TABLE + WHERE + ProgramModel.Columns.UID + EQUAL + QUESTION_MARK;
        String selectProgramStageQuery = SELECT + ALL + FROM + PROGRAM_STAGE_TABLE +
                WHERE + PROGRAM_STAGE_PROGRAM + EQUAL + QUESTION_MARK +
                ORDER_BY + PROGRAM_STAGE_TABLE + POINT + PROGRAM_STAGE_SORT_ORDER;

        return briteDatabase.createQuery(ProgramModel.TABLE, selectProgramQuery, programUid)
                .mapToOne(ProgramModel::create)
                .flatMap(programModel ->
                        briteDatabase.createQuery(PROGRAM_STAGE_TABLE, selectProgramStageQuery, programModel.uid())
                                .mapToList(ProgramStage::create).map(programstages -> Trio.create(programModel.useFirstStageDuringRegistration(), programstages, programModel.trackedEntityType())))
                .map(data -> {
                    ProgramStage stageToOpen = null;
                    if (data.val0()) {
                        stageToOpen = data.val1().get(0);
                    } else {
                        for (ProgramStage programStage : data.val1()) {
                            if (programStage.openAfterEnrollment() && stageToOpen == null)
                                stageToOpen = programStage;
                        }
                    }

                    if (stageToOpen != null) { //we should check if event exist (if not create) and open
                        return searchEventAndOpenDashboard(stageToOpen);
                    } else { //open Dashboard
                        return openDashboard();
                    }
                });
    }

    private Trio<String, String, String> openDashboard() {
        Cursor tetCursor = briteDatabase.query(SELECT_TE_TYPE, enrollmentUid);
        String mProgramUid = "";
        String teiUid = "";
        if (tetCursor != null && tetCursor.moveToFirst()) {
            mProgramUid = tetCursor.getString(0);
            teiUid = tetCursor.getString(1);
            tetCursor.close();
        }
        return Trio.create(teiUid, mProgramUid, "");
    }


    private Trio<String, String, String> searchEventAndOpenDashboard(ProgramStage stageToOpen) {
        String eventUidQuery = SELECT + EventModel.TABLE + POINT + EventModel.Columns.UID +
                FROM + EventModel.TABLE +
                WHERE + EventModel.TABLE + POINT + EventModel.Columns.PROGRAM_STAGE +
                EQUAL + QUESTION_MARK +
                AND + EventModel.TABLE + POINT + EventModel.Columns.ENROLLMENT +
                EQUAL + QUESTION_MARK;

        Cursor eventCursor = briteDatabase.query(eventUidQuery, stageToOpen.uid(), enrollmentUid);

        if (eventCursor != null && eventCursor.moveToFirst()) {
            String eventUid = eventCursor.getString(0);
            eventCursor.close();
            return Trio.create(getTeiUid(), programUid, eventUid);
        } else {
            String orgUnitQuery = SELECT + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.ORGANISATION_UNIT +
                    FROM + EnrollmentModel.TABLE +
                    WHERE + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.UID +
                    EQUAL + QUESTION_MARK;
            Cursor enrollmentOrgUnitCursor = briteDatabase.query(orgUnitQuery, enrollmentUid);
            if (enrollmentOrgUnitCursor != null && enrollmentOrgUnitCursor.moveToFirst()) {
                Date createdDate = DateUtils.getInstance().getCalendar().getTime();
                EventModel eventToCreate = EventModel.builder()
                        .uid(codeGenerator.generate())
                        .created(createdDate)
                        .lastUpdated(createdDate)
                        .eventDate(createdDate)
                        .enrollment(enrollmentUid)
                        .program(stageToOpen.program().uid())
                        .programStage(stageToOpen.uid())
                        .organisationUnit(enrollmentOrgUnitCursor.getString(0))
                        .status(EventStatus.ACTIVE)
                        .state(State.TO_POST)
                        .build();

                enrollmentOrgUnitCursor.close();
                if (briteDatabase.insert(EventModel.TABLE, eventToCreate.toContentValues()) < 0) {
                    throw new OnErrorNotImplementedException(new Throwable("Unable to store event:" + eventToCreate));
                }

                return Trio.create(getTeiUid(), programUid, eventToCreate.uid());//teiUid, programUio, eventUid
            } else {
                throw new IllegalArgumentException("Can't create event in enrollment with null organisation unit");
            }
        }
    }

    private String getTeiUid() {
        String teiQuery = SELECT_DISTINCT + TrackedEntityInstanceModel.TABLE + POINT + TrackedEntityInstanceModel.Columns.UID +
                FROM + TrackedEntityInstanceModel.TABLE +
                JOIN + EnrollmentModel.TABLE +
                ON + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE +
                EQUAL + TrackedEntityInstanceModel.TABLE + POINT + TrackedEntityInstanceModel.Columns.UID +
                WHERE + EnrollmentModel.TABLE + POINT + EnrollmentModel.Columns.UID +
                EQUAL + QUESTION_MARK + LIMIT_1;
        Cursor teiUidCursor = briteDatabase.query(teiQuery, enrollmentUid);
        String teiUid = "";
        if (teiUidCursor != null && teiUidCursor.moveToFirst()) {
            teiUid = teiUidCursor.getString(0);
            teiUidCursor.close();
        }
        return teiUid;
    }

    @NonNull
    private Flowable<String> enrollmentProgram() {
        return briteDatabase
                .createQuery(EnrollmentModel.TABLE, SELECT_PROGRAM, enrollmentUid)
                .mapToOne(cursor -> {
                    programUid = cursor.getString(0);
                    return programUid;
                })
                .toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    public String getOrgUnitCode(String orgUnitUid) {
        String ouCode = "";
        Cursor cursor = briteDatabase.query(SELECT + OrganisationUnitModel.Columns.CODE +
                FROM + OrganisationUnitModel.TABLE + WHERE + OrganisationUnitModel.Columns.UID +
                EQUAL + QUESTION_MARK + LIMIT_1, orgUnitUid);
        if (cursor != null && cursor.moveToFirst()) {
            ouCode = cursor.getString(0);
            cursor.close();
        }
        return ouCode;
    }
}