package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.database.Cursor;

import androidx.annotation.NonNull;

import org.dhis2.Bindings.RuleExtensionsKt;
import org.dhis2.Bindings.ValueExtensionsKt;
import org.dhis2.data.dhislogic.DhisEventUtils;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory;
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel;
import org.dhis2.data.forms.dataentry.fields.image.ImageViewModel;
import org.dhis2.data.forms.dataentry.fields.optionset.OptionSetViewModel;
import org.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitViewModel;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;
import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ObjectWithUid;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.imports.TrackerImportConflict;
import org.hisp.dhis.android.core.legendset.Legend;
import org.hisp.dhis.android.core.legendset.LegendSet;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.option.OptionGroup;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramIndicator;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageDataElement;
import org.hisp.dhis.android.core.program.ProgramStageSection;
import org.hisp.dhis.android.core.program.ProgramStageSectionDeviceRendering;
import org.hisp.dhis.android.core.program.ProgramStageSectionRendering;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueObjectRepository;
import org.hisp.dhis.rules.models.RuleDataValue;
import org.hisp.dhis.rules.models.RuleEffect;
import org.hisp.dhis.rules.models.RuleEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;

import static android.text.TextUtils.isEmpty;

import static org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureRepositoryFunctionsKt.getProgramStageName;

public class EventCaptureRepositoryImpl implements EventCaptureContract.EventCaptureRepository {

    private final FieldViewModelFactory fieldFactory;

    private final String eventUid;
    private final Event currentEvent;
    private final ProgramStage currentStage;

    private final FormRepository formRepository;
    private final D2 d2;
    private boolean isEventEditable;
    private final HashMap<String, ProgramStageSection> sectionMap;
    private final HashMap<String, ProgramStageDataElement> stageDataElementsMap;
    private RuleEvent.Builder eventBuilder;
    private List<FieldViewModel> sectionFields;
    private final DhisEventUtils dhisEventUtils;

    private static final String SELECT_LEGEND =
            "SELECT Legend.color FROM Legend \n"
            + "JOIN ProgramIndicatorLegendSetLink ON ProgramIndicatorLegendSetLink.legendSet = Legend.LegendSet \n"
            + "JOIN ProgramIndicator ON ProgramIndicator.uid = ProgramIndicatorLegendSetLink.programIndicator \n"
            + "WHERE ProgramIndicator.uid = ? AND Legend.startValue <= ? AND Legend.endValue > ?";

    public EventCaptureRepositoryImpl(FieldViewModelFactory fieldFactory, FormRepository formRepository, String eventUid, D2 d2, DhisEventUtils dhisEventUtils) {
        this.eventUid = eventUid;
        this.formRepository = formRepository;
        this.d2 = d2;
        this.dhisEventUtils = dhisEventUtils;

        currentEvent = d2.eventModule().events().withTrackedEntityDataValues().uid(eventUid).blockingGet();
        currentStage = d2.programModule().programStages().uid(currentEvent.programStage()).blockingGet();
        OrganisationUnit ou = d2.organisationUnitModule().organisationUnits().uid(currentEvent.organisationUnit()).blockingGet();

        eventBuilder = RuleEvent.builder()
                .event(currentEvent.uid())
                .programStage(currentEvent.programStage())
                .programStageName(currentStage.displayName())
                .status(RuleEvent.Status.valueOf(currentEvent.status().name()))
                .eventDate(currentEvent.eventDate())
                .dueDate(currentEvent.dueDate() != null ? currentEvent.dueDate() : currentEvent.eventDate())
                .organisationUnit(currentEvent.organisationUnit())
                .organisationUnitCode(ou.code());

        this.fieldFactory = fieldFactory;

        List<ProgramStageSection> sections = d2.programModule().programStageSections().byProgramStageUid().eq(currentStage.uid())
                .withDataElements().withProgramIndicators().blockingGet();
        sectionMap = new HashMap<>();
        if (sections != null && !sections.isEmpty()) {
            for (ProgramStageSection section : sections) {
                sectionMap.put(section.uid(), section);
            }
        }

        stageDataElementsMap = new HashMap<>();
        List<ProgramStageDataElement> programStageDataElements = d2.programModule().programStageDataElements()
                .byProgramStage().eq(currentStage.uid())
                .blockingGet();
        for (ProgramStageDataElement psDe : programStageDataElements) {
            stageDataElementsMap.put(psDe.dataElement().uid(), psDe);
        }

        sectionFields = new ArrayList<>();
    }

