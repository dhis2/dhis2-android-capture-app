package org.dhis2.data.forms;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.event.EventTableInfo;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;
import org.hisp.dhis.rules.RuleEngine;
import org.hisp.dhis.rules.RuleEngineContext;
import org.hisp.dhis.rules.RuleExpressionEvaluator;
import org.hisp.dhis.rules.models.TriggerEnvironment;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

@SuppressWarnings({
        "PMD.AvoidDuplicateLiterals"
})
public class EventRepository implements FormRepository {
    private static final List<String> TITLE_TABLES = Arrays.asList(
            "Program", "ProgramStage");

    private static final List<String> SECTION_TABLES = Arrays.asList(
            "Event", "Program", "ProgramStage", "ProgramStageSelection");

    private static final String SELECT_PROGRAM = "SELECT Program.*\n" +
            "FROM Program JOIN Event ON Event.program = Program.uid \n" +
            "WHERE Event.uid =?\n" +
            "LIMIT 1;";

    private static final String SELECT_PROGRAM_FROM_EVENT = String.format(
            "SELECT %s.* from %s JOIN %s " +
                    "ON %s.%s = %s.%s " +
                    "WHERE %s.%s = ? LIMIT 1",
            "Program", "Program", "Event",
            "Event", "program", "Program", "uid",
            "Event", "uid");

    private static final String SELECT_TITLE = "SELECT\n" +
            "  Program.displayName,\n" +
            "  ProgramStage.displayName\n" +
            "FROM Event\n" +
            "  JOIN Program ON Event.program = Program.uid\n" +
            "  JOIN ProgramStage ON Event.programStage = ProgramStage.uid\n" +
            "WHERE Event.uid = ? " +
            "LIMIT 1";

    private static final String SELECT_SECTIONS = "SELECT\n" +
            "  Program.uid AS programUid,\n" +
            "  ProgramStage.uid AS programStageUid,\n" +
            "  ProgramStageSection.uid AS programStageSectionUid,\n" +
            "  ProgramStageSection.displayName AS programStageDisplayName,\n" +
            "  ProgramStageSection.mobileRenderType AS renderType,\n" +
            "  ProgramStageSection.sortOrder AS sectionOrder\n" +
            "FROM Event\n" +
            "  JOIN Program ON Event.program = Program.uid\n" +
            "  JOIN ProgramStage ON Event.programStage = ProgramStage.uid\n" +
            "  LEFT OUTER JOIN ProgramStageSection ON ProgramStageSection.programStage = Event.programStage\n" +
            "WHERE Event.uid = ? ORDER BY ProgramStageSection.sortOrder";


    private static final String SELECT_EVENT_STATUS = "SELECT\n" +
            "  Event.status\n" +
            "FROM Event\n" +
            "WHERE Event.uid = ? " +
            "LIMIT 1";

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
            "  Field.sectionOrder,\n" +
            "  Field.fieldMask\n" +
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
            "        ProgramStageSectionDataElementLink.sortOrder AS sectionOrder,\n" +
            "        DataElement.fieldMask AS fieldMask\n" +
            "      FROM ProgramStageDataElement\n" +
            "        INNER JOIN DataElement ON DataElement.uid = ProgramStageDataElement.dataElement\n" +
            "        LEFT JOIN ProgramStageSection ON ProgramStageSection.programStage = ProgramStageDataElement.programStage\n" +
            "        LEFT JOIN ProgramStageSectionDataElementLink ON ProgramStageSectionDataElementLink.programStageSection = ProgramStageSection.uid AND ProgramStageSectionDataElementLink.dataElement = DataElement.uid\n" +
            "    ) AS Field ON (Field.stage = Event.programStage)\n" +
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

    @NonNull
    private final BriteDatabase briteDatabase;

    @NonNull
    private Flowable<RuleEngine> cachedRuleEngineFlowable;

    private RuleEngine ruleEngine = null;

    @Nullable
    private final String eventUid;
    private final D2 d2;
    private final RulesRepository rulesRepository;
    private final RuleExpressionEvaluator evaluator;
    private String programUid;

