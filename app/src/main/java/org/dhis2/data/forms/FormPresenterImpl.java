package org.dhis2.data.forms;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.forms.dataentry.EnrollmentRuleEngineRepository;
import org.dhis2.data.forms.dataentry.EventsRuleEngineRepository;
import org.dhis2.data.forms.dataentry.RuleEngineRepository;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.rules.models.RuleAction;
import org.hisp.dhis.rules.models.RuleActionErrorOnCompletion;
import org.hisp.dhis.rules.models.RuleActionHideField;
import org.hisp.dhis.rules.models.RuleActionHideSection;
import org.hisp.dhis.rules.models.RuleActionSetMandatoryField;
import org.hisp.dhis.rules.models.RuleActionShowError;
import org.hisp.dhis.rules.models.RuleActionWarningOnCompletion;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
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
    private final D2 d2;
    private FormView view;

    private boolean isEvent;

    FormPresenterImpl(@NonNull FormViewArguments formViewArguments,
                      @NonNull SchedulerProvider schedulerProvider,
                      @NonNull BriteDatabase briteDatabase,
                      @NonNull FormRepository formRepository,
                      @NonNull D2 d2) {
        this.d2 = d2;
        this.formViewArguments = formViewArguments;
        this.formRepository = formRepository;
        this.schedulerProvider = schedulerProvider;
        this.compositeDisposable = new CompositeDisposable();
        if (formViewArguments.type() == FormViewArguments.Type.ENROLLMENT) {
            isEvent = false;
            this.ruleEngineRepository = new EnrollmentRuleEngineRepository(briteDatabase, formRepository, formViewArguments.uid(), d2);
        } else {
            isEvent = true;
            this.ruleEngineRepository = new EventsRuleEngineRepository(briteDatabase, formRepository, formViewArguments.uid());
        }

        this.processor = PublishProcessor.create();
    }

    @Override
    public String getEnrollmentOu(String enrollmentUid) {
        if (d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingExists())
            return d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet().organisationUnit();
        else
            return null;
    }

    @Override
    public void onAttach(@NonNull FormView view) {
        isNull(view, "FormView must not be null");
        this.view = view;

        compositeDisposable.add(formRepository.title()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(view.renderTitle(), Timber::e));

        if (!isEvent) {
            compositeDisposable.add(formRepository.reportDate()
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .filter(programAndDate -> !isEmpty(programAndDate.val1()))
                    .subscribe(view.renderReportDate(), Timber::e));

            compositeDisposable.add(formRepository.incidentDate()
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .filter(programAndDate -> programAndDate.val0().displayIncidentDate())
                    .subscribe(view.renderIncidentDate(), Timber::e)
            );

            compositeDisposable.add(formRepository.captureCoodinates()
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(view.renderCaptureCoordinates(), Timber::e)
            );

            compositeDisposable.add(formRepository.captureTeiCoordinates()
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(view.renderTeiCoordinates(), Timber::e)
            );

            compositeDisposable.add(formRepository.getAllowDatesInFuture()
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(program -> view.initReportDatePicker(program.selectEnrollmentDatesInFuture(), program.selectIncidentDatesInFuture()),
                            Timber::e)
            );


        } else {
            view.hideDates();
            compositeDisposable.add(formRepository.getProgramCategoryCombo(null)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(hasOptionCatComboAndOption -> {
                                if (!hasOptionCatComboAndOption.val0() && !hasOptionCatComboAndOption.val1().isDefault())
                                    view.showCatComboDialog(hasOptionCatComboAndOption.val1(), hasOptionCatComboAndOption.val2());
                            },
                            Timber::e));
        }

        compositeDisposable.add(formRepository.getOrgUnitDates()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(orgUnit -> view.setMinMaxDates(orgUnit.openingDate(), orgUnit.closedDate()),
                        Timber::e));

        //region SECTIONS
        Flowable<List<FormSectionViewModel>> sectionsFlowable = formRepository.sections();
        Flowable<Result<RuleEffect>> ruleEffectFlowable = ruleEngineRepository.calculate()
                .subscribeOn(schedulerProvider.computation()).onErrorReturn(throwable -> Result.failure(new Exception(throwable)));

        // Combining results of two repositories into a single stream.
        Flowable<List<FormSectionViewModel>> sectionModelsFlowable = Flowable.zip(
                sectionsFlowable, ruleEffectFlowable, this::applyEffects);

        compositeDisposable.add(processor.startWith("init").debounce(500, TimeUnit.MILLISECONDS)
                .flatMap(data -> sectionModelsFlowable)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(view.renderSectionViewModels(), Timber::e));

        //endregion

        compositeDisposable.add(view.reportDateChanged()
                .switchMap(formRepository::saveReportDate)
                .subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.io())
                .subscribe(saved -> Timber.d("reportDate saved"), Timber::e));

        compositeDisposable.add(view.incidentDateChanged()
                .switchMap(formRepository::saveIncidentDate)
                .subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.io())
                .subscribe(saved -> Timber.d("incidentDate saved"), Timber::e));

        compositeDisposable.add(view.reportCoordinatesChanged()
                .filter(geometry -> geometry != null)
                .subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.io())
                .subscribe(formRepository.storeCoordinates(), Timber::e));

        compositeDisposable.add(view.reportCoordinatesCleared()
                .subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.io())
                .subscribe(formRepository.clearCoordinates(), Timber::e));

        compositeDisposable.add(view.teiCoordinatesChanged()
                .filter(geometry -> geometry != null)
                .subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.io())
                .subscribe(formRepository.storeTeiCoordinates(), Timber::e));

        ConnectableObservable<ReportStatus> statusChangeObservable = view.eventStatusChanged()
                .publish();

        compositeDisposable.add(statusChangeObservable
                .filter(eventStatus -> formViewArguments.type() != FormViewArguments.Type.ENROLLMENT)
                .subscribeOn(schedulerProvider.ui())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::onNext, throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                }));

        Observable<String> enrollmentDoneStream = statusChangeObservable
                .filter(eventStatus -> formViewArguments.type() == FormViewArguments.Type.ENROLLMENT)
                .map(reportStatus -> formViewArguments.uid())
                .observeOn(schedulerProvider.io()).share();

        compositeDisposable.add(enrollmentDoneStream
                .flatMap(formRepository::autoGenerateEvents) //Autogeneration of events
                .flatMap(data -> formRepository.useFirstStageDuringRegistration()) //Checks if first Stage Should be used
                .subscribeOn(schedulerProvider.ui())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view.finishEnrollment(),
                        throwable -> {
                            throw new OnErrorNotImplementedException(throwable);
                        }));

        compositeDisposable.add(statusChangeObservable.connect());
    }

    public void initializeSaveObservable() {
        ConnectableObservable<EnrollmentStatus> statusChangeObservable = view.onObservableBackPressed()
                .publish();

        compositeDisposable.add(statusChangeObservable
                .filter(eventStatus -> formViewArguments.type() != FormViewArguments.Type.ENROLLMENT)
                .subscribeOn(schedulerProvider.ui())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> checkMandatoryFields(), Timber::e));

        Observable<String> enrollmentDoneStream = statusChangeObservable
                .filter(eventStatus -> formViewArguments.type() == FormViewArguments.Type.ENROLLMENT)
                .map(reportStatus -> formViewArguments.uid())
                .observeOn(schedulerProvider.io()).share();

        compositeDisposable.add(enrollmentDoneStream
                .subscribeOn(schedulerProvider.ui())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        o -> checkMandatoryFields()
                        , Timber::e));

        compositeDisposable.add(statusChangeObservable.connect());
    }

    private List<FieldViewModel> applyFieldViewEffects(
            @NonNull List<FieldViewModel> viewModels,
            @NonNull Result<RuleEffect> calcResult) {
        if (calcResult.error() != null) {
            Timber.e(calcResult.error());
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
            } else if (ruleAction instanceof RuleActionSetMandatoryField) {
                RuleActionSetMandatoryField mandatoryField = (RuleActionSetMandatoryField) ruleAction;
                FieldViewModel model = fieldViewModels.get(mandatoryField.field());
                if (model != null)
                    fieldViewModels.put(mandatoryField.field(), model.setMandatory());
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
            Timber.e(calcResult.error());
            return viewModels;
        }

        Map<String, FormSectionViewModel> fieldViewModels = toMap(viewModels);
        applyRuleEffects(fieldViewModels, calcResult);

        return new ArrayList<>(fieldViewModels.values());
    }

    private void applyRuleEffects(Map<String, FormSectionViewModel> fieldViewModels, Result<RuleEffect> calcResult) {
        //TODO: APPLY RULE EFFECTS TO ALL MODELS
        view.setErrorOnCompletion(null);
        view.setWarningOnCompletion(null);
        view.setShowError(null);
        for (RuleEffect ruleEffect : calcResult.items()) {
            RuleAction ruleAction = ruleEffect.ruleAction();

            if (ruleAction instanceof RuleActionHideSection) {
                RuleActionHideSection hideSection = (RuleActionHideSection) ruleAction;
                fieldViewModels.remove(hideSection.programStageSection());
            } else if (ruleAction instanceof RuleActionWarningOnCompletion) {
                RuleActionWarningOnCompletion warningOnCompletion = (RuleActionWarningOnCompletion) ruleAction;
                view.setWarningOnCompletion(warningOnCompletion);
                view.messageOnComplete(warningOnCompletion.content(), true);
            } else if (ruleAction instanceof RuleActionErrorOnCompletion) {
                RuleActionErrorOnCompletion errorOnCompletion = (RuleActionErrorOnCompletion) ruleAction;
                view.setErrorOnCompletion(errorOnCompletion);
            } else if (ruleAction instanceof RuleActionShowError) {
                RuleActionShowError showError = (RuleActionShowError) ruleAction;
                view.setShowError(showError);
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
                    .subscribeOn(schedulerProvider.computation()).onErrorReturn(throwable -> Result.failure(new Exception(throwable)));

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
                .subscribeOn(schedulerProvider.computation()).onErrorReturn(throwable -> Result.failure(new Exception(throwable)));

        Observable<List<FieldViewModel>> fieldValues = Observable.zip(
                values, ruleEffect, this::applyFieldViewEffects);

        CompositeDisposable disposable = new CompositeDisposable();
        disposable.add(fieldValues
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(data -> {
                    view.isMandatoryFieldsRequired(data);
                    disposable.clear();
                }, t -> {
                    Timber.e(t);
                    view.isMandatoryFieldsRequired(new ArrayList<>());
                })
        );
    }

    private void deleteTrackedEntityAttributeValues(@NonNull String trackedEntityAttributeInstanceId) {
        formRepository.deleteTrackedEntityAttributeValues(trackedEntityAttributeInstanceId);
    }

    private void deleteEnrollment(@NonNull String trackedEntityAttributeInstanceId) {
        formRepository.deleteEnrollment(trackedEntityAttributeInstanceId);
    }

    private void deleteEvent() {
        formRepository.deleteEvent();
    }

    private void deleteTrackedEntityInstance(@NonNull String trackedEntityAttributeInstanceId) {
        formRepository.deleteTrackedEntityInstance(trackedEntityAttributeInstanceId);
    }

    public void deleteCascade() {
        CompositeDisposable disposable = new CompositeDisposable();
        if (isEvent) {
            deleteEvent();
            disposable.clear();
            view.onAllSavedDataDeleted();
        } else {
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

    @Override
    public void saveCategoryOption(CategoryOptionCombo selectedOption) {
        formRepository.saveCategoryOption(selectedOption);
    }

    public void getNeedInitial(String eventUid) {
        compositeDisposable.add(
                Flowable.zip(
                        formRepository.getProgramStage(eventUid),
                        formRepository.getProgramCategoryCombo(eventUid).toFlowable(BackpressureStrategy.LATEST),
                        Pair::create
                )
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                pair -> {
                                    ProgramStage programStage = pair.val0();
                                    Trio<Boolean, CategoryCombo, List<CategoryOptionCombo>> trio = pair.val1();
                                    view.setNeedInitial(programStage.featureType().equals(FeatureType.POINT) || !trio.val1().isDefault(), programStage.uid());
                                },
                                Timber::e
                        )
        );
    }
}