    @Override
    public boolean isEnrollmentOpen() {
        Enrollment enrollment = d2.enrollmentModule().enrollments().uid(d2.eventModule().events().uid(eventUid).blockingGet().enrollment()).blockingGet();
        return enrollment == null || enrollment.status() == EnrollmentStatus.ACTIVE;
    }

    @Override
    public boolean isEnrollmentCancelled() {
        Enrollment enrollment = d2.enrollmentModule().enrollments().uid(d2.eventModule().events().uid(eventUid).blockingGet().enrollment()).blockingGet();
        if (enrollment == null)
            return false;
        else
            return d2.enrollmentModule().enrollments().uid(d2.eventModule().events().uid(eventUid).blockingGet().enrollment()).blockingGet().status() == EnrollmentStatus.CANCELLED;
    }

    @Override
    public boolean isEventEditable(String eventUid) {
        return dhisEventUtils.isEventEditable(eventUid);
    }

    @Override
    public Flowable<String> programStageName() {
        return Flowable.just(getProgramStageName(d2, eventUid));
    }

    @Override
    public Flowable<String> eventDate() {
        return Flowable.just(d2.eventModule().events().uid(eventUid).blockingGet())
                .map(event -> DateUtils.uiDateFormat().format(event.eventDate()));
    }

    @Override
    public Flowable<OrganisationUnit> orgUnit() {
        return Flowable.just(d2.eventModule().events().uid(eventUid).blockingGet())
                .map(event -> d2.organisationUnitModule().organisationUnits().uid(event.organisationUnit()).blockingGet());
    }


    @Override
    public Flowable<String> catOption() {
        return Flowable.just(d2.eventModule().events().uid(eventUid).blockingGet())
                .map(event -> d2.categoryModule().categoryOptionCombos().uid(event.attributeOptionCombo()))
                .map(categoryOptionComboRepo -> {
                    if (categoryOptionComboRepo.blockingGet() == null)
                        return "";
                    else
                        return categoryOptionComboRepo.blockingGet().displayName();
                })
                .map(displayName -> displayName.equals("default") ? "" : displayName);
    }

    @Override
    public Flowable<List<FormSectionViewModel>> eventSections() {
        return d2.eventModule().events().uid(eventUid).get()
                .map(eventSingle -> {
                    List<FormSectionViewModel> formSection = new ArrayList<>();
                    if (eventSingle.deleted() == null || !eventSingle.deleted()) {
                        ProgramStage stage = d2.programModule().programStages().uid(eventSingle.programStage()).blockingGet();
                        List<ProgramStageSection> stageSections = d2.programModule().programStageSections().byProgramStageUid().eq(stage.uid()).blockingGet();
                        if (stageSections.size() > 0) {
                            Collections.sort(stageSections, (one, two) ->
                                    one.sortOrder().compareTo(two.sortOrder()));

                            for (ProgramStageSection section : stageSections)
                                formSection.add(FormSectionViewModel.createForSection(
                                        eventUid,
                                        section.uid(),
                                        section.displayName(),
                                        section.renderType().mobile() != null ?
                                                section.renderType().mobile().type().name() :
                                                null)
                                );
                        } else {
                            formSection.add(FormSectionViewModel.createForSection(
                                    eventUid,
                                    "",
                                    "",
                                    ProgramStageSectionRenderingType.LISTING.name()));
                        }
                    }
                    return formSection;
                }).toFlowable();
    }


    private ProgramStageSectionRenderingType renderingType(String sectionUid) {
        ProgramStageSectionRenderingType renderingType = ProgramStageSectionRenderingType.LISTING;
        if (sectionUid != null) {
            ProgramStageSectionRendering sectionRendering = d2.programModule().programStageSections().uid(sectionUid).blockingGet().renderType();
            ProgramStageSectionDeviceRendering stageSectionRendering = sectionRendering != null ? sectionRendering.mobile() : null;
            if (stageSectionRendering != null)
                renderingType = stageSectionRendering.type();
        }

        return renderingType;
    }

