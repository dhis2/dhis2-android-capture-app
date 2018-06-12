package com.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dhis2.Bindings.Bindings;
import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.data.schedulers.SchedulerProvider;
import com.dhis2.usescases.eventsWithoutRegistration.eventSummary.EventSummaryRepository;
import com.dhis2.utils.DateUtils;
import com.dhis2.utils.OrgUnitUtils;
import com.dhis2.utils.Result;

import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.rules.models.RuleAction;
import org.hisp.dhis.rules.models.RuleActionHideField;
import org.hisp.dhis.rules.models.RuleActionShowError;
import org.hisp.dhis.rules.models.RuleActionShowWarning;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import rx.exceptions.OnErrorNotImplementedException;
import timber.log.Timber;

/**
 * Created by Cristian on 01/03/2018.
 */

public class EventInitialInteractor implements EventInitialContract.Interactor {

    private final MetadataRepository metadataRepository;
    private final EventInitialRepository eventInitialRepository;
    private final EventSummaryRepository eventSummaryRepository;
    private EventInitialContract.View view;
    private CompositeDisposable compositeDisposable;
    private ProgramModel programModel;
    private CategoryComboModel catCombo;
    private String eventUid;

    @NonNull
    private SchedulerProvider schedulerProvider;


    EventInitialInteractor(@NonNull EventSummaryRepository eventSummaryRepository,
                           @NonNull EventInitialRepository eventInitialRepository,
                           @NonNull MetadataRepository metadataRepository,
                           @NonNull SchedulerProvider schedulerProvider) {
        this.metadataRepository = metadataRepository;
        this.eventInitialRepository = eventInitialRepository;
        this.eventSummaryRepository = eventSummaryRepository;
        this.schedulerProvider = schedulerProvider;
        Bindings.setMetadataRepository(metadataRepository);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(EventInitialContract.View view, String programId, @Nullable String eventUid) {
        this.view = view;
        this.eventUid = eventUid;

        if (eventUid != null)
            compositeDisposable.add(
                    eventInitialRepository.event(eventUid)
                            .flatMap(
                                    (eventModel) -> {
                                        view.setEvent(eventModel);
                                        return metadataRepository.getProgramWithId(programId)
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread());
                                    }
                            )
                            .flatMap(
                                    programModel -> {
                                        this.programModel = programModel;
                                        view.setProgram(programModel);
                                        return metadataRepository.getCategoryComboWithId(programModel.categoryCombo())
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread());
                                    }
                            )
                            .flatMap(
                                    catCombo -> {
                                        this.catCombo = catCombo;
                                        return eventInitialRepository.catCombo(programModel.categoryCombo());
                                    }
                            )
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    catComboOptions -> view.setCatComboOptions(catCombo, catComboOptions),
                                    error -> Timber.log(1, error)
                            )
            );
        else
            compositeDisposable.add(
                    metadataRepository.getProgramWithId(programId)
                            .flatMap(
                                    programModel -> {
                                        this.programModel = programModel;
                                        view.setProgram(programModel);
                                        return metadataRepository.getCategoryComboWithId(programModel.categoryCombo());
                                    }
                            )
                            .flatMap(
                                    catCombo -> {
                                        this.catCombo = catCombo;
                                        return eventInitialRepository.catCombo(programModel.categoryCombo());
                                    }
                            )
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    catComboOptions -> view.setCatComboOptions(catCombo, catComboOptions),
                                    Timber::d
                            )
            );
        getOrgUnits();
        getProgramStages(programId);
        if (eventUid != null)
            getEventSections(eventUid);
    }

    private void getProgramStages(String programUid) {
        compositeDisposable.add(eventInitialRepository.programStage(programUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        programStageModel -> view.setProgramStage(programStageModel),
                        throwable -> view.showProgramStageSelection()
                ));
    }

    @Override
    public void getProgramStageWithId(String programStageUid) {
        compositeDisposable.add(eventInitialRepository.programStageWithId(programStageUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        programStageModel -> view.setProgramStage(programStageModel),
                        throwable -> view.showProgramStageSelection()
                ));
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }

    @Override
    public void getCatOption(String categoryOptionComboId) {
        compositeDisposable.add(metadataRepository.getCategoryOptionComboWithId(categoryOptionComboId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (catOption) -> view.setCatOption(catOption),
                        Timber::d)
        );
    }

    @Override
    public void getOrgUnits() {
        compositeDisposable.add(eventInitialRepository.orgUnits()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        orgUnits -> view.addTree(OrgUnitUtils.renderTree(view.getContext(), orgUnits)),
                        throwable -> view.renderError(throwable.getMessage())
                ));
    }

    @Override
    public void getFilteredOrgUnits(String date) {
        compositeDisposable.add(eventInitialRepository.filteredOrgUnits(date)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        orgUnits -> view.addTree(OrgUnitUtils.renderTree(view.getContext(), orgUnits)),
                        throwable -> view.renderError(throwable.getMessage())

                ));
    }

    @Override
    public void createNewEvent(String enrollmentUid, String programStageModelUid, String programUid, Date date, String orgUnitUid,
                               String categoryOptionComboUid, String categoryOptionsUid,
                               String latitude, String longitude) {
        compositeDisposable.add(
                eventInitialRepository.createEvent(enrollmentUid, null, view.getContext(), programUid,
                        programStageModelUid, date, orgUnitUid,
                        categoryOptionComboUid, categoryOptionsUid,
                        latitude, longitude)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(view::onEventCreated,
                                t -> view.renderError(t.getMessage()))
        );
    }

    @Override
    public void createNewEventPermanent(String enrollmentUid, String trackedEntityInstanceUid, String programStageModelUid,
                                        String programUid, Date date, String orgUnitUid,
                                        String categoryOptionComboUid, String categoryOptionsUid,
                                        String latitude, String longitude) {
        compositeDisposable.add(
                eventInitialRepository.createEvent(enrollmentUid, trackedEntityInstanceUid, view.getContext(),
                        programUid, programStageModelUid, date, orgUnitUid,
                        categoryOptionComboUid, categoryOptionsUid,
                        latitude, longitude)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                (eventUid) -> {
                                    compositeDisposable.add(eventInitialRepository.updateTrackedEntityInstance(trackedEntityInstanceUid, orgUnitUid)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(
                                                    (Void) -> view.onEventCreated(eventUid),
                                                    t -> view.renderError(t.getMessage())));

                                },
                                t -> view.renderError(t.getMessage()))
        );
    }


    @Override
    public void editEvent(String programStageModelUid, String eventUid, String date, String orgUnitUid, String catComboUid, String catOptionCombo, String latitude, String longitude) {
        compositeDisposable.add(eventInitialRepository.editEvent(eventUid, date, orgUnitUid, catComboUid, catOptionCombo, latitude, longitude)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (eventModel) -> view.onEventUpdated(eventModel.uid()),
                        Timber::e

                ));
    }

    @Override
    public void onDetach() {
        compositeDisposable.dispose();
    }


    @Override
    public void getEventSections(@NonNull String eventId) {
        compositeDisposable.add(eventSummaryRepository.programStageSections(eventId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::onEventSections,
                        Timber::e
                ));
    }


    @Override
    public void getSectionCompletion(@Nullable String sectionUid) {
        Flowable<List<FieldViewModel>> fieldsFlowable = eventSummaryRepository.list(sectionUid, eventUid);

        Flowable<Result<RuleEffect>> ruleEffectFlowable = eventSummaryRepository.calculate().subscribeOn(schedulerProvider.computation());

        // Combining results of two repositories into a single stream.
        Flowable<List<FieldViewModel>> viewModelsFlowable = Flowable.zip(fieldsFlowable, ruleEffectFlowable, this::applyEffects);

        compositeDisposable.add(viewModelsFlowable
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(view.showFields(sectionUid), throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                }));
    }

    @NonNull
    private List<FieldViewModel> applyEffects(
            @NonNull List<FieldViewModel> viewModels,
            @NonNull Result<RuleEffect> calcResult) {
        if (calcResult.error() != null) {
            calcResult.error().printStackTrace();
            return viewModels;
        }

        Map<String, FieldViewModel> fieldViewModels = toMap(viewModels);
        applyRuleEffects(fieldViewModels, calcResult);

        return new ArrayList<>(fieldViewModels.values());
    }

    @NonNull
    private static Map<String, FieldViewModel> toMap(@NonNull List<FieldViewModel> fieldViewModels) {
        Map<String, FieldViewModel> map = new LinkedHashMap<>();
        for (FieldViewModel fieldViewModel : fieldViewModels) {
            map.put(fieldViewModel.uid(), fieldViewModel);
        }
        return map;
    }

    private void applyRuleEffects(Map<String, FieldViewModel> fieldViewModels, Result<RuleEffect> calcResult) {
        //TODO: APPLY RULE EFFECTS TO ALL MODELS
        for (RuleEffect ruleEffect : calcResult.items()) {
            RuleAction ruleAction = ruleEffect.ruleAction();
            if (ruleAction instanceof RuleActionShowWarning) {
                RuleActionShowWarning showWarning = (RuleActionShowWarning) ruleAction;
                FieldViewModel model = fieldViewModels.get(showWarning.field());

                if (model != null && model instanceof EditTextViewModel) {
                    fieldViewModels.put(showWarning.field(),
                            ((EditTextViewModel) model).withWarning(showWarning.content()));
                }
            } else if (ruleAction instanceof RuleActionShowError) {
                RuleActionShowError showError = (RuleActionShowError) ruleAction;
                FieldViewModel model = fieldViewModels.get(showError.field());

                if (model != null && model instanceof EditTextViewModel) {
                    fieldViewModels.put(showError.field(),
                            ((EditTextViewModel) model).withError(showError.content()));
                }
            } else if (ruleAction instanceof RuleActionHideField) {
                RuleActionHideField hideField = (RuleActionHideField) ruleAction;
                fieldViewModels.remove(hideField.field());
            }
        }
    }

    @Override
    public void getEvents(String programUid, String enrollmentUid, String programStageUid, PeriodType periodType) {
        compositeDisposable.add(
                eventInitialRepository.getEventsFromProgramStage(programUid, enrollmentUid, programStageUid)
                        .map(events -> DateUtils.getInstance().getNewDate(events, periodType))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(date ->
                                        view.setReportDate(DateUtils.uiDateFormat().format(date)),
                                Timber::d
                        )
        );
    }
}
