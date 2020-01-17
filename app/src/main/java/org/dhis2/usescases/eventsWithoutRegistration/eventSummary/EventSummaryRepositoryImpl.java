package org.dhis2.usescases.eventsWithoutRegistration.eventSummary;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.R;
import org.dhis2.data.forms.FormRepository;
import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactory;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.DhisTextUtils;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.option.Option;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageDataElement;
import org.hisp.dhis.android.core.program.ProgramStageSection;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;
import org.hisp.dhis.rules.models.RuleDataValue;
import org.hisp.dhis.rules.models.RuleEffect;
import org.hisp.dhis.rules.models.RuleEvent;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;


/**
 * QUADRAM. Created by Cristian on 22/03/2018.
 */

public class EventSummaryRepositoryImpl implements EventSummaryRepository {


    private final FieldViewModelFactory fieldFactory;

    @NonNull
    private final FormRepository formRepository;

    @Nullable
    private final String eventUid;

    private final D2 d2;

    public EventSummaryRepositoryImpl(@NonNull Context context,
                                      @NonNull FormRepository formRepository,
                                      @Nullable String eventUid,
                                      @NonNull D2 d2) {
        this.formRepository = formRepository;
        this.eventUid = eventUid;
        this.d2 = d2;
        fieldFactory = new FieldViewModelFactoryImpl(
                context.getString(R.string.enter_text),
                context.getString(R.string.enter_long_text),
                context.getString(R.string.enter_number),
                context.getString(R.string.enter_integer),
                context.getString(R.string.enter_positive_integer),
                context.getString(R.string.enter_negative_integer),
                context.getString(R.string.enter_positive_integer_or_zero),
                context.getString(R.string.filter_options),
                context.getString(R.string.choose_date));
    }

    @NonNull
    @Override
    public Flowable<List<FormSectionViewModel>> programStageSections(String eventUid) {
        return d2.eventModule().events().uid(eventUid).get()
                .map(eventSingle -> {
                    List<FormSectionViewModel> formSection = new ArrayList<>();
                    if (eventSingle.deleted() == null || !eventSingle.deleted()) {
                        ProgramStage stage = d2.programModule().programStages().uid(eventSingle.programStage()).blockingGet();
                        List<ProgramStageSection> stageSections = d2.programModule().programStageSections().byProgramStageUid().eq(stage.uid()).blockingGet();
                        if (stageSections.size() > 0) {
                            for (ProgramStageSection section : stageSections)
                                formSection.add(FormSectionViewModel.createForSection(eventUid, section.uid(), section.displayName(),
                                        section.renderType().mobile() != null ? section.renderType().mobile().type().name() : null));
                        } else
                            formSection.add(FormSectionViewModel.createForProgramStageWithLabel(eventUid, stage.displayName(), stage.uid()));
                    }
                    return formSection;
                }).toFlowable();
    }

    @Override
    public boolean isEnrollmentOpen() {
        boolean isEnrollmentOpen = true;
        if (d2.eventModule().events().byUid().eq(eventUid).one().blockingExists()) {
            isEnrollmentOpen = d2.enrollmentModule().enrollments().byUid()
                    .eq(d2.eventModule().events().byUid().eq(eventUid).one().blockingGet().enrollment())
                    .one().blockingGet().status() == EnrollmentStatus.ACTIVE;
        }
        return isEnrollmentOpen;
    }