    private List<FieldViewModel> checkRenderType(List<FieldViewModel> fieldViewModels) {
        ArrayList<FieldViewModel> renderList = new ArrayList<>();

        for (FieldViewModel fieldViewModel : fieldViewModels) {

            ProgramStageSectionRenderingType renderingType = renderingType(fieldViewModel.programStageSection());
            if (fieldViewModel instanceof ImageViewModel && !isEmpty(fieldViewModel.optionSet()) && renderingType != ProgramStageSectionRenderingType.LISTING) {
                List<Option> options = d2.optionModule().options().byOptionSetUid().eq(fieldViewModel.optionSet() == null ? "" : fieldViewModel.optionSet())
                        .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                        .blockingGet();
                for (Option option : options) {
                    ValueTypeDeviceRendering fieldRendering = null;

                    if (stageDataElementsMap.containsKey(fieldViewModel.uid())) {
                        ProgramStageDataElement psDE = stageDataElementsMap.get(fieldViewModel.uid());
                        fieldRendering = psDE.renderType() != null && psDE.renderType().mobile() != null ? psDE.renderType().mobile() : null;
                    }

                    ObjectStyle objectStyle = option.style();

                    renderList.add(fieldFactory.create(
                            fieldViewModel.uid() + "." + option.uid(),
                            fieldViewModel.label() + ImageViewModel.NAME_CODE_DELIMITATOR + option.displayName() + ImageViewModel.NAME_CODE_DELIMITATOR + option.code(),
                            ValueType.TEXT,
                            fieldViewModel.mandatory(),
                            fieldViewModel.optionSet(),
                            fieldViewModel.value(),
                            fieldViewModel.programStageSection(),
                            fieldViewModel.allowFutureDate(),
                            fieldViewModel.editable() == null ? false : fieldViewModel.editable(),
                            renderingType, fieldViewModel.description(),
                            fieldRendering,
                            options.size(),
                            objectStyle,
                            fieldViewModel.fieldMask(),null));

                }
            } else if (fieldViewModel instanceof OptionSetViewModel) {
                List<Option> options = d2.optionModule().options().byOptionSetUid().eq(fieldViewModel.optionSet() == null ? "" : fieldViewModel.optionSet())
                        .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                        .blockingGet();
                renderList.add(((OptionSetViewModel) fieldViewModel).withOptions(options));
            } else
                renderList.add(fieldViewModel);
        }
        return renderList;
    }

