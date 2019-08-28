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
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.event.EventTableInfo;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageDataElement;
import org.hisp.dhis.android.core.program.ProgramStageSection;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.rules.RuleEngine;
import org.hisp.dhis.rules.RuleEngineContext;
import org.hisp.dhis.rules.RuleExpressionEvaluator;
import org.hisp.dhis.rules.models.TriggerEnvironment;

import java.util.ArrayList;
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
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

@SuppressWarnings({
        "PMD.AvoidDuplicateLiterals"
})
public class EventRepository implements FormRepository {

    @NonNull
    private final BriteDatabase briteDatabase;

    @NonNull
    private Flowable<RuleEngine> cachedRuleEngineFlowable;

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
        // We don't want to rebuild RuleEngine on each request, since metadata of
        // the event is not changing throughout lifecycle of FormComponent.
        this.cachedRuleEngineFlowable = eventProgram()
                .switchMap(program -> Flowable.zip(
                        rulesRepository.rulesNew(program),
                        rulesRepository.ruleVariables(program),
                        rulesRepository.otherEvents(eventUid),
                        rulesRepository.enrollment(eventUid),
                        rulesRepository.queryConstants(),
                        rulesRepository.getSuplementaryData(d2),
                        (rules, variables, events, enrollment, constants,supplementaryData) -> {

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
                        }))
                .cacheWithInitialCapacity(1);
    }


    @Override
    public Flowable<RuleEngine> restartRuleEngine() {
        return this.cachedRuleEngineFlowable = eventProgram()
                .switchMap(program -> Flowable.zip(
                        rulesRepository.rulesNew(program),
                        rulesRepository.ruleVariables(program),
                        rulesRepository.otherEvents(eventUid),
                        rulesRepository.enrollment(eventUid),
                        rulesRepository.queryConstants(),
                        (rules, variables, events, enrollment, constants) -> {

                            RuleEngine.Builder builder = RuleEngineContext.builder(evaluator)
                                    .rules(rules)
                                    .ruleVariables(variables)
                                    .constantsValue(constants)
                                    .calculatedValueMap(new HashMap<>())
                                    .supplementaryData(new HashMap<>())
                                    .build().toEngineBuilder();
                            builder.triggerEnvironment(TriggerEnvironment.ANDROIDCLIENT);
                            builder.events(events);
                            if (!isEmpty(enrollment.enrollment()))
                                builder.enrollment(enrollment);
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
        return d2.eventModule().events.uid(eventUid).get()
                .flatMap(event -> d2.programModule().programStages.uid(event.programStage()).get()
                        .flatMap(programStage -> d2.programModule().programs.uid(event.program()).get()
                                .map(program -> program.displayName() + " - " + programStage.displayName())))
                .toFlowable();
    }

    @NonNull
    @Override
    public Flowable<Pair<Program, String>> reportDate() {
        return d2.eventModule().events.uid(eventUid).get()
                .flatMap(event -> d2.programModule().programs.uid(event.program()).get()
                        .map(program -> Pair.create(program, "")))
                .toFlowable();
    }

    @NonNull
    @Override
    public Flowable<Pair<Program, String>> incidentDate() {
        return d2.eventModule().events.uid(eventUid).get()
                .flatMap(event -> d2.programModule().programs.uid(event.program()).get()
                        .map(program -> Pair.create(program, "")))
                .toFlowable();
    }



    @Override
    public Flowable<Program> getAllowDatesInFuture() {
        return d2.eventModule().events.uid(eventUid).get()
                .flatMap(event -> d2.programModule().programs.uid(event.program()).get())
                .toFlowable();
    }

    @NonNull
    @Override
    public Flowable<ReportStatus> reportStatus() {
        return d2.eventModule().events.uid(eventUid).get()
                .map(event -> ReportStatus.fromEventStatus(event.status()))
                .toFlowable();
    }

    @NonNull
    @Override
    public Flowable<List<FormSectionViewModel>> sections() {
        return d2.eventModule().events.uid(eventUid).get()
                .flatMap(event -> d2.programModule().programStageSections.byProgramStageUid().eq(event.programStage()).get()
                        .map(programStageSections -> {
                            List<FormSectionViewModel> sections = new ArrayList<>();
                            if(programStageSections.isEmpty()){
                                // This programstage has no sections
                                sections.add(FormSectionViewModel.createForProgramStage(
                                        eventUid, event.programStage()));
                            } else {
                                // This programstage has sections
                                for(ProgramStageSection stageSection: programStageSections){
                                    sections.add( FormSectionViewModel.createForSection(
                                            eventUid, stageSection.uid(), stageSection.displayName(), stageSection.renderType().mobile().type().name()));
                                }
                            }

                            return sections;
                        })).toFlowable();
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

            updateProgramTable(Calendar.getInstance().getTime(), programUid);

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

    @NonNull
    @Override
    public Consumer<ReportStatus> storeReportStatus() {
        return reportStatus -> {
            ContentValues event = new ContentValues();
            event.put(EventTableInfo.Columns.STATUS, ReportStatus.toEventStatus(reportStatus).name());
            event.put(EventTableInfo.Columns.STATE, State.TO_UPDATE.name()); // TODO: Check if state is TO_POST
            // TODO: and if so, keep the TO_POST state

            updateProgramTable(Calendar.getInstance().getTime(), programUid);

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
        //TODO need testing this method
        return d2.eventModule().events.uid(eventUid).get()
                .flatMap(event -> d2.programModule().programStages.withProgramStageDataElements()
                                    .withProgramStageSections().uid(event.programStage()).get()
                        .map(programStage -> {
                            List<FieldViewModel> fields = new ArrayList<>();
                            if(programStage.programStageSections().size() > 0){
                                for(ProgramStageSection section: programStage.programStageSections()){
                                    for(DataElement dataElement: section.dataElements()){
                                        dataElement = d2.dataElementModule().dataElements.uid(dataElement.uid()).blockingGet();
                                        for(ProgramStageDataElement programStageDataElement: programStage.programStageDataElements()){
                                            if(programStageDataElement.dataElement().uid().equals(dataElement.uid())){
                                                TrackedEntityDataValue dataValue = d2.trackedEntityModule().trackedEntityDataValues.byEvent().eq(event.uid()).byDataElement().eq(dataElement.uid()).one().blockingGet();
                                                fields.add(transform(dataElement, programStageDataElement, dataValue, section.uid(), event));
                                            }
                                        }
                                    }
                                }
                            }else{
                                for(ProgramStageDataElement programStageDataElement: programStage.programStageDataElements()){
                                    DataElement dataElement = d2.dataElementModule().dataElements.uid(programStageDataElement.dataElement().uid()).blockingGet();
                                    TrackedEntityDataValue dataValue = d2.trackedEntityModule().trackedEntityDataValues.byEvent().eq(event.uid()).byDataElement().eq(dataElement.uid()).one().blockingGet();
                                    fields.add(transform(dataElement, programStageDataElement, dataValue, null, event));
                                }
                            }
                            return fields;
                        })).toObservable();
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
        return Observable.defer(() -> d2.enrollmentModule().enrollments.uid(
                d2.eventModule().events.uid(eventUid).blockingGet().enrollment()
        ).get().toObservable())
                .map(Enrollment::trackedEntityInstance);
    }

    @Override
    public Observable<Trio<Boolean, CategoryCombo, List<CategoryOptionCombo>>> getProgramCategoryCombo(String eventId) {
        return d2.eventModule().events.uid(eventId).get()
                .flatMap(event -> d2.programModule().programs.withCategoryCombo().uid(event.program()).get()
                        .flatMap(program -> d2.categoryModule().categoryOptionCombos
                                .byCategoryComboUid().eq(program.categoryCombo().uid()).get()
                                .map(categoryOptionCombos -> {
                                    boolean eventHastOptionSelected = false;
                                    for (CategoryOptionCombo options : categoryOptionCombos) {
                                        if (event.attributeOptionCombo() != null && event.attributeOptionCombo().equals(options.uid()))
                                            eventHastOptionSelected = true;
                                    }
                                    return Trio.create(eventHastOptionSelected, program.categoryCombo(), categoryOptionCombos);
                                })
                        )).toObservable();
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
    public Observable<Boolean> captureCoodinates() {
        return d2.eventModule().events.byUid().eq(eventUid).one().get().toObservable()
                .map(event -> d2.programModule().programStages.byUid().eq(event.programStage()).one().blockingGet())
                .map(programStage -> programStage.featureType() != FeatureType.NONE);
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
    public Single<FeatureType> captureTeiCoordinates() {
        return Single.just(FeatureType.NONE);
    }

    @Override
    public Consumer<Geometry> storeTeiCoordinates() {
        return geometry -> {

        };
    }

    @NonNull
    private FieldViewModel transform(DataElement dataElement, ProgramStageDataElement programStageDataElement,
                                         TrackedEntityDataValue trackedEntityDataValue, String section, Event event) {
        String uid = dataElement.uid();
        String label = dataElement.displayName();
        ValueType valueType = dataElement.valueType();
        boolean mandatory = programStageDataElement.compulsory();
        String optionSetUid = dataElement.optionSetUid();
        Boolean allowFutureDates = programStageDataElement.allowFutureDate();
        EventStatus status = event.status();
        String dataValue = trackedEntityDataValue != null ? trackedEntityDataValue.value() : "";
        String formLabel = dataElement.displayFormName();
        String description = dataElement.displayDescription();

        List<Option> options = d2.optionModule().options.byOptionSetUid().eq(optionSetUid).byCode().eq(dataValue).blockingGet();
        if (options.size()> 0) {
            dataValue = options.get(0).displayName();
        }

        int optionCount = d2.optionModule().options.byOptionSetUid().eq(optionSetUid).blockingGet().size();

        ValueTypeDeviceRendering fieldRendering = null; //TODO does not exist into modules from dataElement
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
                status == EventStatus.ACTIVE, null, description, fieldRendering, optionCount, objectStyle);
    }

    @NonNull
    private Flowable<String> eventProgram() {
        return d2.eventModule().events.uid(eventUid).get()
                .flatMap(event -> d2.programModule().programs.uid(event.program()).get()
                        .map(program -> {
                            programUid = program.uid();
                            return programUid;}))
                .toFlowable();
    }


    private void updateProgramTable(Date lastUpdated, String programUid) {
        /*ContentValues program = new ContentValues();TODO: Crash if active
        program.put(EnrollmentModel.Columns.LAST_UPDATED, BaseIdentifiableObject.DATE_FORMAT.format(lastUpdated));
        briteDatabase.update("Program", program, ProgramModel.Columns.UID + " = ?", programUid);*/
    }
}