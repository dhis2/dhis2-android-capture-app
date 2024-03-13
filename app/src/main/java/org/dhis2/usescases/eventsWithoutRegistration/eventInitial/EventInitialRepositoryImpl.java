package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.R;
import org.dhis2.commons.resources.MetadataIconProvider;
import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.mobileProgramRules.RuleEngineHelper;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.form.model.OptionSetConfiguration;
import org.dhis2.form.ui.FieldViewModelFactory;
import org.dhis2.ui.MetadataIconData;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventCreateProjection;
import org.hisp.dhis.android.core.event.EventEditableStatus;
import org.hisp.dhis.android.core.event.EventObjectRepository;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageDataElement;
import org.hisp.dhis.android.core.program.ProgramStageSection;
import org.hisp.dhis.android.core.program.SectionRenderingType;
import org.hisp.dhis.android.core.settings.ProgramConfigurationSetting;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import timber.log.Timber;

public class EventInitialRepositoryImpl implements EventInitialRepository {

    private final FieldViewModelFactory fieldFactory;
    @Nullable
    private final RuleEngineHelper ruleEngineHelper;

    private final String eventUid;
    private final D2 d2;
    private final String stageUid;

    private final MetadataIconProvider metadataIconProvider;

    EventInitialRepositoryImpl(String eventUid,
                               String stageUid,
                               D2 d2,
                               FieldViewModelFactory fieldFactory,
                               @Nullable RuleEngineHelper ruleEngineHelper,
                               MetadataIconProvider metadataIconProvider) {
        this.eventUid = eventUid;
        this.stageUid = stageUid;
        this.d2 = d2;
        this.fieldFactory = fieldFactory;
        this.ruleEngineHelper = ruleEngineHelper;
        this.metadataIconProvider = metadataIconProvider;
    }

    @NonNull
    @Override
    public Observable<Event> event(String eventId) {
        return d2.eventModule().events().uid(eventId).get().toObservable();
    }

    public Observable<List<OrganisationUnit>> orgUnits(String programId, String parentUid) {
        return d2.organisationUnitModule().organisationUnits()
                .byProgramUids(Collections.singletonList(programId))
                .byParentUid().eq(parentUid)
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .get()
                .toObservable();
    }