    @NonNull
    @Override
    public Flowable<List<FieldViewModel>> list() {
        isEventEditable = isEventEditable(eventUid);
        if (!sectionFields.isEmpty()) {
            return Flowable.just(sectionFields)
                    .flatMapIterable(fieldViewModels -> fieldViewModels)
                    .map(fieldViewModel -> {
                        String uid = fieldViewModel.uid();
                        TrackedEntityDataValueObjectRepository valueRepository = d2.trackedEntityModule().trackedEntityDataValues().value(eventUid, uid);

                        String value = null;
                        String rawValue = null;
                        if (valueRepository.blockingExists()) {
                            value = valueRepository.blockingGet().value();
                            rawValue = value;
                            String friendlyValue = ValueExtensionsKt.userFriendlyValue(ValueExtensionsKt.blockingGetValueCheck(valueRepository, d2, uid), d2);

                            if (fieldViewModel instanceof OrgUnitViewModel && !isEmpty(value)) {
                                value = value + "_ou_" + friendlyValue;
                            } else {
                                value = friendlyValue;
                            }
                        }

                        String error = checkConflicts(uid, valueRepository.blockingExists() ? valueRepository.blockingGet().value() : null);

                        boolean editable = fieldViewModel.editable() != null ? fieldViewModel.editable() : true;
                        fieldViewModel = fieldViewModel
                                .withValue(value)
                                .withEditMode(editable || isEventEditable);

                        if (fieldViewModel instanceof EditTextViewModel) {
                            String colorByLegend = getColorByLegend(rawValue, uid);
                            fieldViewModel = ((EditTextViewModel)fieldViewModel)
                                    .withColorByLegend(colorByLegend);
                        } else if (fieldViewModel instanceof SpinnerViewModel){
                            String colorByLegend = getColorByLegend(rawValue, uid);
                            fieldViewModel = ((SpinnerViewModel)fieldViewModel)
                                    .withColorByLegend(colorByLegend);
                        }

                        if (!error.isEmpty()) {
                            fieldViewModel.withError(error);
                        }

                        return fieldViewModel;
                    }).toList().toFlowable()
                    .map(fieldViewModels -> sectionFields = fieldViewModels)
                    .map(this::checkRenderType);
        } else {
            return Flowable.fromCallable(() -> {
                List<ProgramStageDataElement> stageDataElements = d2.programModule().programStageDataElements()
                        .byProgramStage().eq(currentStage.uid())
                        .withRenderType().blockingGet();
                List<ProgramStageSection> stageSections = d2.programModule().programStageSections()
                        .byProgramStageUid().eq(currentStage.uid())
                        .withDataElements()
                        .blockingGet();
                if (!stageSections.isEmpty()) {
                    //REORDER PROGRAM STAGE DATA ELEMENTS
                    List<String> dataElementsOrder = new ArrayList<>();
                    for (ProgramStageSection section : stageSections) {
                        dataElementsOrder.addAll(UidsHelper.getUidsList(section.dataElements()));
                    }
                    Collections.sort(stageDataElements, (de1, de2) -> {
                        Integer pos1 = dataElementsOrder.indexOf(de1.dataElement().uid());
                        Integer pos2 = dataElementsOrder.indexOf(de2.dataElement().uid());
                        return pos1.compareTo(pos2);
                    });
                }
                return stageDataElements;
            })
                    .flatMapIterable(list -> list)
                    .map(programStageDataElement -> {
                        DataElement de = d2.dataElementModule().dataElements().uid(programStageDataElement.dataElement().uid()).blockingGet();
                        TrackedEntityDataValueObjectRepository valueRepository = d2.trackedEntityModule().trackedEntityDataValues().value(eventUid, de.uid());

                        ProgramStageSection programStageSection = null;
                        for (ProgramStageSection section : sectionMap.values()) {
                            if (UidsHelper.getUidsList(section.dataElements()).contains(de.uid())) {
                                programStageSection = section;
                                break;
                            }
                        }

                        String uid = de.uid();
                        String displayName = de.displayName();
                        ValueType valueType = de.valueType();
                        boolean mandatory = programStageDataElement.compulsory() != null ? programStageDataElement.compulsory() : false;
                        String optionSet = de.optionSetUid();
                        String dataValue = valueRepository.blockingExists() ? valueRepository.blockingGet().value() : null;
                        String rawValue = dataValue;
                        String friendlyValue = dataValue != null ? ValueExtensionsKt.userFriendlyValue(ValueExtensionsKt.blockingGetValueCheck(valueRepository, d2, uid), d2) : null;

                        boolean allowFutureDates = programStageDataElement.allowFutureDate() != null ? programStageDataElement.allowFutureDate() : false;
                        String formName = de.displayFormName();
                        String description = de.displayDescription();

                        int optionCount = 0;
                        if (!isEmpty(optionSet)) {
                            if (!isEmpty(dataValue)) {
                                if (d2.optionModule().options().byOptionSetUid().eq(optionSet).byCode().eq(dataValue).one().blockingExists()) {
                                    dataValue = d2.optionModule().options().byOptionSetUid().eq(optionSet).byCode().eq(dataValue).one().blockingGet().displayName();
                                }
                            }
                            optionCount = d2.optionModule().options().byOptionSetUid().eq(optionSet).blockingCount();
                        }

                        ValueTypeDeviceRendering fieldRendering = programStageDataElement.renderType() != null ?
                                programStageDataElement.renderType().mobile() : null;

                        ObjectStyle objectStyle = de.style() != null ? de.style() : ObjectStyle.builder().build();

                        String error = checkConflicts(de.uid(), dataValue);

                        if (valueType == ValueType.ORGANISATION_UNIT && !isEmpty(dataValue)) {
                            dataValue = dataValue + "_ou_" + friendlyValue;
                        } else {
                            dataValue = friendlyValue;
                        }

                        String colorByLegend = getColorByLegend(rawValue, uid);

                        ProgramStageSectionRenderingType renderingType = programStageSection != null && programStageSection.renderType() != null &&
                                programStageSection.renderType().mobile() != null ?
                                programStageSection.renderType().mobile().type() : null;

                        FieldViewModel fieldViewModel =
                                fieldFactory.create(uid, formName == null ? displayName : formName,
                                        valueType, mandatory, optionSet, dataValue,
                                        programStageSection != null ? programStageSection.uid() : null, allowFutureDates,
                                        isEventEditable,
                                        renderingType, description, fieldRendering, optionCount, objectStyle, de.fieldMask(),colorByLegend);

                        if (!error.isEmpty()) {
                            return fieldViewModel.withError(error);
                        } else {
                            return fieldViewModel;
                        }

                    })
                    .toList().toFlowable()
                    .map(data -> sectionFields = data)
                    .map(this::checkRenderType);
        }
    }

