package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.databinding.ObservableField;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.ValueStore;
import org.dhis2.data.forms.dataentry.fields.display.DisplayViewModel;
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel;
import org.dhis2.commons.prefs.Preference;
import org.dhis2.commons.prefs.PreferenceProvider;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Quartet;
import org.dhis2.form.model.ActionType;
import org.dhis2.form.model.FieldUiModel;
import org.dhis2.form.model.RowAction;
import org.dhis2.utils.AuthorityException;
import org.dhis2.utils.DhisTextUtils;
import org.dhis2.utils.Result;
import org.dhis2.utils.RuleUtilsProviderResult;
import org.dhis2.utils.RulesUtilsProvider;
import org.dhis2.utils.RulesUtilsProviderConfigurationError;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.rules.models.RuleEffect;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.BehaviorSubject;
import kotlin.Pair;
import timber.log.Timber;

@Singleton
public class EventCapturePresenterImpl implements EventCaptureContract.Presenter {

    private final EventCaptureContract.EventCaptureRepository eventCaptureRepository;
    private final RulesUtilsProvider rulesUtils;
    private final String eventUid;
    private final PublishProcessor<Unit> progressProcessor;
    private final PublishProcessor<Unit> sectionAdjustProcessor;
    private final PublishProcessor<Unit> formAdjustProcessor;
    private final SchedulerProvider schedulerProvider;
    private final ValueStore valueStore;
    private final EventFieldMapper fieldMapper;
    public CompositeDisposable compositeDisposable;
    private EventCaptureContract.View view;
    private ObservableField<String> currentSection;
    private FlowableProcessor<Boolean> showCalculationProcessor;
    private Map<String, FieldUiModel> emptyMandatoryFields;
    //Rules data
    private boolean canComplete;
    private String completeMessage;
    private Map<String, String> errors;
    private Map<String, String> warnings;
    private EventStatus eventStatus;
    private boolean hasExpired;
    private final Flowable<String> sectionProcessor;
    private ConnectableFlowable<List<FieldUiModel>> fieldFlowable;
    private PublishProcessor<Unit> notesCounterProcessor;
    private BehaviorSubject<List<FieldUiModel>> formFieldsProcessor;
    private boolean assignedValueChanged;
    private int calculationLoop = 0;
    private final int MAX_LOOP_CALCULATIONS = 5;
    private PreferenceProvider preferences;
    private Pair<Boolean, Boolean> showErrors;
    private FlowableProcessor<RowAction> onFieldActionProcessor;
    private List<RulesUtilsProviderConfigurationError> configurationError;
    private boolean showConfigurationError = true;
    private boolean showLoopWarning = true;

    public EventCapturePresenterImpl(EventCaptureContract.View view, String eventUid,
                                     EventCaptureContract.EventCaptureRepository eventCaptureRepository,
                                     RulesUtilsProvider rulesUtils,
                                     ValueStore valueStore, SchedulerProvider schedulerProvider,
                                     PreferenceProvider preferences,
                                     GetNextVisibleSection getNextVisibleSection,
                                     EventFieldMapper fieldMapper,
                                     FlowableProcessor<RowAction> onFieldActionProcessor,
                                     Flowable<String> sectionProcessor
    ) {
        this.view = view;
        this.eventUid = eventUid;
        this.eventCaptureRepository = eventCaptureRepository;
        this.rulesUtils = rulesUtils;
        this.valueStore = valueStore;
        this.schedulerProvider = schedulerProvider;
        this.currentSection = new ObservableField<>("");
        this.errors = new HashMap<>();
        this.warnings = new HashMap<>();
        this.emptyMandatoryFields = new HashMap<>();
        this.canComplete = true;
        this.compositeDisposable = new CompositeDisposable();
        this.preferences = preferences;
        this.showErrors = new Pair<>(false, false);
        this.fieldMapper = fieldMapper;
        this.onFieldActionProcessor = onFieldActionProcessor;

        this.sectionProcessor = sectionProcessor;
        showCalculationProcessor = PublishProcessor.create();
        progressProcessor = PublishProcessor.create();
        sectionAdjustProcessor = PublishProcessor.create();
        formAdjustProcessor = PublishProcessor.create();
        notesCounterProcessor = PublishProcessor.create();
        formFieldsProcessor = BehaviorSubject.createDefault(new ArrayList<>());
    }