    public EventRepository(@NonNull BriteDatabase briteDatabase,
                           @NonNull RuleExpressionEvaluator evaluator,
                           @NonNull RulesRepository rulesRepository,
                           @Nullable String eventUid,
                           @NonNull D2 d2) {
        this.d2 = d2;
        this.briteDatabase = briteDatabase;
        this.eventUid = eventUid;
        this.rulesRepository = rulesRepository;
        this.evaluator = evaluator;
        String program = eventUid != null ? d2.eventModule().events.uid(eventUid).blockingGet().program() : "";

        // We don't want to rebuild RuleEngine on each request, since metadata of
        // the event is not changing throughout lifecycle of FormComponent.
        this.cachedRuleEngineFlowable = Single.zip(
                rulesRepository.rulesNew(program).subscribeOn(Schedulers.io()),
                rulesRepository.ruleVariables(program).subscribeOn(Schedulers.io()),
                rulesRepository.otherEvents(eventUid).subscribeOn(Schedulers.io()),
                rulesRepository.enrollment(eventUid).subscribeOn(Schedulers.io()),
                rulesRepository.queryConstants().subscribeOn(Schedulers.io()),
                rulesRepository.getSuplementaryData().subscribeOn(Schedulers.io()),
                (rules, variables, events, enrollment, constants, supplementaryData) -> {

                    RuleEngine.Builder builder = RuleEngineContext.builder(evaluator)
                            .rules(rules)
                            .ruleVariables(variables)
                            .constantsValue(constants)
                            .calculatedValueMap(new HashMap<>())
                            .supplementaryData(supplementaryData)
                            .build().toEngineBuilder();
                    builder.triggerEnvironment(TriggerEnvironment.ANDROIDCLIENT);
                    builder.events(events);
                    if (!isEmpty(enrollment.enrollment()))
                        builder.enrollment(enrollment);
                    return builder.build();
                })
                .doOnSuccess(ruleEngine -> {
                    this.ruleEngine = ruleEngine;
                    Timber.tag("ROGRAMRULEREPOSITORY").d("RULE ENGINE READY AT %s", Thread.currentThread().getName());
                }).toFlowable()
                .cacheWithInitialCapacity(1);
    }


    @Override
    public Flowable<RuleEngine> restartRuleEngine() {
        return this.cachedRuleEngineFlowable = eventProgram()
                .switchMap(program -> Single.zip(
                        rulesRepository.rulesNew(program).subscribeOn(Schedulers.io()),
                        rulesRepository.ruleVariables(program).subscribeOn(Schedulers.io()),
                        rulesRepository.otherEvents(eventUid).subscribeOn(Schedulers.io()),
                        rulesRepository.enrollment(eventUid).subscribeOn(Schedulers.io()),
                        rulesRepository.queryConstants().subscribeOn(Schedulers.io()),
                        rulesRepository.getSuplementaryData().subscribeOn(Schedulers.io()),
                        (rules, variables, events, enrollment, constants, supplementaryData) -> {

                            RuleEngine.Builder builder = RuleEngineContext.builder(evaluator)
                                    .rules(rules)
                                    .ruleVariables(variables)
                                    .constantsValue(constants)
                                    .calculatedValueMap(new HashMap<>())
                                    .supplementaryData(supplementaryData)
                                    .build().toEngineBuilder();
                            builder.triggerEnvironment(TriggerEnvironment.ANDROIDCLIENT);
                            builder.events(events);
                            if (!isEmpty(enrollment.enrollment()))
                                builder.enrollment(enrollment);
                            return builder.build();
                        }).toFlowable())
                .cacheWithInitialCapacity(1);
    }

    @NonNull
    @Override
    public Flowable<RuleEngine> ruleEngine() {
        return ruleEngine != null ? Flowable.just(ruleEngine) : cachedRuleEngineFlowable;
    }

    @NonNull
    @Override
    public Flowable<String> title() {
        return briteDatabase
                .createQuery(TITLE_TABLES, SELECT_TITLE, eventUid == null ? "" : eventUid)
                .mapToOne(cursor -> cursor.getString(0) + " - " + cursor.getString(1)).toFlowable(BackpressureStrategy.LATEST)
                .distinctUntilChanged();
    }

    @NonNull
    @Override
    public Flowable<Pair<Program, String>> reportDate() {
        return briteDatabase.createQuery("Program", SELECT_PROGRAM, eventUid == null ? "" : eventUid)
                .mapToOne(Program::create)
                .map(program -> Pair.create(program, ""))
                .toFlowable(BackpressureStrategy.LATEST)
                .distinctUntilChanged();
    }

    @NonNull
    @Override
    public Flowable<Pair<Program, String>> incidentDate() {
        return briteDatabase.createQuery("Program", SELECT_PROGRAM, eventUid == null ? "" : eventUid)
                .mapToOne(Program::create)
                .map(program -> Pair.create(program, ""))
                .toFlowable(BackpressureStrategy.LATEST)
                .distinctUntilChanged();
    }