    private String checkConflicts(String dataElementUid, String value) {
        List<TrackerImportConflict> conflicts =
                d2.importModule().trackerImportConflicts()
                        .byEventUid().eq(eventUid)
                        .blockingGet();
        String error = "";
        for (TrackerImportConflict conflict : conflicts) {
            if (conflict.event() != null && conflict.event().equals(eventUid) &&
                    conflict.dataElement() != null && conflict.dataElement().equals(dataElementUid)) {
                if (Objects.equals(conflict.value(), value)) {
                    error = conflict.displayDescription();
                }
            }
        }
        return error;
    }

    private String getColorByLegend(String value, String dataElementUid) {
        String color = "";
        try{

            final DataElement dataElement = d2.dataElementModule().dataElements()
                    .byUid().eq(dataElementUid)
                    .withLegendSets()
                    .one().blockingGet();

            if (dataElement != null && dataElement.valueType().isNumeric() &&
                    dataElement.legendSets() != null && !dataElement.legendSets().isEmpty()){
                LegendSet legendSet = dataElement.legendSets().get(0);
                List<Legend> legends = d2.legendSetModule().legends().byStartValue().smallerThan(
                        Double.valueOf(value)).byEndValue().biggerThan(Double.valueOf(value))
                        .byLegendSet().eq(legendSet.uid()).blockingGet();

                if (legends.size() > 0) {
                    color = legends.get(0).color();
                }
            }

            return color;
        } catch (Exception e){
            return color;
        }
    }

    @NonNull
    @Override
    public Flowable<Result<RuleEffect>> calculate() {
        return queryDataValues(eventUid)
                .switchMap(dataValues ->
                        formRepository.ruleEngine()
                                .flatMap(ruleEngine ->
                                        Flowable.fromCallable(
                                                ruleEngine.evaluate(
                                                        eventBuilder.dataValues(dataValues).build()
                                                )))
                                .map(Result::success)
                )
                .doOnError(error -> Result.failure(new Exception(error)));
    }

    @Override
    public Observable<Boolean> completeEvent() {
        return Observable.fromCallable(() -> {
            try {
                d2.eventModule().events().uid(currentEvent.uid()).setStatus(EventStatus.COMPLETED);
                return true;
            } catch (D2Error d2Error) {
                d2Error.printStackTrace();
                return false;
            }
        });
    }

    @Override
    public boolean reopenEvent() {
        try {
            d2.eventModule().events().uid(currentEvent.uid())
                    .setStatus(EventStatus.ACTIVE);
            return true;
        } catch (D2Error d2Error) {
            d2Error.printStackTrace();
            return false;
        }
    }

    @Override
    public Observable<Boolean> deleteEvent() {
        return d2.eventModule().events().uid(eventUid).delete().toObservable();
    }

    @Override
    public Observable<Boolean> updateEventStatus(EventStatus status) {

        return Observable.fromCallable(() -> {
            d2.eventModule().events().uid(currentEvent.uid())
                    .setStatus(status);
            return true;
        });
    }

    @Override
    public Observable<Boolean> rescheduleEvent(Date newDate) {
        return Observable.fromCallable(() -> {
            d2.eventModule().events().uid(currentEvent.uid())
                    .setDueDate(newDate);
            d2.eventModule().events().uid(currentEvent.uid())
                    .setStatus(EventStatus.SCHEDULE);
            return true;
        });
    }

    @Override
    public Observable<String> programStage() {
        return Observable.defer(() -> Observable.just(currentEvent.programStage()));
    }

    @Override
    public boolean getAccessDataWrite() {
        boolean canWrite;
        canWrite =
                d2.programModule().programs().uid(
                        d2.eventModule().events().uid(eventUid).blockingGet().program()
                ).blockingGet().access().data().write();
        if (canWrite)
            canWrite =
                    d2.programModule().programStages().uid(
                            d2.eventModule().events().uid(eventUid).blockingGet().programStage()
                    ).blockingGet().access().data().write();
        return canWrite;
    }

