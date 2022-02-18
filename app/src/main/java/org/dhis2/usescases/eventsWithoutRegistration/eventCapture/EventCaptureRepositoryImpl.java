package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import static android.text.TextUtils.isEmpty;

import androidx.annotation.NonNull;

import org.dhis2.Bindings.ValueExtensionsKt;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.data.dhislogic.AuthoritiesKt;
import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.RuleEngineRepository;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory;
import org.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitViewModel;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.form.model.LegendValue;
import org.dhis2.form.model.RowAction;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ObjectWithUid;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventEditableStatus;
import org.hisp.dhis.android.core.event.EventNonEditableReason;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.imports.TrackerImportConflict;
import org.hisp.dhis.android.core.legendset.Legend;
import org.hisp.dhis.android.core.legendset.LegendSet;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.option.OptionGroup;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.ProgramRule;
import org.hisp.dhis.android.core.program.ProgramRuleAction;
import org.hisp.dhis.android.core.program.ProgramRuleActionType;
import org.hisp.dhis.android.core.program.ProgramStageDataElement;
import org.hisp.dhis.android.core.program.ProgramStageSection;
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueObjectRepository;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.processors.FlowableProcessor;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

import static org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureRepositoryFunctionsKt.getProgramStageName;

public class EventCaptureRepositoryImpl implements EventCaptureContract.EventCaptureRepository {

    private final FieldViewModelFactory fieldFactory;

    private final String eventUid;
    private final Event currentEvent;

    private final RuleEngineRepository ruleEngineRepository;
    private final D2 d2;
    private final ResourceManager resourceManager;
    private boolean isEventEditable;
    private final LinkedHashMap<String, ProgramStageSection> sectionMap;
    private List<FieldUiModel> sectionFields;

    public EventCaptureRepositoryImpl(FieldViewModelFactory fieldFactory,
                                      RuleEngineRepository ruleEngineRepository,
                                      String eventUid,
                                      D2 d2,
                                      ResourceManager resourceManager) {
        this.eventUid = eventUid;
        this.ruleEngineRepository = ruleEngineRepository;
        this.d2 = d2;
        this.resourceManager = resourceManager;

        currentEvent = d2.eventModule().events().uid(eventUid).blockingGet();
        this.fieldFactory = fieldFactory;

        List<ProgramStageSection> sections = d2.programModule().programStageSections()
                .byProgramStageUid().eq(currentEvent.programStage())
                .withDataElements()
                .blockingGet();
        sectionMap = new LinkedHashMap<>();
        if (sections != null && !sections.isEmpty()) {
            for (ProgramStageSection section : sections) {
                sectionMap.put(section.uid(), section);
            }
        }

        sectionFields = new ArrayList<>();
    }

    @Override
    public boolean isEnrollmentOpen() {
        return currentEvent.enrollment() == null || d2.enrollmentModule().enrollmentService().blockingIsOpen(currentEvent.enrollment());
    }

    @Override
    public boolean isEnrollmentCancelled() {
        Enrollment enrollment = d2.enrollmentModule().enrollments().uid(currentEvent.enrollment()).blockingGet();
        if (enrollment == null)
            return false;
        else
            return enrollment.status() == EnrollmentStatus.CANCELLED;
    }

    @Override
    public boolean isEventEditable(String eventUid) {
        return d2.eventModule().eventService().blockingIsEditable(eventUid);
    }

    @Override
    public Flowable<String> programStageName() {
        return Flowable.just(getProgramStageName(d2, eventUid));
    }

    @Override
    public Flowable<String> eventDate() {
        return Flowable.just(
                currentEvent.eventDate() != null ? DateUtils.uiDateFormat().format(currentEvent.eventDate()) : ""
        );
    }

    @Override
    public Flowable<OrganisationUnit> orgUnit() {
        return Flowable.just(d2.organisationUnitModule().organisationUnits().uid(currentEvent.organisationUnit()).blockingGet());
    }


