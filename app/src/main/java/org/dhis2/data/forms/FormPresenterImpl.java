package org.dhis2.data.forms;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.dhis2.data.forms.dataentry.EnrollmentRuleEngineRepository;
import org.dhis2.data.forms.dataentry.EventsRuleEngineRepository;
import org.dhis2.data.forms.dataentry.RuleEngineRepository;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.Result;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.rules.models.RuleAction;
import org.hisp.dhis.rules.models.RuleActionHideField;
import org.hisp.dhis.rules.models.RuleActionHideSection;
import org.hisp.dhis.rules.models.RuleActionWarningOnCompletion;
import org.hisp.dhis.rules.models.RuleEffect;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import rx.exceptions.OnErrorNotImplementedException;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static org.dhis2.utils.Preconditions.isNull;


class FormPresenterImpl implements FormPresenter {

    @NonNull
    private final FormViewArguments formViewArguments;

    @NonNull
    private final SchedulerProvider schedulerProvider;

    @NonNull
    private final FormRepository formRepository;

    @NonNull
    private final CompositeDisposable compositeDisposable;
    @NonNull
    private final RuleEngineRepository ruleEngineRepository;

    @NonNull
    private final FlowableProcessor<String> processor;
    private FormView view;

    private boolean isEvent = false;

    FormPresenterImpl(@NonNull FormViewArguments formViewArguments,
                      @NonNull SchedulerProvider schedulerProvider,
                      @NonNull BriteDatabase briteDatabase,
                      @NonNull FormRepository formRepository) {
        this.formViewArguments = formViewArguments;
        this.formRepository = formRepository;
        this.schedulerProvider = schedulerProvider;
        this.compositeDisposable = new CompositeDisposable();
        if (formViewArguments.type() == FormViewArguments.Type.ENROLLMENT) {
            isEvent = false;
            this.ruleEngineRepository = new EnrollmentRuleEngineRepository(briteDatabase, formRepository, formViewArguments.uid());
        }
        else {
            isEvent = true;
            this.ruleEngineRepository = new EventsRuleEngineRepository(briteDatabase, formRepository, formViewArguments.uid());
        }

        this.processor = PublishProcessor.create();
    }

    @Override
    public void onAttach(@NonNull FormView view) {
        isNull(view, "FormView must not be null");
        this.view = view;

        compositeDisposable.add(formRepository.title()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(view.renderTitle(), Timber::e));

        compositeDisposable.add(formRepository.reportDate()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .filter(date -> !isEmpty(date))
                .map(date -> {
                    try {
                        return DateUtils.uiDateFormat().format(DateUtils.databaseDateFormat().parse(date));
                    } catch (ParseException e) {
                        Timber.e(e, "DashboardRepository: Unable to parse date. Expected format: " +
                                DateUtils.databaseDateFormat().toPattern() + ". Input: " + date);
                        return date;
                    }
                })
                .subscribe(view.renderReportDate(), Timber::e));

        compositeDisposable.add(formRepository.incidentDate()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .filter(programModelAndDate -> programModelAndDate.val0().displayIncidentDate())
                .subscribe(view.renderIncidentDate(), Timber::e)
        );

        compositeDisposable.add(formRepository.getAllowDatesInFuture()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(program -> view.initReportDatePicker(program.selectEnrollmentDatesInFuture(), program.selectIncidentDatesInFuture()),
                        Timber::e)
        );

        //region SECTIONS
        Flowable<List<FormSectionViewModel>> sectionsFlowable = formRepository.sections();
        Flowable<Result<RuleEffect>> ruleEffectFlowable = ruleEngineRepository.calculate()
                .subscribeOn(schedulerProvider.computation());

        // Combining results of two repositories into a single stream.
        Flowable<List<FormSectionViewModel>> sectionModelsFlowable = Flowable.zip(
                sectionsFlowable, ruleEffectFlowable, this::applyEffects);

        compositeDisposable.add(processor.startWith("init")
                .flatMap(data -> sectionModelsFlowable)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(view.renderSectionViewModels(), Timber::e));

        //endregion

        compositeDisposable.add(view.reportDateChanged()
                .subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.io())
                .subscribe(formRepository.storeReportDate(), Timber::e));

