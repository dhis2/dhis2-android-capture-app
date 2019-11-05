package org.dhis2.data.forms;

import android.content.ContentValues;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.CodeGenerator;
import org.dhis2.utils.Constants;
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
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.enrollment.EnrollmentTableInfo;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueTableInfo;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceTableInfo;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;
import org.hisp.dhis.rules.RuleEngine;
import org.hisp.dhis.rules.RuleEngineContext;
import org.hisp.dhis.rules.RuleExpressionEvaluator;
import org.hisp.dhis.rules.models.TriggerEnvironment;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.exceptions.OnErrorNotImplementedException;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static android.text.TextUtils.isEmpty;

@SuppressWarnings({
        "PMD.AvoidDuplicateLiterals"
})
public class EnrollmentFormRepository implements FormRepository {

    @NonNull
    private final BriteDatabase briteDatabase;

    @NonNull
    private final CodeGenerator codeGenerator;

    @NonNull
    private Flowable<RuleEngine> cachedRuleEngineFlowable;

    @NonNull
    private final String enrollmentUid;
    private final D2 d2;
    private final RulesRepository rulesRepository;
    private final RuleExpressionEvaluator expressionEvaluator;

    private String programUid;

    public EnrollmentFormRepository(@NonNull BriteDatabase briteDatabase,
                                    @NonNull RuleExpressionEvaluator expressionEvaluator,
                                    @NonNull RulesRepository rulesRepository,
                                    @NonNull CodeGenerator codeGenerator,
                                    @NonNull String enrollmentUid,
                                    @NonNull D2 d2) {
        this.d2 = d2;
        this.briteDatabase = briteDatabase;
        this.codeGenerator = codeGenerator;
        this.enrollmentUid = enrollmentUid;
        this.rulesRepository = rulesRepository;
        this.expressionEvaluator = expressionEvaluator;

        // We don't want to rebuild RuleEngine on each request, since metadata of
        // the event is not changing throughout lifecycle of FormComponent.
        this.cachedRuleEngineFlowable = enrollmentProgram()
                .switchMap(program -> Single.zip(
                        rulesRepository.rulesNew(program).subscribeOn(Schedulers.io()),
                        rulesRepository.ruleVariables(program).subscribeOn(Schedulers.io()),
                        rulesRepository.enrollmentEvents(enrollmentUid).subscribeOn(Schedulers.io()),
                        rulesRepository.queryConstants().subscribeOn(Schedulers.io()),
                        rulesRepository.supplementaryData().subscribeOn(Schedulers.io()),
                        (rules, variables, events, constants, supplementaryData) -> {
                            RuleEngine.Builder builder = RuleEngineContext.builder(expressionEvaluator)
                                    .rules(rules)
                                    .ruleVariables(variables)
                                    .calculatedValueMap(new HashMap<>())
                                    .supplementaryData(supplementaryData)
                                    .constantsValue(constants)
                                    .build().toEngineBuilder();
                            builder.triggerEnvironment(TriggerEnvironment.ANDROIDCLIENT);
                            builder.events(events);
                            return builder.build();
                        }).toFlowable())
                .cacheWithInitialCapacity(1);
    }

    @Override
    public Flowable<RuleEngine> restartRuleEngine() {
        return this.cachedRuleEngineFlowable = enrollmentProgram()
                .switchMap(program -> Single.zip(
                        rulesRepository.rulesNew(program),
                        rulesRepository.ruleVariables(program),
                        rulesRepository.enrollmentEvents(enrollmentUid),
                        rulesRepository.queryConstants(),
                        rulesRepository.supplementaryData(),
                        (rules, variables, events, constants, supplementaryData) -> {
                            RuleEngine.Builder builder = RuleEngineContext.builder(expressionEvaluator)
                                    .rules(rules)
                                    .ruleVariables(variables)
                                    .calculatedValueMap(new HashMap<>())
                                    .supplementaryData(supplementaryData)
                                    .constantsValue(constants)
                                    .build().toEngineBuilder();
                            builder.triggerEnvironment(TriggerEnvironment.ANDROIDCLIENT);
                            builder.events(events);
                            return builder.build();
                        }).toFlowable())
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
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
                .flatMap(enrollment -> d2.programModule().programs().uid(enrollment.program()).get())
                .map(program -> program.displayName()).toFlowable();
    }