    @Override
    public void setLastUpdated(String lastUpdatedUid) {
    }

    @Override
    public Flowable<EventStatus> eventStatus() {
        return Flowable.just(d2.eventModule().events().uid(eventUid).blockingGet())
                .map(Event::status);
    }

    @NonNull
    private Flowable<List<RuleDataValue>> queryDataValues(String eventUid) {
        return d2.eventModule().events().uid(eventUid).get()
                .flatMap(event -> d2.trackedEntityModule().trackedEntityDataValues().byEvent().eq(eventUid).byValue().isNotNull().get()
                        .map(values -> RuleExtensionsKt.toRuleDataValue(values, event, d2.dataElementModule().dataElements(), d2.programModule().programRuleVariables(), d2.optionModule().options()))).toFlowable();
    }

    @Override
    public String getSectionFor(String field) {
        String sectionToReturn = "NO_SECTION";
        List<ProgramStageSection> programStages = d2.programModule().programStageSections().byProgramStageUid().eq(currentEvent.programStage()).withDataElements().blockingGet();
        for (ProgramStageSection section : programStages) {
            if (UidsHelper.getUidsList(section.dataElements()).contains(field)) {
                sectionToReturn = section.uid();
                break;
            }
        }
        return sectionToReturn;
    }

    @Override
    public Single<Boolean> canReOpenEvent() {
        return Single.defer(() -> Single.fromCallable(() -> d2.userModule().authorities()
                .byName().in("F_UNCOMPLETE_EVENT", "ALL").one().blockingExists()
        ));
    }

    private Observable<Program> getExpiryDateFromEvent(String eventUid) {
        return d2.eventModule().events().uid(eventUid).get().
                flatMap(event -> d2.programModule().programs().uid(event.program()).get())
                .toObservable();
    }

    @Override
    public Observable<Boolean> isCompletedEventExpired(String eventUid) {
        return Observable.zip(d2.eventModule().events().uid(eventUid).get().toObservable(),
                getExpiryDateFromEvent(eventUid),
                ((event, program) -> DateUtils.getInstance().isEventExpired(null, event.completedDate(), program.completeEventsExpiryDays())));
    }

    @Override
    public Flowable<Boolean> eventIntegrityCheck() {
        return d2.eventModule().events().uid(eventUid).get()
                .map(event ->
                        (event.status() == EventStatus.COMPLETED ||
                                event.status() == EventStatus.ACTIVE) &&
                                event.eventDate() != null && !event.eventDate().after(new Date())
                ).toFlowable();
    }

    @Override
    public Single<Integer> getNoteCount() {
        return d2.noteModule().notes().byEventUid().eq(eventUid).count();
    }

    @Override
    public List<String> getOptionsFromGroups(List<String> optionGroupUids) {
        List<String> optionsFromGroups = new ArrayList<>();
        List<OptionGroup> optionGroups = d2.optionModule().optionGroups().withOptions().byUid().in(optionGroupUids).blockingGet();
        for (OptionGroup optionGroup : optionGroups) {
            for (ObjectWithUid option : optionGroup.options()) {
                if (!optionsFromGroups.contains(option.uid())) {
                    optionsFromGroups.add(option.uid());
                }
            }
        }
        return optionsFromGroups;
    }

    @Override
    public Flowable<List<ProgramIndicator>> getIndicators( String programUid )
    {
        return d2.programModule().programIndicators().byProgramUid().eq( programUid ).withLegendSets().get()
                .toFlowable();
    }

    @Override
    public Observable<Trio<ProgramIndicator, String, String>> getLegendColorForIndicator( ProgramIndicator indicator,
            String value )
    {
        String piId = indicator != null && indicator.uid() != null ? indicator.uid() : "";
        String color = "";
        try (Cursor cursor = d2.databaseAdapter().rawQuery( SELECT_LEGEND, piId, value == null ? "" : value,
                value == null ? "" : value ))
        {
            if ( cursor != null && cursor.moveToFirst() && cursor.getCount() > 0 )
            {
                color = cursor.getString( 0 );
            }
        }
        return Observable.just( Trio.create( indicator, value, color ) );
    }
}