    @Override
    public void init() {

        compositeDisposable.add(
                eventCaptureRepository.eventIntegrityCheck()
                        .filter(check -> !check)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                checkDidNotPass -> view.showEventIntegrityAlert(),
                                Timber::e
                        )
        );

        compositeDisposable.add(
                Flowable.zip(
                        eventCaptureRepository.programStageName(),
                        eventCaptureRepository.eventDate(),
                        eventCaptureRepository.orgUnit(),
                        eventCaptureRepository.catOption(),
                        Quartet::create
                )
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                data -> {
                                    preferences.setValue(Preference.CURRENT_ORG_UNIT, data.val2().uid());
                                    view.renderInitialInfo(data.val0(), data.val1(), data.val2().displayName(), data.val3());
                                },
                                Timber::e
                        )

        );

        compositeDisposable.add(
                eventCaptureRepository.programStage()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view::setProgramStage,
                                Timber::e
                        )
        );


        compositeDisposable.add(
                eventCaptureRepository.eventStatus()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                data -> {
                                    this.eventStatus = data;
                                    checkExpiration();
                                },
                                Timber::e
                        )
        );

        compositeDisposable.add(
                Flowable.zip(
                        sectionAdjustProcessor.onBackpressureBuffer(),
                        formAdjustProcessor.onBackpressureBuffer(),
                        (a, b) -> new Unit())
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.io())
                        .subscribe(
                                unit -> {
                                    showCalculationProcessor.onNext(false);
                                    progressProcessor.onNext(new Unit());
                                },
                                Timber::e
                        )
        );

        fieldFlowable = getFieldFlowable();

        compositeDisposable.add(
                eventCaptureRepository.eventSections()
                        .flatMap(sectionList ->
                                sectionProcessor.startWith(sectionList.get(0).sectionUid())
                                        .observeOn(schedulerProvider.io())
                                        .switchMap(section ->
                                                fieldFlowable.map(fields ->
                                                        fieldMapper.map(
                                                                fields,
                                                                sectionList,
                                                                section,
                                                                errors,
                                                                warnings,
                                                                emptyMandatoryFields,
                                                                showErrors
                                                        ))))
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(sectionsAndFields -> {
                                    if (showErrors.component1() || showErrors.component2()) {
                                        qualityCheck();
                                    }
                                    if (assignedValueChanged && errors.isEmpty() && calculationLoop < MAX_LOOP_CALCULATIONS) {
                                        calculationLoop++;
                                        nextCalculation(true);
                                    } else {
                                        if (calculationLoop == 5 && showLoopWarning) {
                                            view.showLoopWarning();
                                            showLoopWarning = false;
                                        }
                                        calculationLoop = 0;
                                        formFieldsProcessor.onNext(sectionsAndFields.component2());
                                        formAdjustProcessor.onNext(new Unit());

                                        view.updatePercentage(
                                                fieldMapper.completedFieldsPercentage()
                                        );

                                        if (!configurationError.isEmpty() && showConfigurationError) {
                                            view.displayConfigurationErrors(configurationError);
                                        }
                                    }
                                },
                                Timber::e
                        ));

        compositeDisposable.add(
                sectionProcessor
                        .observeOn(schedulerProvider.io())
                        .subscribeOn(schedulerProvider.ui())
                        .subscribe(
                                data -> {
                                    view.showProgress();
                                    currentSection.set(data);
                                    showCalculationProcessor.onNext(true);
                                },
                                Timber::e
                        )
        );

        fieldFlowable.connect();

        compositeDisposable.add(
                onFieldActionProcessor.subscribe(
                        rowAction -> {
                            if (rowAction.getType() == ActionType.ON_FOCUS) {
                                view.hideNavigationBar();
                            }
                        },
                        Timber::e
                )
        );
    }

    @VisibleForTesting
    public String getFieldSection(FieldUiModel fieldViewModel) {
        String fieldSection;
        if (fieldViewModel instanceof DisplayViewModel) {
            fieldSection = "display";
        } else {
            fieldSection = fieldViewModel.getProgramStageSection() != null ?
                    fieldViewModel.getProgramStageSection() :
                    "";
        }
        return fieldSection;
    }

    @Override
    public BehaviorSubject<List<FieldUiModel>> formFieldsFlowable() {
        return formFieldsProcessor;
    }

    private ConnectableFlowable<List<FieldUiModel>> getFieldFlowable() {
        return showCalculationProcessor
                .startWith(true)
                .filter(newCalculation -> newCalculation)
                .observeOn(schedulerProvider.io())
                .switchMap(newCalculation -> Flowable.zip(
                        eventCaptureRepository.list(onFieldActionProcessor),
                        eventCaptureRepository.calculate(),
                        this::applyEffects)
                ).map(fields ->
                        {
                            emptyMandatoryFields = new HashMap<>();
                            for (FieldUiModel fieldViewModel : fields) {
                                if (fieldViewModel.getMandatory() && DhisTextUtils.Companion.isEmpty(fieldViewModel.getValue())) {
                                    emptyMandatoryFields.put(fieldViewModel.getUid(), fieldViewModel);
                                }
                            }
                            if (!fields.isEmpty()) {
                                int lastIndex = fields.size() - 1;
                                FieldUiModel field = fields.get(lastIndex);
                                if (field instanceof EditTextViewModel &&
                                        ((EditTextViewModel) field).valueType() != ValueType.LONG_TEXT
                                ) {
                                    fields.set(lastIndex, ((EditTextViewModel) field).withKeyBoardActionDone());
                                }
                            }
                            return fields;
                        }
                )
                .publish();

    }

    private void checkExpiration() {
        if (eventStatus == EventStatus.COMPLETED)
            compositeDisposable.add(
                    eventCaptureRepository.isCompletedEventExpired(eventUid)
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .subscribe(
                                    hasExpiredResult -> this.hasExpired = hasExpiredResult && !eventCaptureRepository.isEventEditable(eventUid),
                                    Timber::e
                            )
            );
        else
            this.hasExpired = !eventCaptureRepository.isEventEditable(eventUid);
    }

    @Override
    public void onBackClick() {
        view.goBack();
    }

    @Override
    public void nextCalculation(boolean doNextCalculation) {
        showCalculationProcessor.onNext(doNextCalculation);
    }

    @NonNull
    private synchronized List<FieldUiModel> applyEffects(
            @NonNull List<FieldUiModel> viewModels,
            @NonNull Result<RuleEffect> calcResult) {

        if (calcResult.error() != null) {
            Timber.e(calcResult.error());
            return viewModels;
        }

        Map<String, FieldUiModel> fieldViewModels = toMap(viewModels);
        RuleUtilsProviderResult ruleResults = rulesUtils.applyRuleEffects(
                true,
                fieldViewModels,
                calcResult,
                valueStore);

        assignedValueChanged = !ruleResults.getFieldsToUpdate().isEmpty();
        for (String fieldUid : ruleResults.getFieldsToUpdate()) {
            setValueChanged(fieldUid);
        }

        errors = ruleResults.errorMap();
        warnings = ruleResults.warningMap();
        configurationError = ruleResults.getConfigurationErrors();
        canComplete = ruleResults.getCanComplete();
        completeMessage = ruleResults.getMessageOnComplete();

        ArrayList<FieldUiModel> fieldList = new ArrayList<>(fieldViewModels.values());
        return fieldList;
    }

    @NonNull
    private static Map<String, FieldUiModel> toMap(@NonNull List<FieldUiModel> fieldViewModels) {
        Map<String, FieldUiModel> map = new LinkedHashMap<>();
        for (FieldUiModel fieldViewModel : fieldViewModels) {
            map.put(fieldViewModel.getUid(), fieldViewModel);
        }
        return map;
    }

    @Override
    public void attemptFinish() {

        qualityCheck();

        if (!errors.isEmpty() && errors.get(currentSection.get()) != null) {
            view.showErrorSnackBar();
        }

        if (eventStatus != EventStatus.ACTIVE) {
            setUpActionByStatus(eventStatus);
        } else {
            view.showCompleteActions(canComplete && eventCaptureRepository.isEnrollmentOpen(), completeMessage, errors, emptyMandatoryFields);
        }

        view.showNavigationBar();
    }

    private void setUpActionByStatus(EventStatus eventStatus) {
        switch (eventStatus) {
            case COMPLETED:
                if (!hasExpired && !eventCaptureRepository.isEnrollmentCancelled())
                    view.attemptToReopen();
                else
                    view.finishDataEntry();
                break;
            case OVERDUE:
                view.attemptToSkip();
                break;
            case SKIPPED:
                view.attemptToReschedule();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean isEnrollmentOpen() {
        return eventCaptureRepository.isEnrollmentOpen();
    }

    @Override
    public void goToSection() {

    }

    @Override
    public void completeEvent(boolean addNew) {
        compositeDisposable.add(
                eventCaptureRepository.completeEvent()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                success -> {
                                    if (addNew)
                                        view.restartDataEntry();
                                    else {
                                        preferences.setValue(Preference.PREF_COMPLETED_EVENT, eventUid);
                                        view.finishDataEntry();
                                    }
                                },
                                Timber::e
                        ));
    }

    @Override
    public void reopenEvent() {
        compositeDisposable.add(
                eventCaptureRepository.canReOpenEvent()
                        .flatMap(canReOpen -> {
                            if (canReOpen)
                                return Single.just(true);
                            else
                                return Single.error(new AuthorityException(view.getContext().getString(R.string.uncomplete_authority_error)));
                        })
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(canReOpenEvent -> {
                                    if (canReOpenEvent) {
                                        if (eventCaptureRepository.reopenEvent()) {
                                            view.showSnackBar(R.string.event_reopened);
                                            eventStatus = EventStatus.ACTIVE;
                                        }
                                    }
                                },
                                error -> {
                                    if (error instanceof AuthorityException)
                                        view.displayMessage(error.getMessage());
                                    else
                                        Timber.e(error);
                                }
                        ));
    }

    @Override
    public void deleteEvent() {
        compositeDisposable.add(eventCaptureRepository.deleteEvent()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        result -> {
                            if (result)
                                view.showSnackBar(R.string.event_was_deleted);
                        },
                        Timber::e,
                        () -> view.finishDataEntry()
                )
        );
    }

    @Override
    public void skipEvent() {
        compositeDisposable.add(eventCaptureRepository.updateEventStatus(EventStatus.SKIPPED)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        result -> view.showSnackBar(R.string.event_was_skipped),
                        Timber::e,
                        () -> view.finishDataEntry()
                )
        );
    }

    @Override
    public void rescheduleEvent(Date time) {
        compositeDisposable.add(
                eventCaptureRepository.rescheduleEvent(time)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                result -> view.finishDataEntry(),
                                Timber::e
                        )
        );
    }

    @Override
    public boolean canWrite() {
        return eventCaptureRepository.getAccessDataWrite();
    }

    @Override
    public boolean hasExpired() {
        return hasExpired;
    }

    @Override
    public void onDettach() {
        this.compositeDisposable.clear();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }

    @SuppressLint("CheckResult")
    @Override
    public void saveImage(String uuid, String filePath) {
        valueStore.save(uuid, filePath).blockingFirst();
        setValueChanged(uuid);
    }

    @Override
    public void initNoteCounter() {
        if (!notesCounterProcessor.hasSubscribers()) {
            compositeDisposable.add(
                    notesCounterProcessor.startWith(new Unit())
                            .flatMapSingle(unit ->
                                    eventCaptureRepository.getNoteCount())
                            .subscribeOn(schedulerProvider.io())
                            .observeOn(schedulerProvider.ui())
                            .subscribe(
                                    numberOfNotes ->
                                            view.updateNoteBadge(numberOfNotes),
                                    Timber::e
                            )
            );
        } else {
            notesCounterProcessor.onNext(new Unit());
        }
    }

    @Override
    public void refreshTabCounters() {
        initNoteCounter();
    }

    @Override
    public void hideProgress() {
        view.hideProgress();
    }

    @Override
    public void showProgress() {
        view.showProgress();
    }

    private void qualityCheck() {
        Pair<Boolean, Boolean> currentShowError = showErrors;
        showErrors = new Pair<>(!emptyMandatoryFields.isEmpty() || !warnings.isEmpty(), !errors.isEmpty());
        showCalculationProcessor.onNext(
                currentShowError.getFirst() != showErrors.getFirst() ||
                        currentShowError.getSecond() != showErrors.getSecond()
        );
    }

    @Override
    public boolean getCompletionPercentageVisibility() {
        return eventCaptureRepository.showCompletionPercentage();
    }

    @Override
    public void setValueChanged(@NotNull String uid) {
        eventCaptureRepository.updateFieldValue(uid);
    }

    @Override
    public void disableConfErrorMessage() {
        showConfigurationError = false;
    }

    public CompositeDisposable getDisposable() {
        return compositeDisposable;
    }
}
