package com.dhis2.data.forms;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl;
import com.dhis2.data.tuples.Pair;
import com.dhis2.data.tuples.Trio;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;
import org.hisp.dhis.rules.RuleEngine;
import org.hisp.dhis.rules.RuleEngineContext;
import org.hisp.dhis.rules.RuleExpressionEvaluator;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

import static android.text.TextUtils.isEmpty;

@SuppressWarnings({
        "PMD.AvoidDuplicateLiterals"
})
public class EventRepository implements FormRepository {
    private static final List<String> TITLE_TABLES = Arrays.asList(
            ProgramModel.TABLE, ProgramStageModel.TABLE);

    private static final List<String> SECTION_TABLES = Arrays.asList(
            EventModel.TABLE, ProgramModel.TABLE, ProgramStageModel.TABLE, ProgramStageSectionModel.TABLE);

    private static final String SELECT_PROGRAM = "SELECT Program.uid\n" +
            "FROM Program JOIN Event ON Event.program = Program.uid \n" +
            "WHERE Event.uid =?\n" +
            "LIMIT 1;";

    private static final String SELECT_PROGRAM_FROM_EVENT = String.format(
            "SELECT %s.* from %s JOIN %s " +
                    "ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ?",
            ProgramModel.TABLE, ProgramModel.TABLE, EventModel.TABLE,
            EventModel.TABLE, EventModel.Columns.PROGRAM, ProgramModel.TABLE, ProgramModel.Columns.UID,
            EventModel.TABLE, EventModel.Columns.UID);

    private static final String SELECT_TITLE = "SELECT\n" +
            "  Program.displayName,\n" +
            "  ProgramStage.displayName\n" +
            "FROM Event\n" +
            "  JOIN Program ON Event.program = Program.uid\n" +
            "  JOIN ProgramStage ON Event.programStage = ProgramStage.uid\n" +
            "WHERE Event.uid = ?";

    private static final String SELECT_SECTIONS = "SELECT\n" +
            "  Program.uid AS programUid,\n" +
            "  ProgramStage.uid AS programStageUid,\n" +
            "  ProgramStageSection.uid AS programStageSectionUid,\n" +
            "  ProgramStageSection.displayName AS programStageDisplayName,\n" +
            "  ProgramStageSection.mobileRenderType AS renderType\n" +
            "FROM Event\n" +
            "  JOIN Program ON Event.program = Program.uid\n" +
            "  JOIN ProgramStage ON Event.programStage = ProgramStage.uid\n" +
            "  LEFT OUTER JOIN ProgramStageSection ON ProgramStageSection.programStage = Event.programStage\n" +
            "WHERE Event.uid = ?";

    private static final String SELECT_EVENT_DATE = "SELECT\n" +
            "  Event.eventDate, ProgramStage.periodType\n" +
            "FROM Event\n" +
            "JOIN ProgramStage ON ProgramStage.uid = Event.programStage\n" +
            "WHERE Event.uid = ?";

    private static final String SELECT_EVENT_STATUS = "SELECT\n" +
            "  Event.status\n" +
            "FROM Event\n" +
            "WHERE Event.uid = ?";

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
            "  Event.status\n" +
            "FROM Event\n" +
            "  LEFT OUTER JOIN (\n" +
            "      SELECT\n" +
            "        DataElement.displayName AS label,\n" +
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

    @NonNull
    private final BriteDatabase briteDatabase;

    @NonNull
    private final Flowable<RuleEngine> cachedRuleEngineFlowable;

    @Nullable
    private final String eventUid;
    private String programUid;