    @NonNull
    @Override
    public Flowable<List<FieldViewModel>> list(String section, String eventUid) {
        return d2.eventModule().events().withTrackedEntityDataValues().uid(eventUid).get()
                .map(event -> {
                    List<FieldViewModel> fields = new ArrayList<>();
                    ProgramStage stage = d2.programModule().programStages().uid(event.programStage()).blockingGet();
                    List<ProgramStageDataElement> stageDataElements = d2.programModule().programStageDataElements().byProgramStage().eq(stage.uid()).blockingGet();

                    if (section != null) {
                        ProgramStageSection stageSection = d2.programModule().programStageSections().withDataElements().uid(section).blockingGet();
                        for (ProgramStageDataElement programStageDataElement : stageDataElements) {
                            if (UidsHelper.getUidsList(stageSection.dataElements()).contains(programStageDataElement.dataElement().uid())) {
                                DataElement dataelement = d2.dataElementModule().dataElements().uid(programStageDataElement.dataElement().uid()).blockingGet();
                                fields.add(transform(programStageDataElement, dataelement,
                                        searchValueDataElement(programStageDataElement.dataElement().uid(), event.trackedEntityDataValues()), section, event.status()));
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

    private String searchValueDataElement(String dataElement, List<TrackedEntityDataValue> dataValues) {
        for (TrackedEntityDataValue dataValue : dataValues)
            if (dataValue.dataElement().equals(dataElement)) {
                return dataValue.value();
            }

        return "";
    }

    @NonNull
    private FieldViewModel transform(@NonNull ProgramStageDataElement stage, DataElement dataElement, String value, String programStageSection, EventStatus eventStatus) {
        String uid = dataElement.uid();
        String displayName = dataElement.displayName();
        String valueTypeName = dataElement.valueType().name();
        boolean mandatory = stage.compulsory();
        String optionSet = dataElement.optionSetUid();
        String dataValue = value;
        List<Option> option = optionSet!=null?d2.optionModule().options().byOptionSetUid().eq(optionSet).byCode().eq(dataValue).blockingGet() : new ArrayList<>();
        boolean allowFurureDates = stage.allowFutureDate();
        String formName = dataElement.displayFormName();
        String description = dataElement.displayDescription();

        int optionCount = 0;
        if (!option.isEmpty()) {
            dataValue = option.get(0).displayName();
            option.size();
        }

        ValueTypeDeviceRendering fieldRendering = stage.renderType() == null ? null : stage.renderType().mobile();

        ObjectStyle objectStyle = d2.dataElementModule().dataElements().uid(uid).blockingGet().style();

        if (ValueType.valueOf(valueTypeName) == ValueType.ORGANISATION_UNIT && !DhisTextUtils.Companion.isEmpty(dataValue)) {
            dataValue = dataValue + "_ou_" + d2.organisationUnitModule().organisationUnits().uid(dataValue).blockingGet().displayName();
        }
        return fieldFactory.create(uid, formName == null ? displayName : formName,
                ValueType.valueOf(valueTypeName), mandatory, optionSet, dataValue,
                programStageSection, allowFurureDates,
                eventStatus == EventStatus.ACTIVE,
                null, description, fieldRendering, optionCount, objectStyle, dataElement.fieldMask());
    }

    @NonNull
    @Override
    public Flowable<Result<RuleEffect>> calculate() {
        return queryDataValues(eventUid)
                .switchMap(this::queryEvent)
                .switchMap(
                        event -> formRepository.ruleEngine()
                                .switchMap(ruleEngine -> Flowable.fromCallable(ruleEngine.evaluate(event))
                                        .map(Result::success)
                                        .onErrorReturn(error -> Result.failure(new Exception(error)))
                                )
                );
    }

    @Override
    public Observable<Event> changeStatus(String eventUid) {
        return d2.eventModule().events().uid(eventUid).get()
                .map(event -> {
                    switch (event.status()) {
                        case ACTIVE:
                        case SKIPPED:
                        case VISITED:
                        case SCHEDULE:
                        case OVERDUE:
                            d2.eventModule().events().uid(event.uid()).setStatus(EventStatus.COMPLETED);
                            d2.eventModule().events().uid(event.uid()).setCompletedDate(DateUtils.getInstance().getToday());
                            break;
                        case COMPLETED:
                            d2.eventModule().events().uid(event.uid()).setStatus(EventStatus.ACTIVE);
                            break;
                    }
                    return event;
                }).toObservable();
    }

    @Override
    public Flowable<Event> getEvent(String eventId) {
        return d2.eventModule().events().uid(eventId).get().toFlowable();
    }

    @Override
    public Observable<Boolean> accessDataWrite(String eventId) {
        return d2.eventModule().events().uid(eventId).get()
                .map(Event::programStage)
                .flatMap(programStageUid -> d2.programModule().programStages().uid(programStageUid).get())
                .flatMap(programStage -> d2.programModule().programs().uid(programStage.program().uid()).get()
                        .map(program -> program.access().data().write() && programStage.access().data().write()))
                .toObservable();
    }

    @NonNull
    private Flowable<RuleEvent> queryEvent(@NonNull List<RuleDataValue> dataValues) {
        return d2.eventModule().events().uid(eventUid)
                .get()
                .map(event ->
                        RuleEvent.builder()
                                .event(eventUid)
                                .programStage(event.programStage())
                                .programStageName(d2.programModule().programStages().uid(event.programStage()).blockingGet().displayName())
                                .status(RuleEvent.Status.valueOf(event.status().name()))
                                .eventDate(event.eventDate())
                                .dueDate(event.dueDate() != null ? event.dueDate() : event.eventDate())
                                .organisationUnit(event.organisationUnit())
                                .organisationUnitCode(d2.organisationUnitModule().organisationUnits().uid(event.organisationUnit()).blockingGet().code())
                                .dataValues(dataValues)
                                .build()
                ).toFlowable();
    }

    @NonNull
    private Flowable<List<RuleDataValue>> queryDataValues(String eventUid) {
        return d2.eventModule().events().uid(eventUid).get()
                .flatMap(event -> d2.trackedEntityModule().trackedEntityDataValues()
                        .byEvent().eq(eventUid)
                        .get()
                        .toFlowable()
                        .flatMapIterable(list -> list)
                        .map(dataValue ->
                                RuleDataValue.create(event.eventDate(), event.programStage(), dataValue.dataElement(), dataValue.value())
                        ).toList()).toFlowable();
    }

    @Override
    public Observable<Program> getProgramWithId(String programUid) {
        return d2.programModule().programs().withTrackedEntityType()
                .uid(programUid).get().toObservable();
    }
}