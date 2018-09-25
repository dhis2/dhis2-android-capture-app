package org.dhis2.data.forms;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.CodeGenerator;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;
import org.hisp.dhis.rules.RuleEngine;
import org.hisp.dhis.rules.RuleEngineContext;
import org.hisp.dhis.rules.RuleExpressionEvaluator;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

@SuppressWarnings({
        "PMD.AvoidDuplicateLiterals"
})
class EnrollmentFormRepository implements FormRepository {
    private static final List<String> TITLE_TABLES = Arrays.asList(
            EnrollmentModel.TABLE, ProgramModel.TABLE);

    private static final String SELECT_TITLE = "SELECT Program.displayName\n" +
            "FROM Enrollment\n" +
            "  JOIN Program ON Enrollment.program = Program.uid\n" +
            "WHERE Enrollment.uid = ? " +
            "LIMIT 1";

    private static final String SELECT_ENROLLMENT_UID = "SELECT Enrollment.uid\n" +
            "FROM Enrollment\n" +
            "WHERE Enrollment.uid = ?";

    private static final String SELECT_ENROLLMENT_STATUS = "SELECT Enrollment.status\n" +
            "FROM Enrollment\n" +
            "WHERE Enrollment.uid = ? " +
            "LIMIT 1";

    private static final String SELECT_ENROLLMENT_DATE = "SELECT Enrollment.enrollmentDate\n" +
            "FROM Enrollment\n" +
            "WHERE Enrollment.uid = ? " +
            "LIMIT 1";

    private static final String SELECT_ENROLLMENT_PROGRAM = "SELECT Program.*\n" +
            "FROM Program JOIN Enrollment ON Enrollment.program = Program.uid\n" +
            "WHERE Enrollment.uid = ? " +
            "LIMIT 1";

    private static final String SELECT_INCIDENT_DATE = "SELECT Enrollment.* FROM Enrollment WHERE Enrollment.uid = ? LIMIT 1";

    private static final String SELECT_AUTO_GENERATE_PROGRAM_STAGE = "SELECT " +
            "ProgramStage.uid, " +
            "Program.uid, " +
            "Enrollment.organisationUnit, " +
            "ProgramStage.minDaysFromStart, " +
            "ProgramStage.generatedByEnrollmentDate, " +
            "Enrollment.incidentDate, " +
            "Enrollment.enrollmentDate, " +
            "ProgramStage.periodType \n" +
            "FROM Enrollment\n" +
            "  JOIN Program ON Enrollment.program = Program.uid\n" +
            "  JOIN ProgramStage ON Program.uid = ProgramStage.program \n" +
            "WHERE Enrollment.uid = ? AND ProgramStage.autoGenerateEvent = 1";

    private static final String GET_FIRST_STAGE = "SELECT " +
            "ProgramStage.* FROM ProgramStage " +
            "WHERE ProgramStage.program = ? " +
            "AND ProgramStage.sortOrder != 0 " +
            "ORDER BY ProgramStage.sortOrder ASC LIMIT 1";
    private static final String CHECK_IF_FIRST_STAGE_EVENT_EXIST_IN_ENROLLMENT = "SELECT " +
            "Event.uid, " +
            "Program.trackedEntityType "+
            "FROM Event " +
            "JOIN Enrollment ON Enrollment.uid = Event.enrollment " +
            "JOIN Program ON Program.uid = Enrollment.program " +
            "WHERE Event.programStage = ? AND Enrollment.uid = ?";

    private static final String SELECT_USE_FIRST_STAGE =
            "SELECT ProgramStage.uid, " +
                    "ProgramStage.program, " +
                    "Enrollment.organisationUnit, " +
                    "Program.trackedEntityType, " +
                    "Event.uid\n" +
                    "FROM Enrollment\n" +
                    "  JOIN Program ON Enrollment.program = Program.uid\n" +
                    "  JOIN ProgramStage ON Program.uid = ProgramStage.program\n" +
                    "  JOIN Event ON event.enrollment = Enrollment.uid\n" +
                    "WHERE Enrollment.uid = ? AND (Program.useFirstStageDuringRegistration  = 1 OR ProgramStage.openAfterEnrollment = 1) " +
                    "ORDER BY ProgramStage.sortOrder ASC LIMIT 1";

