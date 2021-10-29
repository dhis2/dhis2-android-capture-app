package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.RuleEngineRepository;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory;
import org.dhis2.data.forms.dataentry.fields.coordinate.CoordinateViewModel;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.form.model.RowAction;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.FeatureType;
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
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.processors.FlowableProcessor;
import timber.log.Timber;

public class EventInitialRepositoryImpl implements EventInitialRepository {

    private final FieldViewModelFactory fieldFactory;
    private final RuleEngineRepository ruleEngineRepository;

    private final String eventUid;
    private final D2 d2;
    private final String stageUid;

    EventInitialRepositoryImpl(String eventUid,
                               String stageUid,
                               D2 d2,
                               FieldViewModelFactory fieldFactory,
                               RuleEngineRepository ruleEngineRepository) {
        this.eventUid = eventUid;
        this.stageUid = stageUid;
        this.d2 = d2;
        this.fieldFactory = fieldFactory;
        this.ruleEngineRepository = ruleEngineRepository;
    }

    @NonNull
    @Override
    public Observable<Event> event(String eventId) {
        return d2.eventModule().events().uid(eventId).get().toObservable();
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnit>> filteredOrgUnits(String date, String programId, String parentId) {
        return (parentId == null ? orgUnits(programId) : orgUnits(programId, parentId))
                .map(organisationUnits -> {
                    if (date == null) {
                        return organisationUnits;
                    }
                    Iterator<OrganisationUnit> iterator = organisationUnits.iterator();
                    while (iterator.hasNext()) {
                        OrganisationUnit organisationUnit = iterator.next();
                        if (organisationUnit.openingDate() != null && organisationUnit.openingDate().after(DateUtils.databaseDateFormat().parse(date))
                                || organisationUnit.closedDate() != null && organisationUnit.closedDate().before(DateUtils.databaseDateFormat().parse(date)))
                            iterator.remove();
                    }
                    return organisationUnits;
                });
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnit>> orgUnits(String programId) {
        return d2.organisationUnitModule().organisationUnits()
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byProgramUids(Collections.singletonList(programId))
                .get()
                .toObservable();
    }

    public Observable<List<OrganisationUnit>> orgUnits(String programId, String parentUid) {
        return d2.organisationUnitModule().organisationUnits()
                .byProgramUids(Collections.singletonList(programId))
                .byParentUid().eq(parentUid)
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .get()
                .toObservable();
    }

    @NonNull
    @Override
    public Observable<CategoryCombo> catCombo(String programUid) {
        return d2.programModule().programs().uid(programUid).get()
                .flatMap(program -> d2.categoryModule().categoryCombos()
                        .withCategories()
                        .withCategoryOptionCombos()
                        .uid(program.categoryComboUid()
                        ).get()).toObservable();
    }

    @Override
    public Observable<List<CategoryOptionCombo>> catOptionCombos(String catOptionComboUid) {
        return d2.categoryModule().categoryOptionCombos().byCategoryComboUid().eq(catOptionComboUid).get().toObservable();
    }

    @Override
    public Flowable<Map<String, CategoryOption>> getOptionsFromCatOptionCombo(String eventId) {
        return d2.eventModule().events().uid(eventUid).get().toFlowable()
                .flatMap(event -> catCombo(event.program()).toFlowable(BackpressureStrategy.LATEST)
                        .flatMap(categoryCombo -> {
                            Map<String, CategoryOption> map = new HashMap<>();
                            if (!categoryCombo.isDefault() && event.attributeOptionCombo() != null) {
                                List<CategoryOption> selectedCatOptions = d2.categoryModule().categoryOptionCombos().withCategoryOptions().uid(event.attributeOptionCombo()).blockingGet().categoryOptions();
                                for (Category category : categoryCombo.categories()) {
                                    for (CategoryOption categoryOption : selectedCatOptions) {
                                        List<CategoryOption> categoryOptions = d2.categoryModule().categoryOptions().byCategoryUid(category.uid()).blockingGet();
                                        if (categoryOptions.contains(categoryOption))
                                            map.put(category.uid(), categoryOption);
                                    }
                                }
                            }

                            return Flowable.just(map);
                        }));
    }

    @Override
    public Date getStageLastDate(String programStageUid, String enrollmentUid) {
        List<Event> activeEvents = d2.eventModule().events().byEnrollmentUid().eq(enrollmentUid).byProgramStageUid().eq(programStageUid)
                .orderByEventDate(RepositoryScope.OrderByDirection.DESC).blockingGet();
        List<Event> scheduleEvents = d2.eventModule().events().byEnrollmentUid().eq(enrollmentUid).byProgramStageUid().eq(programStageUid)
                .orderByDueDate(RepositoryScope.OrderByDirection.DESC).blockingGet();

        Date activeDate = null;
        Date scheduleDate = null;
        if (!activeEvents.isEmpty()) {
            activeDate = activeEvents.get(0).eventDate();
        }
        if (!scheduleEvents.isEmpty())
            scheduleDate = scheduleEvents.get(0).dueDate();

        if (activeDate != null) {
            return activeDate;
        } else if (scheduleDate != null) {
            return scheduleDate;
        } else {
            return Calendar.getInstance().getTime();
        }
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

    @NonNull
    @Override
    public Observable<Event> editEvent(String trackedEntityInstance,
                                       String eventUid,
                                       String date,
                                       String orgUnitUid,
                                       String catComboUid,
                                       String catOptionCombo,
                                       Geometry geometry) {

        return Observable.fromCallable(() -> d2.eventModule().events().uid(eventUid))
                .map(eventRepository -> {
                    eventRepository.setEventDate(DateUtils.databaseDateFormat().parse(date));
                    eventRepository.setOrganisationUnitUid(orgUnitUid);
                    eventRepository.setAttributeOptionComboUid(catOptionCombo);
                    FeatureType featureType = d2.programModule().programStages().uid(eventRepository.blockingGet().programStage()).blockingGet().featureType();
                    if (featureType != null)
                        switch (featureType) {
                            case NONE:
                                break;
                            case POINT:
                            case POLYGON:
                            case MULTI_POLYGON:
                                eventRepository.setGeometry(geometry);
                                break;
                            default:
                                break;
                        }
                    return eventRepository.blockingGet();
                });
    }

    @Override
    public Observable<Boolean> accessDataWrite(String programUid) {
        if (eventUid != null) {
            return d2.eventModule().eventService().isEditable(eventUid).toObservable();
        } else {
            return d2.programModule().programStages().uid(stageUid).get().toObservable()
                    .map(programStage-> programStage.access().data().write());
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
    public Observable<OrganisationUnit> getOrganisationUnit(String orgUnitUid) {
        return d2.organisationUnitModule().organisationUnits().byUid().eq(orgUnitUid).one().get().toObservable();
    }

    @Override
    public Observable<ObjectStyle> getObjectStyle(String uid) {
        return d2.programModule().programStages().uid(uid).get()
                .map(programStage -> {
                    Program program = d2.programModule().programs().uid(programStage.program().uid()).blockingGet();
                    ObjectStyle programStyle = program.style() != null ? program.style() : ObjectStyle.builder().build();
                    if (programStage.style() != null) {
                        programStage.style().icon();
                        programStage.style().color();
                        return ObjectStyle.builder()
                                .icon(programStage.style().icon() != null ? programStage.style().icon() : programStyle.icon())
                                .color(programStage.style().color() != null ? programStage.style().color() : programStyle.color())
                                .build();
                    } else {
                        return programStyle;
                    }
                }).toObservable();
    }

    @Override
    public String getCategoryOptionCombo(String categoryComboUid, List<String> categoryOptionsUid) {
        return d2.categoryModule().categoryOptionCombos()
                .byCategoryComboUid().eq(categoryComboUid)
                .byCategoryOptions(categoryOptionsUid)
                .one().blockingGet().uid();
    }

    @Override
    public CategoryOption getCatOption(String selectedOption) {
        return d2.categoryModule().categoryOptions().uid(selectedOption).blockingGet();
    }

    @Override
    public int getCatOptionSize(String uid) {
        return d2.categoryModule().categoryOptions()
                .byCategoryUid(uid)
                .byAccessDataWrite().isTrue()
                .blockingCount();
    }

    @Override
    public List<CategoryOption> getCategoryOptions(String categoryUid) {
        return d2.categoryModule().categories()
                .withCategoryOptions()
                .uid(categoryUid)
                .blockingGet().categoryOptions();
    }

    @Override
    public boolean showCompletionPercentage() {
        if (d2.settingModule().appearanceSettings().blockingExists()) {
            return d2.settingModule().appearanceSettings().getCompletionSpinnerByUid(
                    d2.eventModule().events().uid(eventUid).blockingGet().program()
            ).visible();
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
                                    ProgramStageSectionRenderingType.LISTING.name()));
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
        return ruleEngineRepository.calculate();
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

        int optionCount = 0;
        if (!option.isEmpty()) {
            dataValue = option.get(0).displayName();
            option.size();
        }

        ValueTypeDeviceRendering fieldRendering = stage.renderType() == null ? null : stage.renderType().mobile();

        ObjectStyle objectStyle = d2.dataElementModule().dataElements().uid(uid).blockingGet().style();

        return fieldFactory.create(uid, formName == null ? displayName : formName,
                ValueType.valueOf(valueTypeName), mandatory, optionSet, dataValue,
                programStageSection, allowFutureDates,
                eventStatus == EventStatus.ACTIVE,
                null, description, fieldRendering, optionCount, objectStyle, dataElement.fieldMask(), null, null, null);
    }

    private String searchValueDataElement(String dataElement, List<TrackedEntityDataValue> dataValues) {
        for (TrackedEntityDataValue dataValue : dataValues)
            if (dataValue.dataElement().equals(dataElement)) {
                return dataValue.value();
            }

        return "";
    }

    @Override
    public Single<CoordinateViewModel> getGeometryModel(String programUid, FlowableProcessor<RowAction> processor) {
        return Single.fromCallable(() -> {
            ArrayList<EventStatus> nonEditableStatus = new ArrayList<>();
            nonEditableStatus.add(EventStatus.COMPLETED);
            nonEditableStatus.add(EventStatus.SKIPPED);
            boolean shouldBlockEdition = eventUid != null &&
                    !d2.eventModule().eventService().blockingIsEditable(eventUid) &&
                    nonEditableStatus.contains(d2.eventModule().events().uid(eventUid).blockingGet().status());
            FeatureType featureType = programStageWithId(stageUid).blockingFirst().featureType();
            boolean accessDataWrite = accessDataWrite(programUid).blockingFirst() && isEnrollmentOpen();
            String coordinatesValue = null;
            if (eventUid != null) {
                Geometry geometry = d2.eventModule().events().uid(eventUid).blockingGet().geometry();
                if (geometry != null) {
                    coordinatesValue = geometry.coordinates();
                }
            }
            return (CoordinateViewModel) fieldFactory.create(
                    "",
                    "",
                    ValueType.COORDINATE,
                    false,
                    null,
                    coordinatesValue,
                    null,
                    null,
                    accessDataWrite && !shouldBlockEdition,
                    null,
                    null,
                    null,
                    null,
                    ObjectStyle.builder().build(),
                    null,
                    null,
                    null,
                    featureType
            );
        });
    }

    @Override
    public Flowable<EventEditableStatus> getEditableStatus() {
        return d2.eventModule().eventService().getEditableStatus(eventUid).toFlowable();
    }

    @Override
    public int getMinDaysFromStartByProgramStage(String programStageUid) {
        ProgramStage programStage = d2.programModule().programStages().uid(programStageUid).blockingGet();
        if (programStage.minDaysFromStart() != null) {
            return programStage.minDaysFromStart();
        }
        return 0;
    }
}