    @NonNull
    @Override
    public Flowable<Pair<Program, String>> reportDate() {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
                .flatMap(enrollment -> d2.programModule().programs().uid(enrollment.program()).get()
                        .map(program -> Pair.create(program, enrollment.enrollmentDate() != null ?
                                DateUtils.uiDateFormat().format(enrollment.enrollmentDate()) : "")))
                .toFlowable();
    }

    @NonNull
    @Override
    public Flowable<Pair<Program, String>> incidentDate() {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
                .flatMap(enrollment -> d2.programModule().programs().uid(enrollment.program()).get().
                        map(program -> Pair.create(program, enrollment.incidentDate() != null ?
                                DateUtils.uiDateFormat().format(enrollment.incidentDate()) : "")))
                .toFlowable();
    }

    @Override
    public Flowable<Program> getAllowDatesInFuture() {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
                .flatMap(enrollment -> d2.programModule().programs().uid(enrollment.program()).get())
                .toFlowable();
    }

    @NonNull
    @Override
    public Flowable<ReportStatus> reportStatus() {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
                .map(enrollment -> ReportStatus.fromEnrollmentStatus(enrollment.status()))
                .toFlowable();
    }

    @NonNull
    @Override
    public Flowable<List<FormSectionViewModel>> sections() {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
                .map(enrollment -> Arrays.asList(FormSectionViewModel
                        .createForEnrollment(enrollment.uid()))).toFlowable();
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
            enrollment.put("enrollmentDate", DateUtils.databaseDateFormat().format(cal.getTime()));
            enrollment.put(EnrollmentTableInfo.Columns.STATE, State.TO_UPDATE.name()); // TODO: Check if state is TO_POST
            // TODO: and if so, keep the TO_POST state

            briteDatabase.update("Enrollment", enrollment,
                    " uid = ?", enrollmentUid == null ? "" : enrollmentUid);
        };
    }

    @NonNull
    @Override
    public Observable<Long> saveReportDate(String reportDate) {
        try {

            String reportDateToStore = null;
            if (!isEmpty(reportDate)) {

                Calendar cal = Calendar.getInstance();
                Date date = DateUtils.databaseDateFormat().parse(reportDate);
                cal.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                reportDateToStore = DateUtils.databaseDateFormat().format(cal.getTime());
            }

            ContentValues enrollment = new ContentValues();
            enrollment.put("enrollmentDate", reportDateToStore);
            enrollment.put(EnrollmentTableInfo.Columns.STATE, State.TO_UPDATE.name()); // TODO: Check if state is TO_POST
            // TODO: and if so, keep the TO_POST state

            long updated = briteDatabase.update("Enrollment", enrollment,
                    " uid = ?", enrollmentUid == null ? "" : enrollmentUid);

            return Observable.just(updated);
        } catch (ParseException e) {
            return Observable.error(new Exception("Error saving reportDate"));
        }

    }

    @NonNull
    @Override
    public Observable<Long> saveIncidentDate(String incidentDate) {
        try {
            String incidentDateToStore = null;
            if (!isEmpty(incidentDate)) {
                Calendar cal = Calendar.getInstance();
                Date date = DateUtils.databaseDateFormat().parse(incidentDate);
                cal.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                incidentDateToStore = DateUtils.databaseDateFormat().format(cal.getTime());
            }
            ContentValues enrollment = new ContentValues();
            enrollment.put("incidentDate", incidentDateToStore);
            enrollment.put(EnrollmentTableInfo.Columns.STATE, State.TO_UPDATE.name()); // TODO: Check if state is TO_POST
            // TODO: and if so, keep the TO_POST state

            long updated = briteDatabase.update("Enrollment", enrollment,
                    " uid = ?", enrollmentUid == null ? "" : enrollmentUid);

            return Observable.just(updated);
        } catch (ParseException e) {
            return Observable.error(new Exception("Error saving reportDate"));
        }
    }

    @NonNull
    @Override
    public Consumer<Geometry> storeCoordinates() {
        return geometry -> {
            EnrollmentObjectRepository repo = d2.enrollmentModule().enrollments().uid(enrollmentUid);
            repo.setGeometry(geometry);
        };
    }

    @NonNull
    @Override
    public Consumer<Geometry> storeTeiCoordinates() {
        return geometry -> {
            String teiUid = d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet().trackedEntityInstance();
            d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).setGeometry(geometry);
        };
    }


    @Override
    public Consumer<Unit> clearCoordinates() {
        return unit -> d2.enrollmentModule().enrollments().uid(enrollmentUid).setGeometry(null);
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
            enrollment.put("incidentDate", DateUtils.databaseDateFormat().format(cal.getTime()));
            enrollment.put(EnrollmentTableInfo.Columns.STATE, State.TO_UPDATE.name()); // TODO: Check if state is TO_POST
            // TODO: and if so, keep the TO_POST state

            briteDatabase.update("Enrollment", enrollment,
                    " uid = ?", enrollmentUid == null ? "" : enrollmentUid);
        };
    }

    @NonNull
    @Override
    public Consumer<ReportStatus> storeReportStatus() {
        return reportStatus -> {
            ContentValues enrollment = new ContentValues();
            enrollment.put("status",
                    ReportStatus.toEnrollmentStatus(reportStatus).name());
            enrollment.put(EnrollmentTableInfo.Columns.STATE, State.TO_UPDATE.name()); // TODO: Check if state is TO_POST
            // TODO: and if so, keep the TO_POST state

            briteDatabase.update("Enrollment", enrollment,
                    " uid = ?", enrollmentUid == null ? "" : enrollmentUid);
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


        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
                .flatMap(enrollment -> d2.programModule().programStages().byAutoGenerateEvent().isTrue()
                        .byProgramUid().eq(enrollment.program()).get()
                        .flatMap(programStages -> {
                            for (ProgramStage programStage : programStages) {

                                boolean hideDueDate = programStage.hideDueDate() != null ? programStage.hideDueDate() : false;

                                String program = enrollment.program();
                                String orgUnit = enrollment.organisationUnit();
                                int minDaysFromStart = programStage.minDaysFromStart();
                                String reportDateToUse = programStage.reportDateToUse() != null ? programStage.reportDateToUse() : "";
                                Date incidentDate = enrollment.incidentDate();
                                Date enrollmentDate = enrollment.enrollmentDate();
                                PeriodType periodType = programStage.periodType();
                                boolean generatedByEnrollmentDate = programStage.generatedByEnrollmentDate();

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

                                if (!generatedByEnrollmentDate && incidentDate != null)
                                    cal.setTime(incidentDate);

                                if (generatedByEnrollmentDate)
                                    cal.setTime(enrollmentDate);

                                cal.set(Calendar.HOUR_OF_DAY, 0);
                                cal.set(Calendar.MINUTE, 0);
                                cal.set(Calendar.SECOND, 0);
                                cal.set(Calendar.MILLISECOND, 0);
                                cal.add(Calendar.DATE, minDaysFromStart);
                                eventDate = cal.getTime();

                                if (periodType != null)
                                    eventDate = DateUtils.getInstance().getNextPeriod(periodType, eventDate, 0); //Sets eventDate to current Period date

                                List<Event> events = d2.eventModule().events().byEnrollmentUid().eq(enrollment.uid()).byProgramStageUid().eq(programStage.uid()).blockingGet();
                                if (events == null || events.isEmpty()) {

                                    Event.Builder eventBuilder = Event.builder()
                                            .uid(codeGenerator.generate())
                                            .created(Calendar.getInstance().getTime())
                                            .lastUpdated(Calendar.getInstance().getTime())
                                            .enrollment(enrollmentUid)
                                            .program(program)
                                            .programStage(programStage.uid())
                                            .organisationUnit(orgUnit)
                                            .status(eventDate.after(now) && !hideDueDate ? EventStatus.SCHEDULE : EventStatus.ACTIVE)
                                            .state(State.TO_POST);
                                    if (eventDate.after(now) && !hideDueDate) //scheduling
                                        eventBuilder.dueDate(eventDate);
                                    else
                                        eventBuilder.eventDate(eventDate);

                                    Event event = eventBuilder.build();


                                    if (briteDatabase.insert("Event", event.toContentValues()) < 0) {
                                        throw new OnErrorNotImplementedException(new Throwable("Unable to store event:" + event));
                                    }
                                }

                            }
                            return Single.just(enrollmentUid);

                        })).toObservable();

    }

    @NonNull
    @Override
    public Observable<List<FieldViewModel>> fieldValues() {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
                .flatMap(enrollment -> d2.programModule().programs().withProgramTrackedEntityAttributes().uid(enrollment.program()).get()
                        .map(program -> {
                            List<FieldViewModel> fieldViewModelList = new ArrayList<>();
                            for (ProgramTrackedEntityAttribute ptea : program.programTrackedEntityAttributes()) {
                                TrackedEntityAttribute tea = d2.trackedEntityModule().trackedEntityAttributes().withObjectStyle().uid(ptea.trackedEntityAttribute().uid()).blockingGet();
                                TrackedEntityAttributeValue value = d2.trackedEntityModule().trackedEntityAttributeValues()
                                        .byTrackedEntityAttribute().eq(tea.uid())
                                        .byTrackedEntityInstance().eq(enrollment.trackedEntityInstance())
                                        .one().blockingGet();
                                fieldViewModelList.add(transform(tea, ptea, value != null ? value.value() : "", enrollment));
                            }
                            return fieldViewModelList;
                        })).toObservable();
    }


    @Override
    public void deleteTrackedEntityAttributeValues(@NonNull String trackedEntityInstanceId) {
        String DELETE_WHERE_RELATIONSHIP = String.format(
                "%s.%s = ",
                TrackedEntityAttributeValueTableInfo.TABLE_INFO.name(), TrackedEntityAttributeValueTableInfo.Columns.TRACKED_ENTITY_INSTANCE);
        briteDatabase.delete(TrackedEntityAttributeValueTableInfo.TABLE_INFO.name(), DELETE_WHERE_RELATIONSHIP + "'" + trackedEntityInstanceId + "'");
    }

    @Override
    public void deleteEnrollment(@NonNull String trackedEntityInstanceId) {
        String DELETE_WHERE_RELATIONSHIP = String.format(
                "%s.%s = ",
                "Enrollment", "trackedEntityInstance");
        briteDatabase.delete("Enrollment", DELETE_WHERE_RELATIONSHIP + "'" + trackedEntityInstanceId + "'");
    }

    @Override
    public void deleteEvent() {
        // not necessary
    }

    @Override
    public void deleteTrackedEntityInstance(@NonNull String trackedEntityInstanceId) {
        String DELETE_WHERE_RELATIONSHIP = String.format(
                "%s.%s = ",
                TrackedEntityInstanceTableInfo.TABLE_INFO.name(), "uid");
        briteDatabase.delete(TrackedEntityInstanceTableInfo.TABLE_INFO.name(), DELETE_WHERE_RELATIONSHIP + "'" + trackedEntityInstanceId + "'");
    }

    @NonNull
    @Override
    public Observable<String> getTrackedEntityInstanceUid() {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
                .map(enrollment -> enrollment.trackedEntityInstance()).toObservable();
    }

    @Override
    public Observable<Trio<Boolean, CategoryCombo, List<CategoryOptionCombo>>> getProgramCategoryCombo(String eventUid) {
        return d2.eventModule().events().uid(eventUid).get()
                .flatMap(event -> d2.programModule().programs().uid(event.program()).get()
                        .flatMap(program -> d2.categoryModule().categoryOptionCombos()
                                .byCategoryComboUid().eq(program.categoryComboUid()).get()
                                .map(categoryOptionCombos -> {
                                    boolean eventHastOptionSelected = false;
                                    for (CategoryOptionCombo options : categoryOptionCombos) {
                                        if (event.attributeOptionCombo() != null && event.attributeOptionCombo().equals(options.uid()))
                                            eventHastOptionSelected = true;
                                    }
                                    CategoryCombo catCombo = d2.categoryModule().categoryCombos().uid(program.categoryComboUid()).blockingGet();
                                    return Trio.create(eventHastOptionSelected, catCombo, categoryOptionCombos);
                                })
                        )).toObservable();
    }

    @Override
    public void saveCategoryOption(CategoryOptionCombo selectedOption) {

    }

    @Override
    public Observable<FeatureType> captureCoodinates() {
        return d2.enrollmentModule().enrollments().byUid().eq(enrollmentUid).one().get().toObservable()
                .map(enrollment -> d2.programModule().programs().byUid().eq(enrollment.program()).one().blockingGet())
                .map(program -> {
                    if (program.featureType() == null)
                        return FeatureType.NONE;
                    else
                        return program.featureType();
                });
    }


    @Override
    public Single<TrackedEntityType> captureTeiCoordinates() {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
                .flatMap(enrollment -> d2.programModule().programs().withTrackedEntityType().uid(enrollment.program()).get())
                .flatMap(program -> d2.trackedEntityModule().trackedEntityTypes().withTrackedEntityTypeAttributes().withStyle()
                                        .uid(program.trackedEntityType().uid()).get());
    }

    @Override
    public Observable<OrganisationUnit> getOrgUnitDates() {
        return Observable.defer(() -> Observable.just(d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet()))
                .switchMap(enrollment -> Observable.just(d2.organisationUnitModule().organisationUnits().uid(enrollment.organisationUnit()).blockingGet()));
    }

    @NonNull
    private FieldViewModel transform(TrackedEntityAttribute tea, ProgramTrackedEntityAttribute programTrackedEntityAttribute,
                                     String dataValue, Enrollment enrollment) {
        String uid = tea.uid();
        String label = tea.displayName();
        ValueType valueType = tea.valueType();
        boolean mandatory = programTrackedEntityAttribute.mandatory();
        String optionSetUid = tea.optionSet() != null ? tea.optionSet().uid() : null;
        Boolean allowFutureDates = programTrackedEntityAttribute.allowFutureDate();
        Boolean generated = tea.generated();
        EnrollmentStatus status = enrollment.status();
        String description = tea.displayDescription();

        if (generated && isEmpty(dataValue))
            mandatory = true;

        int optionCount = 0;
        if (optionSetUid != null)
            optionCount = d2.optionModule().optionSets().withOptions().uid(optionSetUid).blockingGet().options().size();

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

        ObjectStyle objectStyle = ObjectStyle.builder().build();//TODO change to module tea.style(); that return a null even getting "withObjectStyle"
        try (Cursor objStyleCursor = briteDatabase.query("SELECT * FROM ObjectStyle WHERE uid = ?", uid)) {
            if (objStyleCursor != null && objStyleCursor.moveToFirst())
                objectStyle = ObjectStyle.create(objStyleCursor);
        }

        if (valueType == ValueType.ORGANISATION_UNIT && !isEmpty(dataValue)) {
            dataValue = dataValue + "_ou_" + d2.organisationUnitModule().organisationUnits().uid(dataValue).blockingGet().displayName();
        }

        return fieldFactory.create(uid, label, valueType, mandatory, optionSetUid, dataValue, null,
                allowFutureDates, status == EnrollmentStatus.ACTIVE, null, description, null, optionCount, objectStyle, tea.fieldMask());
    }

    @NonNull
    @Override
    public Observable<Trio<String, String, String>> useFirstStageDuringRegistration() { //enrollment uid, trackedEntityType, event uid
        return d2.programModule().programs().uid(programUid).get()
                .flatMap(program -> d2.programModule().programStages().byProgramUid().eq(programUid).get()
                        .map(programStages -> {
                            Collections.sort(programStages, (ps1, ps2) -> {
                                Integer priority1 = ps1.sortOrder();
                                Integer priority2 = ps2.sortOrder();
                                if (priority1 != null && priority2 != null)
                                    return priority1.compareTo(priority2);
                                else if (priority1 != null)
                                    return -1;
                                else if (priority2 != null)
                                    return 1;
                                else
                                    return 0;
                            });
                            return programStages;
                        })
                        .map(programStages ->
                                Trio.create(program.useFirstStageDuringRegistration(), programStages, program.trackedEntityType())))
                .map(data -> {
                    ProgramStage stageToOpen = null;
                    if (data.val0() && !data.val1().isEmpty()) {
                        stageToOpen = data.val1().get(0);
                    } else if (!data.val1().isEmpty()) {
                        for (ProgramStage programStage : data.val1()) {
                            if (programStage.openAfterEnrollment() && stageToOpen == null)
                                stageToOpen = programStage;
                        }
                    }

                    if (stageToOpen != null) { //we should check if event exist (if not create) and open
                        List<Event> event = d2.eventModule().events().byProgramStageUid().eq(stageToOpen.uid()).byEnrollmentUid().eq(enrollmentUid).blockingGet();

                        if (event != null && !event.isEmpty()) {
                            String eventUid = event.get(0).uid();
                            return Trio.create(getTeiUid(), programUid, eventUid);
                        } else {
                            Enrollment enrollment = d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet();

                            if (enrollment != null) {
                                Date createdDate = DateUtils.getInstance().getCalendar().getTime();
                                Event eventToCreate = Event.builder()
                                        .uid(codeGenerator.generate())
                                        .created(createdDate)
                                        .lastUpdated(createdDate)
                                        .eventDate(createdDate)
                                        .enrollment(enrollmentUid)
                                        .program(stageToOpen.program().uid())
                                        .programStage(stageToOpen.uid())
                                        .organisationUnit(enrollment.organisationUnit())
                                        .status(EventStatus.ACTIVE)
                                        .state(State.TO_POST)
                                        .build();

                                if (briteDatabase.insert("Event", eventToCreate.toContentValues()) < 0) {
                                    throw new OnErrorNotImplementedException(new Throwable("Unable to store event:" + eventToCreate));
                                }

                                return Trio.create(getTeiUid(), programUid, eventToCreate.uid());//teiUid, programUio, eventUid
                            } else
                                throw new IllegalArgumentException("Can't create event in enrollment with null organisation unit");

                        }
                    } else { //open Dashboard
                        Enrollment enrollment = d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet();

                        String programUid = "";
                        String teiUid = "";
                        if (enrollment != null) {
                            programUid = enrollment.program();
                            teiUid = enrollment.trackedEntityInstance();
                        }
                        return Trio.create(teiUid, programUid, "");

                    }
                }).toObservable();

    }

    private String getTeiUid() {
        String teiUid = "";
        Enrollment enrollment = d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet();
        if (enrollment != null)
            teiUid = enrollment.trackedEntityInstance();

        return teiUid;
    }

    @NonNull
    private Flowable<String> enrollmentProgram() {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get()
                .map(enrollment -> {
                    this.programUid = enrollment.program();
                    return enrollment.program();
                })
                .toFlowable();
    }

    public Flowable<ProgramStage> getProgramStage(String eventUid) {
        return Flowable.fromCallable(() -> d2.eventModule().events().byUid().eq(eventUid).one().blockingGet())
                .map(event -> d2.programModule().programStages().byUid().eq(event.programStage()).one().blockingGet());
    }
}