    public EventRepository(@NonNull BriteDatabase briteDatabase,
                           @NonNull RuleExpressionEvaluator evaluator,
                           @NonNull RulesRepository rulesRepository,
                           @Nullable String eventUid) {
        this.briteDatabase = briteDatabase;
        this.eventUid = eventUid;

        // We don't want to rebuild RuleEngine on each request, since metadata of
        // the event is not changing throughout lifecycle of FormComponent.
        this.cachedRuleEngineFlowable = eventProgram()
                .switchMap(program -> Flowable.zip(
                        rulesRepository.rulesNew(program),
                        rulesRepository.ruleVariables(program),
                        rulesRepository.otherEvents(eventUid),
                        (rules, variables,events) ->
                                RuleEngineContext.builder(evaluator)
                                        .rules(rules)
                                        .ruleVariables(variables)
                                        .build().toEngineBuilder()
                                        .events(events)
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
                .createQuery(TITLE_TABLES, SELECT_TITLE, eventUid)
                .mapToOne(cursor -> cursor.getString(0) + " - " + cursor.getString(1)).toFlowable(BackpressureStrategy.LATEST)
                .distinctUntilChanged();
    }

    @NonNull
    @Override
    public Flowable<String> reportDate() {
        return briteDatabase
                .createQuery(EventModel.TABLE, SELECT_EVENT_DATE, eventUid)
                .mapToOne(cursor -> {
                    PeriodType periodType = null;
                    String eventDate = cursor.getString(0) == null ? "" : cursor.getString(0);
                    if (cursor.getString(1) != null)
                        periodType = PeriodType.valueOf(PeriodType.class, cursor.getString(1));
                    return eventDate;
                }).toFlowable(BackpressureStrategy.LATEST)
                .distinctUntilChanged();
    }

    @NonNull
    @Override
    public Flowable<Pair<ProgramModel, String>> incidentDate() {
        return briteDatabase.createQuery(ProgramModel.TABLE, SELECT_PROGRAM, eventUid)
                .mapToOne(ProgramModel::create)
                .map(programModel -> Pair.create(programModel, ""))
                .toFlowable(BackpressureStrategy.LATEST)
                .distinctUntilChanged();
    }

    @Override
    public Flowable<ProgramModel> getAllowDatesInFuture() {
        return briteDatabase.createQuery(ProgramModel.TABLE, SELECT_PROGRAM_FROM_EVENT, eventUid)
                .mapToOne(ProgramModel::create)
                .toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    @Override
    public Flowable<ReportStatus> reportStatus() {
        return briteDatabase
                .createQuery(EventModel.TABLE, SELECT_EVENT_STATUS, eventUid)
                .mapToOne(cursor -> ReportStatus.fromEventStatus(EventStatus.valueOf(cursor.getString(0)))).toFlowable(BackpressureStrategy.LATEST)
                .distinctUntilChanged();
    }

    @NonNull
    @Override
    public Flowable<List<FormSectionViewModel>> sections() {
        return briteDatabase
                .createQuery(SECTION_TABLES, SELECT_SECTIONS, eventUid)
                .mapToList(cursor -> mapToFormSectionViewModels(eventUid == null ? "" : eventUid, cursor))
                .distinctUntilChanged().toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    @Override
    public Consumer<String> storeReportDate() {
        return reportDate -> {
            ContentValues event = new ContentValues();
            event.put(EventModel.Columns.EVENT_DATE, reportDate);
            event.put(EventModel.Columns.STATE, State.TO_UPDATE.name()); // TODO: Check if state is TO_POST
            // TODO: and if so, keep the TO_POST state

            updateProgramTable(Calendar.getInstance().getTime(), programUid);

            briteDatabase.update(EventModel.TABLE, event, EventModel.Columns.UID + " = ?", eventUid);
        };
    }

    @NonNull
    @Override
    public Consumer<String> storeIncidentDate() {
        return data -> {
            //incident date is only for tracker events
        };
    }

    @NonNull
    @Override
    public Consumer<LatLng> storeCoordinates() {
        return data -> {
            //coordinates are only for tracker events
        };
    }

    @NonNull
    @Override
    public Consumer<ReportStatus> storeReportStatus() {
        return reportStatus -> {
            ContentValues event = new ContentValues();
            event.put(EventModel.Columns.STATUS, ReportStatus.toEventStatus(reportStatus).name());
            event.put(EventModel.Columns.STATE, State.TO_UPDATE.name()); // TODO: Check if state is TO_POST
            // TODO: and if so, keep the TO_POST state

            updateProgramTable(Calendar.getInstance().getTime(), programUid);

            briteDatabase.update(EventModel.TABLE, event, EventModel.Columns.UID + " = ?", eventUid);
        };
    }

    @NonNull
    @Override
    public Consumer<String> autoGenerateEvent() {
        return s -> {
            // no-op. Events are only auto generated for Enrollments
        };
    }

    @NonNull
    @Override
    public Observable<Trio<String, String, String>> useFirstStageDuringRegistration() {
        return Observable.just(null);
    }

    @NonNull
    @Override
    public Observable<String> autoGenerateEvents(String enrollmentUid) {
        return null;
    }

    @NonNull
    @Override
    public Observable<List<FieldViewModel>> fieldValues() {
        String where = String.format(Locale.US, "WHERE Event.uid = '%s'", eventUid);
        return briteDatabase.createQuery(TrackedEntityDataValueModel.TABLE, String.format(Locale.US, QUERY, where))
                .mapToList(this::transform);
    }

    @Override
    public void deleteTrackedEntityAttributeValues(@NonNull String trackedEntityInstanceId) {
        // not necessary
    }

    @Override
    public void deleteEnrollment(@NonNull String trackedEntityInstanceId) {
        // not necessary
    }

    @Override
    public void deleteEvent() {
        String DELETE_WHERE_RELATIONSHIP = String.format(
                "%s.%s = ",
                EventModel.TABLE, EventModel.Columns.UID);
        briteDatabase.delete(EventModel.TABLE, DELETE_WHERE_RELATIONSHIP + "'" + eventUid + "'");
    }

    @Override
    public void deleteTrackedEntityInstance(@NonNull String trackedEntityInstanceId) {
        // not necessary
    }

    @NonNull
    @Override
    public Observable<String> getTrackedEntityInstanceUid() {
        String SELECT_TE = "SELECT " + EventModel.TABLE + "." + EventModel.Columns.TRACKED_ENTITY_INSTANCE +
                " FROM " + EventModel.TABLE +
                " WHERE " + EventModel.Columns.UID + " = ?";
        return briteDatabase.createQuery(EnrollmentModel.TABLE, SELECT_TE, eventUid).mapToOne(cursor -> cursor.getString(0));
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
        EventStatus status = EventStatus.valueOf(cursor.getString(9));
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

        return fieldFactory.create(uid, label, valueType, mandatory, optionSetUid, dataValue, section, allowFutureDates, status == EventStatus.ACTIVE, null);
    }

    @NonNull
    private Flowable<String> eventProgram() {
        return briteDatabase.createQuery(EventModel.TABLE, SELECT_PROGRAM, eventUid)
                .mapToOne(cursor -> {
                    programUid = cursor.getString(0);
                    return programUid;
                }).toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    private FormSectionViewModel mapToFormSectionViewModels(@NonNull String eventUid, @NonNull Cursor cursor) {
        if (cursor.getString(2) == null) {
            // This programstage has no sections
            return FormSectionViewModel.createForProgramStage(
                    eventUid, cursor.getString(1));
        } else {
            // This programstage has sections
            return FormSectionViewModel.createForSection(
                    eventUid, cursor.getString(2), cursor.getString(3), cursor.getString(4));
        }
    }

    private void updateProgramTable(Date lastUpdated, String programUid) {
        /*ContentValues program = new ContentValues();TODO: Crash if active
        program.put(EnrollmentModel.Columns.LAST_UPDATED, BaseIdentifiableObject.DATE_FORMAT.format(lastUpdated));
        briteDatabase.update(ProgramModel.TABLE, program, ProgramModel.Columns.UID + " = ?", programUid);*/
    }
}