    private static final String SELECT_USE_FIRST_STAGE_WITHOUT_AUTOGENERATE_EVENT =
            "SELECT ProgramStage.uid, " +
                    "ProgramStage.program, " +
                    "Enrollment.organisationUnit, " +
                    "Program.trackedEntityType\n" +
                    "FROM Enrollment\n" +
                    "  JOIN Program ON Enrollment.program = Program.uid\n" +
                    "  JOIN ProgramStage ON Program.uid = ProgramStage.program\n" +
                    "WHERE Enrollment.uid = ? AND (Program.useFirstStageDuringRegistration  = 1 OR ProgramStage.openAfterEnrollment = 1) " +
                    "ORDER BY ProgramStage.sortOrder ASC";

    private static final String SELECT_PROGRAM = "SELECT \n" +
            "  program\n" +
            "FROM Enrollment\n" +
            "WHERE uid = ?\n" +
            "LIMIT 1;";

    private static final String SELECT_TE_TYPE = "SELECT " +
            "Program.uid, Enrollment.trackedEntityInstance\n" +
            "FROM Program\n" +
            "JOIN Enrollment ON Enrollment.program = Program.uid\n" +
            "WHERE Enrollment.uid = ? LIMIT 1";

    private static final String SELECT_VALUES = "SELECT TrackedEntityAttributeValue.value FROM TrackedEntityAttributeValue " +
            "JOIN TrackedEntityInstance ON TrackedEntityInstance.uid = TrackedEntityAttributeValue.trackedEntityInstance " +
            "JOIN Enrollment ON Enrollment.trackedEntityInstance = TrackedEntityInstance.uid WHERE Enrollment.uid = ?";
    private static final String QUERY = "SELECT \n" +
            "  Field.id,\n" +
            "  Field.label,\n" +
            "  Field.type,\n" +
            "  Field.mandatory,\n" +
            "  Field.optionSet,\n" +
            "  Value.value,\n" +
            "  Option.name,\n" +
            "  Field.allowFutureDate,\n" +
            "  Field.generated,\n" +
            "  Enrollment.organisationUnit,\n" +
            "  Enrollment.status\n" +
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
            "        TrackedEntityAttribute.generated AS generated\n" +
            "      FROM ProgramTrackedEntityAttribute INNER JOIN TrackedEntityAttribute\n" +
            "          ON TrackedEntityAttribute.uid = ProgramTrackedEntityAttribute.trackedEntityAttribute\n" +
            "    ) AS Field ON Field.program = Program.uid\n" +
            "  LEFT OUTER JOIN TrackedEntityAttributeValue AS Value ON (\n" +
            "    Value.trackedEntityAttribute = Field.id\n" +
            "        AND Value.trackedEntityInstance = Enrollment.trackedEntityInstance)\n" +
            "  LEFT OUTER JOIN Option ON (\n" +
            "    Field.optionSet = Option.optionSet AND Value.value = Option.code\n" +
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
                        (rules, variables) ->
                                RuleEngineContext.builder(expressionEvaluator)
                                        .rules(rules)
                                        .ruleVariables(variables)
                                        .build().toEngineBuilder()
                                        .build()))
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
                .createQuery(TITLE_TABLES, SELECT_TITLE, enrollmentUid == null ? "" : enrollmentUid)
                .mapToOne(cursor -> cursor.getString(0)).toFlowable(BackpressureStrategy.LATEST)
                .distinctUntilChanged();
    }

    @NonNull
    @Override
    public Flowable<String> reportDate() {
        return briteDatabase
                .createQuery(EnrollmentModel.TABLE, SELECT_ENROLLMENT_DATE, enrollmentUid == null ? "" : enrollmentUid)
                .mapToOne(cursor -> cursor.getString(0) == null ? "" : cursor.getString(0))
                .toFlowable(BackpressureStrategy.LATEST)
                .distinctUntilChanged();
    }

    @NonNull
    @Override
    public Flowable<Pair<ProgramModel, String>> incidentDate() {
        return briteDatabase.createQuery(ProgramModel.TABLE, SELECT_ENROLLMENT_PROGRAM, enrollmentUid == null ? "" : enrollmentUid)
                .mapToOne(ProgramModel::create)
                .flatMap(programModel -> briteDatabase.createQuery(EnrollmentModel.TABLE, SELECT_INCIDENT_DATE, enrollmentUid == null ? "" : enrollmentUid)
                        .mapToOne(EnrollmentModel::create)
                        .map(enrollmentModel -> Pair.create(programModel, enrollmentModel.incidentDate() != null ?
                                DateUtils.uiDateFormat().format(enrollmentModel.incidentDate()) : "")))
                .toFlowable(BackpressureStrategy.LATEST)
                .distinctUntilChanged();
    }

    @Override
    public Flowable<ProgramModel> getAllowDatesInFuture() {
        return briteDatabase.createQuery(ProgramModel.TABLE, SELECT_ENROLLMENT_PROGRAM, enrollmentUid == null ? "" : enrollmentUid)
                .mapToOne(ProgramModel::create)
                .toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    @Override
    public Flowable<ReportStatus> reportStatus() {
        return briteDatabase
                .createQuery(EnrollmentModel.TABLE, SELECT_ENROLLMENT_STATUS, enrollmentUid == null ? "" : enrollmentUid)
                .mapToOne(cursor ->
                        ReportStatus.fromEnrollmentStatus(EnrollmentStatus.valueOf(cursor.getString(0)))).toFlowable(BackpressureStrategy.LATEST)
                .distinctUntilChanged();
    }

    @NonNull
    @Override
    public Flowable<List<FormSectionViewModel>> sections() {
        return briteDatabase
                .createQuery(EnrollmentModel.TABLE, SELECT_ENROLLMENT_UID, enrollmentUid == null ? "" : enrollmentUid)
                .mapToList(cursor -> FormSectionViewModel
                        .createForEnrollment(cursor.getString(0))).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    @Override
    public Consumer<String> storeReportDate() {
        return reportDate -> {
            Calendar cal = Calendar.getInstance();
            Date date = DateUtils.databaseDateFormat().parse(reportDate);
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            ContentValues enrollment = new ContentValues();
            enrollment.put(EnrollmentModel.Columns.ENROLLMENT_DATE, DateUtils.databaseDateFormat().format(cal.getTime()));
            enrollment.put(EnrollmentModel.Columns.STATE, State.TO_UPDATE.name()); // TODO: Check if state is TO_POST
            // TODO: and if so, keep the TO_POST state

            updateProgramTable(Calendar.getInstance().getTime(), programUid);

            briteDatabase.update(EnrollmentModel.TABLE, enrollment,
                    EnrollmentModel.Columns.UID + " = ?", enrollmentUid == null ? "" : enrollmentUid);
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

            updateProgramTable(Calendar.getInstance().getTime(), programUid);

            briteDatabase.update(EnrollmentModel.TABLE, enrollment,
                    EnrollmentModel.Columns.UID + " = ?", enrollmentUid == null ? "" : enrollmentUid);
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

            updateProgramTable(Calendar.getInstance().getTime(), programUid);

            briteDatabase.update(EnrollmentModel.TABLE, enrollment,
                    EnrollmentModel.Columns.UID + " = ?", enrollmentUid == null ? "" : enrollmentUid);
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

            updateProgramTable(Calendar.getInstance().getTime(), programUid);

            briteDatabase.update(EnrollmentModel.TABLE, enrollment,
                    EnrollmentModel.Columns.UID + " = ?", enrollmentUid == null ? "" : enrollmentUid);
        };
    }

    @NonNull
    @Override
    public Observable<String> autoGenerateEvents(String enrollmentUid) {

        Calendar calNow = Calendar.getInstance();
        calNow.set(Calendar.HOUR_OF_DAY, 0);
        calNow.set(Calendar.MINUTE, 0);
        calNow.set(Calendar.SECOND, 0);
        calNow.set(Calendar.MILLISECOND, 0);
        Date now = calNow.getTime();


        Cursor cursor = briteDatabase.query(SELECT_AUTO_GENERATE_PROGRAM_STAGE, enrollmentUid == null ? "" : enrollmentUid);

        if (cursor != null) {
            cursor.moveToFirst();
            for (int i = 0; i < cursor.getCount(); i++) {


                String programStage = cursor.getString(0);
                String program = cursor.getString(1);
                String orgUnit = cursor.getString(2);
                int minDaysFromStart = cursor.getInt(3);
                Boolean generatedByEnrollmentDate = cursor.getInt(4) == 1;
                Date incidentDate = null;
                Date enrollmentDate = null;
                PeriodType periodType = cursor.getString(7) != null ? PeriodType.valueOf(cursor.getString(7)) : null;

                try {
                    incidentDate = DateUtils.databaseDateFormat().parse(cursor.getString(5));
                    enrollmentDate = DateUtils.databaseDateFormat().parse(cursor.getString(6));

                } catch (Exception e) {
                    Timber.e(e);
                }

                Date eventDate;
                Calendar cal = Calendar.getInstance();
                if (generatedByEnrollmentDate) {

                    cal.setTime(enrollmentDate != null ? enrollmentDate : Calendar.getInstance().getTime());
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    cal.add(Calendar.DATE, minDaysFromStart);
                    eventDate = cal.getTime();
                } else {
                    cal.setTime(incidentDate != null ? incidentDate : Calendar.getInstance().getTime());
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    cal.add(Calendar.DATE, minDaysFromStart);
                    eventDate = cal.getTime();
                }

                if (periodType != null)
                    eventDate = DateUtils.getInstance().getNextPeriod(periodType, eventDate, 0); //Sets eventDate to current Period date

                Cursor eventCursor = briteDatabase.query(CHECK_STAGE_IS_NOT_CREATED, enrollmentUid, programStage);

                if (!eventCursor.moveToFirst()) {

                    EventModel event = EventModel.builder()
                            .uid(codeGenerator.generate())
                            .created(Calendar.getInstance().getTime())
                            .lastUpdated(Calendar.getInstance().getTime())
                            .eventDate(eventDate)
                            .dueDate(eventDate)
                            .enrollment(enrollmentUid)
                            .program(program)
                            .programStage(programStage)
                            .organisationUnit(orgUnit)
                            .status(eventDate.after(now) ? EventStatus.SCHEDULE : EventStatus.ACTIVE)
                            .state(State.TO_POST)
                            .build();


                    if (briteDatabase.insert(EventModel.TABLE, event.toContentValues()) < 0) {
                        throw new OnErrorNotImplementedException(new Throwable("Unable to store event:" + event));
                    }

                    updateProgramTable(now, program);

                } else
                    eventCursor.close();

                cursor.moveToNext();
            }
            cursor.close();

        }

        return Observable.just(enrollmentUid);
    }

    @NonNull
    @Override
    public Observable<List<FieldViewModel>> fieldValues() {
        return briteDatabase
                .createQuery(TrackedEntityAttributeValueModel.TABLE, QUERY, enrollmentUid == null ? "" : enrollmentUid)
                .mapToList(this::transform);
    }


    @Override
    public void deleteTrackedEntityAttributeValues(@NonNull String trackedEntityInstanceId) {
        String DELETE_WHERE_RELATIONSHIP = String.format(
                "%s.%s = ",
                TrackedEntityAttributeValueModel.TABLE, TrackedEntityAttributeValueModel.Columns.TRACKED_ENTITY_INSTANCE);
        briteDatabase.delete(TrackedEntityAttributeValueModel.TABLE, DELETE_WHERE_RELATIONSHIP + "'" + trackedEntityInstanceId + "'");
    }

    @Override
    public void deleteEnrollment(@NonNull String trackedEntityInstanceId) {
        String DELETE_WHERE_RELATIONSHIP = String.format(
                "%s.%s = ",
                EnrollmentModel.TABLE, EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE);
        briteDatabase.delete(EnrollmentModel.TABLE, DELETE_WHERE_RELATIONSHIP + "'" + trackedEntityInstanceId + "'");
    }

    @Override
    public void deleteEvent() {
        // not necessary
    }

    @Override
    public void deleteTrackedEntityInstance(@NonNull String trackedEntityInstanceId) {
        String DELETE_WHERE_RELATIONSHIP = String.format(
                "%s.%s = ",
                TrackedEntityInstanceModel.TABLE, TrackedEntityInstanceModel.Columns.UID);
        briteDatabase.delete(TrackedEntityInstanceModel.TABLE, DELETE_WHERE_RELATIONSHIP + "'" + trackedEntityInstanceId + "'");
    }

    @NonNull
    @Override
    public Observable<String> getTrackedEntityInstanceUid() {
        String SELECT_TE = "SELECT " + EnrollmentModel.TABLE + "." + EnrollmentModel.Columns.TRACKED_ENTITY_INSTANCE +
                " FROM " + EnrollmentModel.TABLE +
                " WHERE " + EnrollmentModel.Columns.UID + " = ?" +
                " LIMIT 1";

        return briteDatabase.createQuery(EnrollmentModel.TABLE, SELECT_TE, enrollmentUid == null ? "" : enrollmentUid).mapToOne(cursor -> cursor.getString(0));
    }

    @NonNull
    private FieldViewModel transform(@NonNull Cursor cursor) {
        String uid = cursor.getString(0);
        String label = cursor.getString(1);
        ValueType valueType = ValueType.valueOf(cursor.getString(2));
        boolean mandatory = cursor.getInt(3) == 1;
        String optionSetUid = cursor.getString(4);
        String dataValue = cursor.getString(5);
        String optionCodeName = cursor.getString(6);
        String section = cursor.getString(7);
        Boolean allowFutureDates = cursor.getInt(8) == 1;
        EnrollmentStatus status = EnrollmentStatus.valueOf(cursor.getString(10));
        if (!isEmpty(optionCodeName)) {
            dataValue = optionCodeName;
        }

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

        return fieldFactory.create(uid, label, valueType, mandatory, optionSetUid, dataValue, section,
                allowFutureDates, status == EnrollmentStatus.ACTIVE, null);
    }

    @NonNull
    @Override
    public Observable<Trio<String, String, String>> useFirstStageDuringRegistration() { //EnrollmentUid,

        return briteDatabase.createQuery(ProgramStageModel.TABLE, GET_FIRST_STAGE, programUid)
                .mapToOne(ProgramStageModel::create)
                .map(programStage->{
                    Cursor eventCursor = briteDatabase.query(CHECK_IF_FIRST_STAGE_EVENT_EXIST_IN_ENROLLMENT,programStage.uid(),enrollmentUid);
                    if(eventCursor!=null && eventCursor.moveToFirst()){ //Event exist
                        return Trio.create(enrollmentUid,eventCursor.getString(1),eventCursor.getString(0));
                    }else{//Event does not exist
                        Cursor newCursor = briteDatabase.query(SELECT_USE_FIRST_STAGE_WITHOUT_AUTOGENERATE_EVENT, enrollmentUid);
                        if (newCursor.moveToFirst()) {
                            String programStageUid = programStage.uid();
                            String programStageProgram = programStage.program();
                            String enrollmentOrgUnit = newCursor.getString(2);
                            String trackedEntityType = newCursor.getString(3);

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(cal.getTime());
                            cal.set(Calendar.HOUR_OF_DAY, 0);
                            cal.set(Calendar.MINUTE, 0);
                            cal.set(Calendar.SECOND, 0);
                            cal.set(Calendar.MILLISECOND, 0);

                            Date createdDate = Calendar.getInstance().getTime();
                            EventModel event = EventModel.builder()
                                    .uid(codeGenerator.generate())
                                    .created(createdDate)
                                    .lastUpdated(createdDate)
                                    .eventDate(cal.getTime())
                                    .enrollment(enrollmentUid)
                                    .program(programStageProgram)
                                    .programStage(programStageUid)
                                    .organisationUnit(enrollmentOrgUnit)
                                    .status(EventStatus.ACTIVE)
                                    .state(State.TO_POST)
                                    .build();

                            if (briteDatabase.insert(EventModel.TABLE, event.toContentValues()) < 0) {
                                throw new OnErrorNotImplementedException(new Throwable("Unable to store event:" + event));
                            }
                            updateProgramTable(createdDate, programStageProgram);
                            return Trio.create(enrollmentUid, trackedEntityType, event.uid());
                    } else {
                            Cursor tetCursor = briteDatabase.query(SELECT_TE_TYPE, enrollmentUid == null ? "" : enrollmentUid);
                            tetCursor.moveToFirst();

                            return Trio.create(tetCursor.getString(0), tetCursor.getString(1), "");
                        }
                }});


        /*return briteDatabase.createQuery(ProgramStageModel.TABLE, SELECT_USE_FIRST_STAGE, enrollmentUid == null ? "" : enrollmentUid)
                .map(query -> {
                    String trackedEntityType = "";
                    String eventUid;
                    Cursor cursor = query.run();
                    if (cursor != null && cursor.moveToFirst()) {
                        trackedEntityType = cursor.getString(3);
                        eventUid = cursor.getString(4);

                        return Trio.create(enrollmentUid, trackedEntityType, eventUid);
                    } else {
                        Cursor newCursor = briteDatabase.query(SELECT_USE_FIRST_STAGE_WITHOUT_AUTOGENERATE_EVENT, enrollmentUid);
                        if (newCursor.moveToFirst()) {
                            String programStageUid = newCursor.getString(0);
                            String programStageProgram = newCursor.getString(1);
                            String enrollmentOrgUnit = newCursor.getString(2);

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(cal.getTime());
                            cal.set(Calendar.HOUR_OF_DAY, 0);
                            cal.set(Calendar.MINUTE, 0);
                            cal.set(Calendar.SECOND, 0);
                            cal.set(Calendar.MILLISECOND, 0);

                            Date createdDate = Calendar.getInstance().getTime();
                            EventModel event = EventModel.builder()
                                    .uid(codeGenerator.generate())
                                    .created(createdDate)
                                    .lastUpdated(createdDate)
                                    .eventDate(cal.getTime())
                                    .enrollment(enrollmentUid)
                                    .program(programStageProgram)
                                    .programStage(programStageUid)
                                    .organisationUnit(enrollmentOrgUnit)
                                    .status(EventStatus.ACTIVE)
                                    .state(State.TO_POST)
                                    .build();

                            if (briteDatabase.insert(EventModel.TABLE, event.toContentValues()) < 0) {
                                throw new OnErrorNotImplementedException(new Throwable("Unable to store event:" + event));
                            }
                            updateProgramTable(createdDate, programStageProgram);
                            return Trio.create(enrollmentUid, trackedEntityType, event.uid());

                        } else {
                            Cursor tetCursor = briteDatabase.query(SELECT_TE_TYPE, enrollmentUid == null ? "" : enrollmentUid);
                            tetCursor.moveToFirst();

                            return Trio.create(tetCursor.getString(0), tetCursor.getString(1), "");
                        }
                    }
                });*/
    }

    @NonNull
    private Flowable<String> enrollmentProgram() {
        return briteDatabase
                .createQuery(EnrollmentModel.TABLE, SELECT_PROGRAM, enrollmentUid == null ? "" : enrollmentUid)
                .mapToOne(cursor -> {
                    programUid = cursor.getString(0);
                    return programUid;
                })
                .toFlowable(BackpressureStrategy.LATEST);
    }


    private void updateProgramTable(Date lastUpdated, String programUid) {
      /*  ContentValues program = new ContentValues(); TODO: This causes the app to crash
        program.put(EnrollmentModel.Columns.LAST_UPDATED, BaseIdentifiableObject.DATE_FORMAT.format(lastUpdated));
        briteDatabase.update(ProgramModel.TABLE, program, ProgramModel.Columns.UID + " = ?", programUid);*/
    }
}