    @Override
    public Flowable<Program> getAllowDatesInFuture() {
        return briteDatabase.createQuery("Program", SELECT_PROGRAM_FROM_EVENT, eventUid == null ? "" : eventUid)
                .mapToOne(Program::create)
                .toFlowable(BackpressureStrategy.LATEST);
    }


    @NonNull
    @Override
    public Flowable<ReportStatus> reportStatus() {
        return briteDatabase
                .createQuery("Event", SELECT_EVENT_STATUS, eventUid == null ? "" : eventUid)
                .mapToOne(cursor -> ReportStatus.fromEventStatus(EventStatus.valueOf(cursor.getString(0)))).toFlowable(BackpressureStrategy.LATEST)
                .distinctUntilChanged();
    }

    @NonNull
    @Override
    public Flowable<List<FormSectionViewModel>> sections() {
        return briteDatabase
                .createQuery(SECTION_TABLES, SELECT_SECTIONS, eventUid == null ? "" : eventUid)
                .mapToList(cursor -> mapToFormSectionViewModels(eventUid == null ? "" : eventUid, cursor))
                .distinctUntilChanged().toFlowable(BackpressureStrategy.LATEST);
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

            ContentValues event = new ContentValues();
            event.put(EventTableInfo.Columns.EVENT_DATE, DateUtils.databaseDateFormat().format(cal.getTime()));
            event.put(EventTableInfo.Columns.STATE, State.TO_UPDATE.name()); // TODO: Check if state is TO_POST
            // TODO: and if so, keep the TO_POST state

            briteDatabase.update("Event", event, EventTableInfo.Columns.UID + " = ?", eventUid == null ? "" : eventUid);
        };
    }

    @NonNull
    @Override
    public Observable<Long> saveReportDate(String date) {
        return Observable.empty();
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
    public Observable<Long> saveIncidentDate(String date) {
        return Observable.empty();
    }

    @NonNull
    @Override
    public Consumer<Geometry> storeCoordinates() {
        return data -> {
            //coordinates are only for tracker events
        };
    }

    @Override
    public Consumer<Unit> clearCoordinates() {
        return unit -> {
            //coordinates are only for tracker events
        };
    }


    @NonNull
    @Override
    public Consumer<ReportStatus> storeReportStatus() {
        return reportStatus -> {
            ContentValues event = new ContentValues();
            event.put(EventTableInfo.Columns.STATUS, ReportStatus.toEventStatus(reportStatus).name());
            event.put(EventTableInfo.Columns.STATE, State.TO_UPDATE.name()); // TODO: Check if state is TO_POST
            // TODO: and if so, keep the TO_POST state

            briteDatabase.update("Event", event, EventTableInfo.Columns.UID + " = ?", eventUid == null ? "" : eventUid);
        };
    }

    @Nullable
    @Override
    public Observable<Trio<String, String, String>> useFirstStageDuringRegistration() {
        return Observable.just(null);
    }

    @Nullable
    @Override
    public Observable<String> autoGenerateEvents(String enrollmentUid) {
        return null;
    }

    @NonNull
    @Override
    public Observable<List<FieldViewModel>> fieldValues() {
        String where = String.format(Locale.US, "WHERE Event.uid = '%s'", eventUid == null ? "" : eventUid);
        return briteDatabase.createQuery("TrackedEntityDataValue", String.format(Locale.US, QUERY, where))
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
                "Event", EventTableInfo.Columns.UID);
        String id = eventUid == null ? "" : eventUid;
        briteDatabase.delete("Event", DELETE_WHERE_RELATIONSHIP + "'" + id + "'");
    }

    @Override
    public void deleteTrackedEntityInstance(@NonNull String trackedEntityInstanceId) {
        // not necessary
    }

    @NonNull
    @Override
    public Observable<String> getTrackedEntityInstanceUid() {
        return Observable.defer(() -> d2.enrollmentModule().enrollments().uid(
                d2.eventModule().events.uid(eventUid).blockingGet().enrollment()
        ).get().toObservable())
                .map(Enrollment::trackedEntityInstance);
    }

    @Override
    public Observable<Trio<Boolean, CategoryCombo, List<CategoryOptionCombo>>> getProgramCategoryCombo(String eventId) {
        return briteDatabase.createQuery("Event", "SELECT * FROM Event WHERE Event.uid = ?", eventUid)
                .mapToOne(Event::create)
                .flatMap(event -> briteDatabase.createQuery("CategoryCombo", "SELECT CategoryCombo.* FROM CategoryCombo " +
                        "JOIN Program ON Program.categoryCombo = CategoryCombo.uid WHERE Program.uid = ?", event.program())
                        .mapToOne(CategoryCombo::create)
                        .flatMap(categoryCombo ->
                                briteDatabase.createQuery("CategoryOptionCombo", "SELECT * FROM CategoryOptionCombo " +
                                        "WHERE categoryCombo = ?", categoryCombo.uid())
                                        .mapToList(CategoryOptionCombo::create)
                                        .map(categoryOptionCombos -> {
                                            boolean eventHastOptionSelected = false;
                                            for (CategoryOptionCombo options : categoryOptionCombos) {
                                                if (event.attributeOptionCombo() != null && event.attributeOptionCombo().equals(options.uid()))
                                                    eventHastOptionSelected = true;
                                            }
                                            return Trio.create(eventHastOptionSelected, categoryCombo, categoryOptionCombos);
                                        })
                        )
                );

    }

    @Override
    public void saveCategoryOption(CategoryOptionCombo selectedOption) {
        ContentValues event = new ContentValues();
        event.put(EventTableInfo.Columns.ATTRIBUTE_OPTION_COMBO, selectedOption.uid());
        event.put(EventTableInfo.Columns.STATE, State.TO_UPDATE.name()); // TODO: Check if state is TO_POST
        // TODO: and if so, keep the TO_POST state

        briteDatabase.update("Event", event, EventTableInfo.Columns.UID + " = ?", eventUid == null ? "" : eventUid);
    }

    @Override
    public Observable<FeatureType> captureCoodinates() {
        return d2.eventModule().events.byUid().eq(eventUid).one().get().toObservable()
                .map(event -> d2.programModule().programStages.byUid().eq(event.programStage()).one().blockingGet())
                .map(programStage -> {
                    if (programStage.featureType() == null)
                        return FeatureType.NONE;
                    else
                        return programStage.featureType();
                });
    }

    @Override
    public Observable<OrganisationUnit> getOrgUnitDates() {
        return Observable.defer(() -> Observable.just(d2.eventModule().events.uid(eventUid).blockingGet()))
                .switchMap(event -> Observable.just(d2.organisationUnitModule().organisationUnits.uid(event.organisationUnit()).blockingGet()));
    }

    @Override
    public Flowable<ProgramStage> getProgramStage(String eventUid) {
        return null;
    }

    @Override
    public Single<TrackedEntityType> captureTeiCoordinates() {
        return Single.just(TrackedEntityType.builder().build());
    }

    @Override
    public Consumer<Geometry> storeTeiCoordinates() {
        return geometry -> {

        };
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
        String formLabel = cursor.getString(10);
        String description = cursor.getString(11);
        String fieldMask = cursor.getString(14);
        if (!isEmpty(optionCodeName)) {
            dataValue = optionCodeName;
        }

        int optionCount = 0;
        try (Cursor countCursor = briteDatabase.query("SELECT COUNT (uid) FROM Option WHERE optionSet = ?", optionSetUid)) {
            if (countCursor != null) {
                if (countCursor.moveToFirst())
                    optionCount = countCursor.getInt(0);
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        ValueTypeDeviceRendering fieldRendering = null;
        try (Cursor rendering = briteDatabase.query("SELECT * FROM ValueTypeDeviceRendering WHERE uid = ?", uid)) {
            if (rendering != null && rendering.moveToFirst()) {
                fieldRendering = ValueTypeDeviceRendering.create(rendering);
            }
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
        ObjectStyle objectStyle = ObjectStyle.builder().build();
        try (Cursor objStyleCursor = briteDatabase.query("SELECT * FROM ObjectStyle WHERE uid = ?", uid)) {
            if (objStyleCursor.moveToFirst())
                objectStyle = ObjectStyle.create(objStyleCursor);
        }
        if (valueType == ValueType.ORGANISATION_UNIT && !isEmpty(dataValue)) {
            dataValue = dataValue + "_ou_" + d2.organisationUnitModule().organisationUnits.uid(dataValue).blockingGet().displayName();
        }

        return fieldFactory.create(uid, isEmpty(formLabel) ? label : formLabel, valueType,
                mandatory, optionSetUid, dataValue, section, allowFutureDates,
                status == EventStatus.ACTIVE, null, description, fieldRendering, optionCount, objectStyle, fieldMask);
    }

    @NonNull
    private Flowable<String> eventProgram() {
        return briteDatabase.createQuery("Event", SELECT_PROGRAM, eventUid == null ? "" : eventUid)
                .mapToOne(Program::create)
                .map(program -> {
                    programUid = program.uid();
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
}