    @Override
    public Flowable<String> catOption() {
        return Flowable.just(d2.categoryModule().categoryOptionCombos().uid(currentEvent.attributeOptionCombo()))
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
        return Flowable.just(currentEvent)
                .map(eventSingle -> {
                    List<FormSectionViewModel> formSection = new ArrayList<>();
                    if (eventSingle.deleted() == null || !eventSingle.deleted()) {
                        List<ProgramStageSection> stageSections = new ArrayList<>(sectionMap.values());
                        if (!stageSections.isEmpty()) {
                            Collections.sort(stageSections, (one, two) ->
                                    one.sortOrder().compareTo(two.sortOrder()));

                            for (ProgramStageSection section : stageSections)
                                formSection.add(FormSectionViewModel.createForSection(
                                        eventUid,
                                        section.uid(),
                                        section.displayName(),
                                        section.renderType() != null && section.renderType().mobile() != null ?
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
                });
    }

    @NonNull
    @Override
    public Flowable<List<FieldUiModel>> list(FlowableProcessor<RowAction> processor) {
        isEventEditable = isEventEditable(eventUid);
        if (!sectionFields.isEmpty()) {
            return Flowable.just(sectionFields);
        } else {
            return Flowable.fromCallable(() -> {
                List<ProgramStageDataElement> stageDataElements = d2.programModule().programStageDataElements()
                        .byProgramStage().eq(currentEvent.programStage())
                        .withRenderType().blockingGet();
                List<ProgramStageSection> stageSections = d2.programModule().programStageSections()
                        .byProgramStageUid().eq(currentEvent.programStage())
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
                        String url = de.url();

                        int optionCount = 0;
                        List<Option> options;
                        if (!isEmpty(optionSet)) {
                            if (!isEmpty(dataValue)) {
                                if (d2.optionModule().options().byOptionSetUid().eq(optionSet).byCode().eq(dataValue).one().blockingExists()) {
                                    dataValue = d2.optionModule().options().byOptionSetUid().eq(optionSet).byCode().eq(dataValue).one().blockingGet().displayName();
                                }
                            }
                            optionCount = d2.optionModule().options().byOptionSetUid().eq(optionSet).blockingCount();
                            options = d2.optionModule().options().byOptionSetUid().eq(optionSet).orderBySortOrder(RepositoryScope.OrderByDirection.ASC).blockingGet();
                        } else {
                            options = new ArrayList<>();
                        }

                        ValueTypeDeviceRendering fieldRendering = programStageDataElement.renderType() != null ?
                                programStageDataElement.renderType().mobile() : null;

                        ObjectStyle objectStyle = de.style() != null ? de.style() : ObjectStyle.builder().build();

                        String error = checkConflicts(de.uid(), dataValue);

                        boolean isOrgUnit = valueType == ValueType.ORGANISATION_UNIT;
                        boolean isDate = valueType != null && valueType.isDate();
                        if (!isOrgUnit && !isDate) {
                            dataValue = friendlyValue;
                        }

                        LegendValue legendValue = getColorByLegend(rawValue, uid);

                        ProgramStageSectionRenderingType renderingType = programStageSection != null && programStageSection.renderType() != null &&
                                programStageSection.renderType().mobile() != null ?
                                programStageSection.renderType().mobile().type() : null;

                        FieldUiModel fieldViewModel =
                                fieldFactory.create(uid,
                                        formName == null ? displayName : formName,
                                        valueType,
                                        mandatory,
                                        optionSet,
                                        dataValue,
                                        programStageSection != null ? programStageSection.uid() : null,
                                        allowFutureDates,
                                        isEventEditable,
                                        renderingType,
                                        description,
                                        fieldRendering,
                                        optionCount,
                                        objectStyle,
                                        de.fieldMask(),
                                        legendValue,
                                        options,
                                        FeatureType.POINT,
                                        url
                                );

                        if (!error.isEmpty()) {
                            return fieldViewModel.setError(error);
                        } else {
                            return fieldViewModel;
                        }

                    })
                    .toList().toFlowable()
                    .map(data -> sectionFields = data);
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

    private LegendValue getColorByLegend(String value, String dataElementUid) {
        if (value == null) {
            return null;
        }
        try {
            final DataElement dataElement = d2.dataElementModule().dataElements()
                    .byUid().eq(dataElementUid)
                    .withLegendSets()
                    .one().blockingGet();

            if (dataElement != null && dataElement.valueType().isNumeric() &&
                    dataElement.legendSets() != null && !dataElement.legendSets().isEmpty()) {
                LegendSet legendSet = dataElement.legendSets().get(0);
                Legend legend = d2.legendSetModule().legends()
                        .byStartValue().smallerThan(Double.valueOf(value))
                        .byEndValue().biggerThan(Double.valueOf(value))
                        .byLegendSet().eq(legendSet.uid())
                        .one()
                        .blockingGet();
                if (legend == null) {
                    legend = d2.legendSetModule().legends()
                            .byEndValue().eq(Double.valueOf(value))
                            .byLegendSet().eq(legendSet.uid())
                            .one()
                            .blockingGet();
                }

                if (legend != null) {
                    return new LegendValue(
                            resourceManager.getColorFrom(legend.color()),
                            legend.displayName());
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @NonNull
    @Override
    public Flowable<Result<RuleEffect>> calculate() {
        return ruleEngineRepository.calculate();
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
        return d2.eventModule().events().uid(eventUid).delete()
                .andThen(Observable.just(true));
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
        return Observable.just(currentEvent.programStage());
    }

    @Override
    public boolean getAccessDataWrite() {
        return d2.eventModule().eventService().blockingHasDataWriteAccess(eventUid);
    }

    @Override
    public Flowable<EventStatus> eventStatus() {
        return Flowable.just(currentEvent.status());
    }

    @Override
    public String getSectionFor(String field) {
        String sectionToReturn = "NO_SECTION";
        for (ProgramStageSection section : sectionMap.values()) {
            if (UidsHelper.getUidsList(section.dataElements()).contains(field)) {
                sectionToReturn = section.uid();
                break;
            }
        }
        return sectionToReturn;
    }

    @Override
    public Single<Boolean> canReOpenEvent() {
        return Single.fromCallable(() -> d2.userModule().authorities()
                .byName().in(AuthoritiesKt.AUTH_UNCOMPLETE_EVENT, AuthoritiesKt.AUTH_ALL).one().blockingExists()
        );
    }

    @Override
    public Observable<Boolean> isCompletedEventExpired(String eventUid) {
        return d2.eventModule().eventService().getEditableStatus(eventUid).map(editionStatus -> {
            if (editionStatus instanceof EventEditableStatus.NonEditable) {
                return ((EventEditableStatus.NonEditable) editionStatus).getReason() == EventNonEditableReason.EXPIRED;
            } else {
                return false;
            }
        }).toObservable();
    }

    @Override
    public Flowable<Boolean> eventIntegrityCheck() {
        return Flowable.just(currentEvent).map(event ->
                (event.status() == EventStatus.COMPLETED ||
                        event.status() == EventStatus.ACTIVE) &&
                        event.eventDate() != null && !event.eventDate().after(new Date())
        );
    }

    @Override
    public Single<Integer> getNoteCount() {
        return d2.noteModule().notes().byEventUid().eq(eventUid).count();
    }

    @Override
    public boolean showCompletionPercentage() {
        if (d2.settingModule().appearanceSettings().blockingExists()) {
            return d2.settingModule().appearanceSettings().getCompletionSpinnerByUid(
                    currentEvent.program()
            ).visible();
        }
        return true;
    }

    @Override
    public void updateFieldValue(String uid) {
        Timber.d("UPDATING VALUE FOR FIELD %s", uid);
        ListIterator<FieldUiModel> iterator = sectionFields.listIterator();
        boolean updated = false;
        while (iterator.hasNext() && !updated) {
            FieldUiModel fieldViewModel = iterator.next();
            if (fieldViewModel.getUid().equals(uid)) {
                TrackedEntityDataValueObjectRepository valueRepository = d2.trackedEntityModule().trackedEntityDataValues().value(eventUid, uid);

                String value = null;
                String rawValue = null;
                String friendlyValue = null;
                if (valueRepository.blockingExists()) {
                    value = valueRepository.blockingGet().value();
                    rawValue = value;
                    friendlyValue = ValueExtensionsKt.userFriendlyValue(ValueExtensionsKt.blockingGetValueCheck(valueRepository, d2, uid), d2);

                    boolean isOrgUnit = fieldViewModel instanceof OrgUnitViewModel;
                    boolean isDate = fieldViewModel.getValueType() != null && fieldViewModel.getValueType().isDate();
                    if (!isOrgUnit && !isDate) {
                        value = friendlyValue;
                    }
                }

                String error = checkConflicts(uid, valueRepository.blockingExists() ? valueRepository.blockingGet().value() : null);

                fieldViewModel = fieldViewModel
                        .setValue(value)
                        .setDisplayName(friendlyValue)
                        .setEditable(fieldViewModel.getEditable() || isEventEditable);
                if (!error.isEmpty()) {
                    fieldViewModel = fieldViewModel.setError(error);
                }

                LegendValue legend = getColorByLegend(rawValue, fieldViewModel.getUid());
                fieldViewModel = fieldViewModel.setLegend(legend);

                iterator.set(fieldViewModel);
                updated = true;
                Timber.d("DONE FOR FIELD %s", uid);
            }
        }
    }

    @Override
    public boolean hasAnalytics() {
        boolean hasProgramIndicators = !d2.programModule().programIndicators().byProgramUid().eq(currentEvent.program()).blockingIsEmpty();
        List<ProgramRule> programRules = d2.programModule().programRules().withProgramRuleActions()
                .byProgramUid().eq(currentEvent.program()).blockingGet();
        boolean hasProgramRules = false;
        for (ProgramRule rule : programRules) {
            for (ProgramRuleAction action : rule.programRuleActions()) {
                if (action.programRuleActionType() == ProgramRuleActionType.DISPLAYKEYVALUEPAIR ||
                        action.programRuleActionType() == ProgramRuleActionType.DISPLAYTEXT) {
                    hasProgramRules = true;
                }
            }
        }
        return hasProgramIndicators || hasProgramRules;
    }

    @Override
    public boolean hasRelationships() {
        return !d2.relationshipModule().relationshipTypes()
                .byAvailableForEvent(eventUid)
                .blockingIsEmpty();
    }
}