        compositeDisposable.add(view.incidentDateChanged()
                .filter(date -> date != null)
                .subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.io())
                .subscribe(formRepository.storeIncidentDate(), Timber::e));

        compositeDisposable.add(view.reportCoordinatesChanged()
                .filter(latLng -> latLng != null)
                .subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.io())
                .subscribe(formRepository.storeCoordinates(), Timber::e));

        ConnectableObservable<ReportStatus> statusChangeObservable = view.eventStatusChanged()
                .publish();

        compositeDisposable.add(statusChangeObservable
                .filter(eventStatus -> formViewArguments.type() != FormViewArguments.Type.ENROLLMENT)
                .subscribeOn(schedulerProvider.ui())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(/*formRepository.storeReportStatus()*/view::onNext, throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                }));

        Observable<String> enrollmentDoneStream = statusChangeObservable
                .filter(eventStatus -> formViewArguments.type() == FormViewArguments.Type.ENROLLMENT)
                .map(reportStatus -> formViewArguments.uid())
                .observeOn(schedulerProvider.io()).share();

        compositeDisposable.add(enrollmentDoneStream
               /* .flatMap(data -> checkMandatory().map(mandatoryRequired -> Pair.create(data, mandatoryRequired)))
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(data -> {
                    view.showMandatoryFieldsDialog();
                    return Observable.just(data);
                })
                .filter(data -> !data.val1()) //
                .map(data -> data.val0())*/
                .flatMap(formRepository::autoGenerateEvents) //Autogeneration of events
                .flatMap(data -> formRepository.useFirstStageDuringRegistration()) //Checks if first Stage Should be used
                .subscribeOn(schedulerProvider.io())
                .subscribe(
                        view.finishEnrollment(),
                        throwable -> {
                            throw new OnErrorNotImplementedException(throwable);
                        }));

        compositeDisposable.add(statusChangeObservable.connect());
    }

    private List<FieldViewModel> applyFieldViewEffects(
            @NonNull List<FieldViewModel> viewModels,
            @NonNull Result<RuleEffect> calcResult) {
        if (calcResult.error() != null) {
            calcResult.error().printStackTrace();
            return viewModels;
        }

        Map<String, FieldViewModel> fieldViewModels = toFieldViewMap(viewModels);
        applyRuleFieldViewsEffects(fieldViewModels, calcResult);

        return new ArrayList<>(fieldViewModels.values());

    }

    private void applyRuleFieldViewsEffects(Map<String, FieldViewModel> fieldViewModels, Result<RuleEffect> calcResult) {
        for (RuleEffect ruleEffect : calcResult.items()) {
            RuleAction ruleAction = ruleEffect.ruleAction();

            if (ruleAction instanceof RuleActionHideField) {
                RuleActionHideField hideField = (RuleActionHideField) ruleAction;
                fieldViewModels.remove(hideField.field());
            } else if (ruleAction instanceof RuleActionHideSection) {
                RuleActionHideSection hideSection = (RuleActionHideSection) ruleAction;
                fieldViewModels.remove(hideSection.programStageSection());
            }
        }
    }

    @NonNull
    private static Map<String, FieldViewModel> toFieldViewMap(@NonNull List<FieldViewModel> fieldViewModels) {
        Map<String, FieldViewModel> map = new LinkedHashMap<>();
        for (FieldViewModel fieldViewModel : fieldViewModels) {
            map.put(fieldViewModel.uid(), fieldViewModel);
        }
        return map;
    }

    @NonNull
    private List<FormSectionViewModel> applyEffects(
            @NonNull List<FormSectionViewModel> viewModels,
            @NonNull Result<RuleEffect> calcResult) {
        if (calcResult.error() != null) {
            calcResult.error().printStackTrace();
            return viewModels;
        }

        Map<String, FormSectionViewModel> fieldViewModels = toMap(viewModels);
        applyRuleEffects(fieldViewModels, calcResult);

        return new ArrayList<>(fieldViewModels.values());
    }

    private void applyRuleEffects(Map<String, FormSectionViewModel> fieldViewModels, Result<RuleEffect> calcResult) {
        //TODO: APPLY RULE EFFECTS TO ALL MODELS
        for (RuleEffect ruleEffect : calcResult.items()) {
            RuleAction ruleAction = ruleEffect.ruleAction();

            if (ruleAction instanceof RuleActionHideSection) {
                RuleActionHideSection hideSection = (RuleActionHideSection) ruleAction;
                fieldViewModels.remove(hideSection.programStageSection());
            } else if (ruleAction instanceof RuleActionWarningOnCompletion) {
                RuleActionWarningOnCompletion warningOnCompletion = (RuleActionWarningOnCompletion) ruleAction;
                view.messageOnComplete(warningOnCompletion.content(), true);
            }
        }
    }

    @NonNull
    private static Map<String, FormSectionViewModel> toMap(@NonNull List<FormSectionViewModel> fieldViewModels) {
        Map<String, FormSectionViewModel> map = new LinkedHashMap<>();
        for (FormSectionViewModel fieldViewModel : fieldViewModels) {
            map.put(fieldViewModel.sectionUid(), fieldViewModel);
        }
        return map;
    }

    @Override
    public void onDetach() {
        compositeDisposable.clear();
    }

    @Override
    public void checkSections() {
        if (processor.hasSubscribers())
            processor.onNext("check");
        else {
            Flowable<List<FormSectionViewModel>> sectionsFlowable = formRepository.sections();
            Flowable<Result<RuleEffect>> ruleEffectFlowable = ruleEngineRepository.calculate()
                    .subscribeOn(schedulerProvider.computation());

            // Combining results of two repositories into a single stream.
            Flowable<List<FormSectionViewModel>> sectionModelsFlowable = Flowable.zip(
                    sectionsFlowable, ruleEffectFlowable, this::applyEffects);
            compositeDisposable.add(processor.startWith("init")
                    .flatMap(data -> sectionModelsFlowable)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(view.renderSectionViewModels(), Timber::e));

        }

    }

    @Override
    public void checkMandatoryFields() {
        Observable<List<FieldViewModel>> values = formRepository.fieldValues();
        Observable<Result<RuleEffect>> ruleEffect = ruleEngineRepository.calculate().toObservable()
                .subscribeOn(schedulerProvider.computation());

        Observable<List<FieldViewModel>> fieldValues = Observable.zip(
                values, ruleEffect, this::applyFieldViewEffects);

        CompositeDisposable disposable = new CompositeDisposable();
        disposable.add(fieldValues
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(data -> {
                    view.isMandatoryFieldsRequired(data);
                    disposable.clear();
                }, Timber::e)
        );
    }

    public Observable<Boolean> checkMandatory() {
        Observable<List<FieldViewModel>> values = formRepository.fieldValues();
        Observable<Result<RuleEffect>> ruleEffect = ruleEngineRepository.calculate().toObservable()
                .subscribeOn(schedulerProvider.computation());

        Observable<List<FieldViewModel>> fieldValues = Observable.zip(
                values, ruleEffect, this::applyFieldViewEffects);

        return fieldValues
                .map(data -> {
                    boolean mandatoryRequired = false;
                    for (FieldViewModel viewModel : data) {
                        if (viewModel.mandatory() && TextUtils.isEmpty(viewModel.value()))
                            mandatoryRequired = true;
                    }
                    return mandatoryRequired;
                });
    }

    private void deleteTrackedEntityAttributeValues(@NonNull String trackedEntityAttributeInstanceId) {
        formRepository.deleteTrackedEntityAttributeValues(trackedEntityAttributeInstanceId);
    }

    private void deleteEnrollment(@NonNull String trackedEntityAttributeInstanceId) {
        formRepository.deleteEnrollment(trackedEntityAttributeInstanceId);
    }

    private void deleteEvent(){
        formRepository.deleteEvent();
    }

    private void deleteTrackedEntityInstance(@NonNull String trackedEntityAttributeInstanceId){
        formRepository.deleteTrackedEntityInstance(trackedEntityAttributeInstanceId);
    }

    public void deleteCascade(){
        CompositeDisposable disposable = new CompositeDisposable();
        if (isEvent){
            deleteEvent();
            disposable.clear();
            view.onAllSavedDataDeleted();
        }
        else {
            disposable.add(formRepository.getTrackedEntityInstanceUid()
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(trackedEntityInstanceUid -> {
                                deleteTrackedEntityAttributeValues(trackedEntityInstanceUid);
                                deleteEnrollment(trackedEntityInstanceUid);
                                deleteTrackedEntityInstance(trackedEntityInstanceUid);
                                disposable.clear();
                                view.onAllSavedDataDeleted();
                            },
                            Timber::e)
            );
        }
    }
}