    @Override
    public Observable<String> createEvent(String enrollmentUid, @Nullable String trackedEntityInstanceUid,
                                          @NonNull String programUid,
                                          @NonNull String programStage, @NonNull Date date,
                                          @NonNull String orgUnitUid, @Nullable String categoryOptionsUid,
                                          @Nullable String categoryOptionComboUid, @NonNull Geometry geometry) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return Observable.fromCallable(() ->
                d2.eventModule().events().blockingAdd(
                        EventCreateProjection.builder()
                                .enrollment(enrollmentUid)
                                .program(programUid)
                                .programStage(programStage)
                                .organisationUnit(orgUnitUid)
                                .attributeOptionCombo(categoryOptionComboUid)
                                .build()
                )
        ).map(uid -> {
            EventObjectRepository eventRepository = d2.eventModule().events().uid(uid);
            eventRepository.setEventDate(cal.getTime());
            if (d2.programModule().programStages().uid(eventRepository.blockingGet().programStage()).blockingGet().featureType() != null)
                switch (d2.programModule().programStages().uid(eventRepository.blockingGet().programStage()).blockingGet().featureType()) {
                    case POINT:
                    case POLYGON:
                    case MULTI_POLYGON:
                        eventRepository.setGeometry(geometry);
                        break;
                    default:
                        break;
                }
            return uid;
        });
    }

    @Override
    public Observable<String> scheduleEvent(String enrollmentUid, @Nullable String trackedEntityInstanceUid,
                                            @NonNull String programUid, @NonNull String programStage,
                                            @NonNull Date dueDate, @NonNull String orgUnitUid, @Nullable String categoryOptionsUid,
                                            @Nullable String categoryOptionComboUid, @NonNull Geometry geometry) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dueDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return Observable.fromCallable(() ->
                d2.eventModule().events().blockingAdd(
                        EventCreateProjection.builder()
                                .enrollment(enrollmentUid)
                                .program(programUid)
                                .programStage(programStage)
                                .organisationUnit(orgUnitUid)
                                .attributeOptionCombo(categoryOptionComboUid)
                                .build()
                )
        ).map(uid -> {
            EventObjectRepository eventRepository = d2.eventModule().events().uid(uid);
            eventRepository.setDueDate(cal.getTime());
            eventRepository.setStatus(EventStatus.SCHEDULE);
            if (d2.programModule().programStages().uid(eventRepository.blockingGet().programStage()).blockingGet().featureType() != null)
                switch (d2.programModule().programStages().uid(eventRepository.blockingGet().programStage()).blockingGet().featureType()) {
                    case POINT:
                    case POLYGON:
                    case MULTI_POLYGON:
                        eventRepository.setGeometry(geometry);
                        break;
                    default:
                        break;
                }
            return uid;
        });
    }

    @NonNull
    @Override
    public Observable<ProgramStage> programStage(String programUid) {
        return d2.programModule().programStages().byProgramUid().eq(programUid).one().get().toObservable();
    }

    @NonNull
    @Override
    public Observable<ProgramStage> programStageWithId(String programStageUid) {
        return d2.programModule().programStages().uid(programStageUid).get().toObservable();
    }

    @Override
    public Flowable<ProgramStage> programStageForEvent(String eventId) {
        return d2.eventModule().events().byUid().eq(eventId).one().get().toFlowable()
                .map(event -> d2.programModule().programStages().byUid().eq(event.programStage()).one().blockingGet());
    }

    @Override
    public Observable<Boolean> accessDataWrite(String programUid) {
        if (eventUid != null) {
            return d2.eventModule().eventService().isEditable(eventUid).toObservable();
        } else {
            return d2.programModule().programStages().uid(stageUid).get().toObservable()
                    .map(programStage -> programStage.access().data().write());
        }
    }

    @Override
    public void deleteEvent(String eventId, String trackedEntityInstance) {
        try {
            d2.eventModule().events().uid(eventId).blockingDelete();
        } catch (D2Error d2Error) {
            Timber.e(d2Error);
        }
    }

    @Override
    public boolean isEnrollmentOpen() {
        Event event = d2.eventModule().events().uid(eventUid).blockingGet();
        return event == null || event.enrollment() == null || d2.enrollmentModule().enrollments().uid(event.enrollment()).blockingGet().status() == EnrollmentStatus.ACTIVE;
    }


    @Override
    public Observable<Program> getProgramWithId(String programUid) {
        return d2.programModule().programs()
                .withTrackedEntityType().byUid().eq(programUid).one().get().toObservable();
    }

    @Override
    public boolean showCompletionPercentage() {
        if (d2.settingModule().appearanceSettings().blockingExists()) {
            String programUid = d2.eventModule().events().uid(eventUid).blockingGet().program();
            ProgramConfigurationSetting programConfigurationSetting = d2.settingModule()
                    .appearanceSettings()
                    .getProgramConfigurationByUid(programUid);

            if (programConfigurationSetting != null &&
                    programConfigurationSetting.completionSpinner() != null) {
                return programConfigurationSetting.completionSpinner();
            }
        }
        return true;
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
                                    SectionRenderingType.LISTING.name()));
                        }
                    }
                    return formSection;
                }).toFlowable();
    }

    @Override
    public Flowable<List<FieldUiModel>> list() {
        return d2.eventModule().events().withTrackedEntityDataValues().uid(eventUid).get()
                .map(event -> {
                    List<FieldUiModel> fields = new ArrayList<>();
                    ProgramStage stage = d2.programModule().programStages().uid(event.programStage()).blockingGet();
                    List<ProgramStageSection> sections = d2.programModule().programStageSections().withDataElements().byProgramStageUid().eq(stage.uid()).blockingGet();
                    List<ProgramStageDataElement> stageDataElements = d2.programModule().programStageDataElements().byProgramStage().eq(stage.uid()).blockingGet();

                    if (!sections.isEmpty()) {
                        for (ProgramStageSection stageSection : sections) {
                            for (ProgramStageDataElement programStageDataElement : stageDataElements) {
                                if (UidsHelper.getUidsList(stageSection.dataElements()).contains(programStageDataElement.dataElement().uid())) {
                                    DataElement dataelement = d2.dataElementModule().dataElements().uid(programStageDataElement.dataElement().uid()).blockingGet();
                                    fields.add(transform(programStageDataElement, dataelement,
                                            searchValueDataElement(programStageDataElement.dataElement().uid(), event.trackedEntityDataValues()), stageSection.uid(), event.status()));
                                }
                            }
                        }

                    } else {
                        for (ProgramStageDataElement programStageDataElement : stageDataElements) {
                            DataElement dataelement = d2.dataElementModule().dataElements().uid(programStageDataElement.dataElement().uid()).blockingGet();
                            fields.add(transform(programStageDataElement, dataelement,
                                    searchValueDataElement(programStageDataElement.dataElement().uid(), event.trackedEntityDataValues()), null, event.status()));

                        }
                    }
                    return fields;
                }).toFlowable();
    }

    @Override
    public Flowable<Result<RuleEffect>> calculate() {
        if(ruleEngineHelper!=null) {
            return Flowable.just(ruleEngineHelper.evaluate()).map(Result::success);
        }else{
            return Flowable.just(Result.success(new ArrayList<>()));
        }
    }

    @NonNull
    private FieldUiModel transform(@NonNull ProgramStageDataElement stage, DataElement dataElement, String value, String programStageSection, EventStatus eventStatus) {
        String uid = dataElement.uid();
        String displayName = dataElement.displayName();
        String valueTypeName = dataElement.valueType().name();
        boolean mandatory = stage.compulsory();
        String optionSet = dataElement.optionSetUid();
        String dataValue = value;
        List<Option> option = optionSet != null ? d2.optionModule().options().byOptionSetUid().eq(optionSet).byCode().eq(dataValue).blockingGet() : new ArrayList<>();
        boolean allowFutureDates = stage.allowFutureDate();
        String formName = dataElement.displayFormName();
        String description = dataElement.displayDescription();

        OptionSetConfiguration optionSetConfig = null;
        if (optionSet != null) {
            List<Option> dataValueOptions = d2.optionModule().options().byOptionSetUid().eq(optionSet).byCode().eq(dataValue).blockingGet();
            if (!dataValueOptions.isEmpty()) {
                dataValue = option.get(0).displayName();
            }
            optionSetConfig = OptionSetConfiguration.Companion.config(
                    d2.optionModule().options().byOptionSetUid().eq(optionSet).blockingCount(),
                    () -> {
                        List<Option> options = d2.optionModule().options().byOptionSetUid().eq(optionSet).blockingGet();
                        HashMap<String, MetadataIconData> metadataIconMap = new HashMap<>();
                        for (Option optionItem : options) {
                            metadataIconMap.put(optionItem.uid(), metadataIconProvider.invoke(optionItem.style()));
                        }

                        return new OptionSetConfiguration.OptionConfigData(
                                options,
                                metadataIconMap
                        );
                    }
            );
        }

        ValueTypeDeviceRendering fieldRendering = stage.renderType() == null ? null : stage.renderType().mobile();

        ObjectStyle objectStyle = d2.dataElementModule().dataElements().uid(uid).blockingGet().style();

        return fieldFactory.create(uid,
                formName == null ? displayName : formName,
                ValueType.valueOf(valueTypeName),
                mandatory,
                optionSet,
                dataValue,
                programStageSection,
                allowFutureDates,
                eventStatus == EventStatus.ACTIVE,
                null,
                description,
                fieldRendering,
                objectStyle,
                dataElement.fieldMask(),
                optionSetConfig,
                null,
                null,
                null,
                null);
    }

    private String searchValueDataElement(String dataElement, List<TrackedEntityDataValue> dataValues) {
        for (TrackedEntityDataValue dataValue : dataValues)
            if (dataValue.dataElement().equals(dataElement)) {
                return dataValue.value();
            }

        return "";
    }

    @Override
    public Flowable<EventEditableStatus> getEditableStatus() {
        return d2.eventModule().eventService().getEditableStatus(eventUid).toFlowable();
    }

    @Override
    public Observable<String> permanentReferral(
            String enrollmentUid,
            @NonNull String teiUid,
            @NonNull String programUid,
            @NonNull String programStage,
            @NonNull Date dueDate,
            @NonNull String orgUnitUid,
            @Nullable String categoryOptionsUid,
            @Nullable String categoryOptionComboUid,
            @NonNull Geometry geometry
    ) {

        d2.trackedEntityModule().ownershipManager()
                .blockingTransfer(teiUid, programUid, orgUnitUid);
        return scheduleEvent(
                enrollmentUid,
                teiUid,
                programUid,
                programStage,
                dueDate,
                orgUnitUid,
                categoryOptionsUid,
                categoryOptionComboUid,
                geometry
        );

    }

}
