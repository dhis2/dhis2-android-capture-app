package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.annotation.SuppressLint;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.databinding.ObservableField;

import org.dhis2.R;
import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.StoreResult;
import org.dhis2.data.forms.dataentry.ValueStore;
import org.dhis2.data.forms.dataentry.ValueStoreImpl;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.display.DisplayViewModel;
import org.dhis2.data.forms.dataentry.fields.image.ImageViewModel;
import org.dhis2.data.forms.dataentry.fields.optionset.OptionSetViewModel;
import org.dhis2.data.forms.dataentry.fields.section.SectionViewModel;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;
import org.dhis2.data.forms.dataentry.fields.unsupported.UnsupportedViewModel;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Quartet;
import org.dhis2.utils.AuthorityException;
import org.dhis2.utils.DhisTextUtils;
import org.dhis2.utils.Result;
import org.dhis2.utils.RulesActionCallbacks;
import org.dhis2.utils.RulesUtilsProvider;
import org.hisp.dhis.android.core.common.Unit;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.rules.models.RuleActionShowError;
import org.hisp.dhis.rules.models.RuleEffect;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

@Singleton
public class EventCapturePresenterImpl implements EventCaptureContract.Presenter, RulesActionCallbacks {

    private final EventCaptureContract.EventCaptureRepository eventCaptureRepository;
    private final RulesUtilsProvider rulesUtils;
    private final String eventUid;
    private final PublishProcessor<Unit> progressProcessor;
    private final PublishProcessor<Unit> sectionAdjustProcessor;
    private final PublishProcessor<Unit> formAdjustProcessor;
    private final SchedulerProvider schedulerProvider;
    private final ValueStore valueStore;
    private CompositeDisposable compositeDisposable;
    private EventCaptureContract.View view;
    private int currentPosition;
    private ObservableField<String> currentSection;
    private FlowableProcessor<Integer> currentSectionPosition;
    private FlowableProcessor<Boolean> showCalculationProcessor;
    private List<FormSectionViewModel> sectionList;
    private Map<String, FieldViewModel> emptyMandatoryFields;
    //Rules data
    private List<String> sectionsToHide;
    private Map<String, List<String>> optionsToHide = new HashMap<>();
    private Map<String, List<String>> optionsGroupsToHide = new HashMap<>();
    private Map<String, List<String>> optionsGroupToShow = new HashMap<>();
    private boolean canComplete;
    private String completeMessage;
    private Map<String, String> errors;
    private EventStatus eventStatus;
    private boolean hasExpired;
    private final FlowableProcessor<String> sectionProcessor;
    private int unsupportedFields;
    private int totalFields;
    private ConnectableFlowable<List<FieldViewModel>> fieldFlowable;
    private PublishProcessor<Unit> notesCounterProcessor;
    private BehaviorSubject<List<FieldViewModel>> formFieldsProcessor;
    private boolean assignedValueChanged;
    private int calculationLoop = 0;
    private final int MAX_LOOP_CALCULATIONS = 5;


    public EventCapturePresenterImpl(EventCaptureContract.View view, String eventUid,
                                     EventCaptureContract.EventCaptureRepository eventCaptureRepository,
                                     RulesUtilsProvider rulesUtils,
                                     ValueStore valueStore, SchedulerProvider schedulerProvider) {
        this.view = view;
        this.eventUid = eventUid;
        this.eventCaptureRepository = eventCaptureRepository;
        this.rulesUtils = rulesUtils;
        this.valueStore = valueStore;
        this.schedulerProvider = schedulerProvider;
        this.currentPosition = 0;
        this.sectionsToHide = new ArrayList<>();
        this.currentSection = new ObservableField<>("");
        this.errors = new HashMap<>();
        this.emptyMandatoryFields = new HashMap<>();
        this.canComplete = true;
        this.sectionList = new ArrayList<>();
        this.compositeDisposable = new CompositeDisposable();

        currentSectionPosition = PublishProcessor.create();
        sectionProcessor = PublishProcessor.create();
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
                                data -> view.renderInitialInfo(data.val0(), data.val1(), data.val2(), data.val3()),
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
                eventCaptureRepository.eventSections()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                data -> {
                                    this.sectionList = data;
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
                                        .switchMap(section -> fieldFlowable
                                                .map(fields -> {
                                                    totalFields = 0;
                                                    unsupportedFields = 0;
                                                    HashMap<String, List<FieldViewModel>> fieldMap = new HashMap<>();
                                                    List<String> optionSets = new ArrayList<>();
                                                    for (FieldViewModel fieldViewModel : fields) {
                                                        String fieldSection = getFieldSection(fieldViewModel);
                                                        if(!fieldSection.isEmpty() || sectionList.size() == 1) {
                                                            if (!fieldMap.containsKey(fieldSection)) {
                                                                fieldMap.put(fieldSection, new ArrayList<>());
                                                            }
                                                            fieldMap.get(fieldSection).add(fieldViewModel);

                                                            if (fieldViewModel.optionSet() == null || !(fieldViewModel instanceof ImageViewModel)) {
                                                                totalFields++;
                                                            } else if (!optionSets.contains(fieldViewModel.optionSet())) {
                                                                optionSets.add(fieldViewModel.optionSet());
                                                                totalFields++;
                                                            }
                                                            if (fieldViewModel instanceof UnsupportedViewModel || fieldViewModel instanceof DisplayViewModel)
                                                                unsupportedFields++;
                                                        }
                                                    }

                                                    List<EventSectionModel> eventSectionModels = new ArrayList<>();
                                                    List<FieldViewModel> finalFieldList = new ArrayList<>();

                                                    for (FormSectionViewModel sectionModel : sectionList) {
                                                        if (sectionList.size() >= 1 && !sectionModel.sectionUid().isEmpty() && !sectionsToHide.contains(sectionModel.sectionUid())) {
                                                            List<FieldViewModel> fieldViewModels = new ArrayList<>();
                                                            if (fieldMap.get(sectionModel.sectionUid()) != null)
                                                                fieldViewModels.addAll(fieldMap.get(sectionModel.sectionUid()));

                                                            int cont = 0;

                                                            HashMap<String, Boolean> finalFields = new HashMap<>();
                                                            for (FieldViewModel fieldViewModel : fieldViewModels) {
                                                                finalFields.put(fieldViewModel.optionSet() == null || !(fieldViewModel instanceof ImageViewModel) ?
                                                                                fieldViewModel.uid() :
                                                                                fieldViewModel.optionSet(),
                                                                        !DhisTextUtils.Companion.isEmpty(fieldViewModel.value()));
                                                            }
                                                            for (String key : finalFields.keySet())
                                                                if (finalFields.get(key))
                                                                    cont++;

                                                            eventSectionModels.add(EventSectionModel.create(sectionModel.label(), sectionModel.sectionUid(), cont, finalFields.keySet().size()));

                                                            boolean isOpen = sectionModel.sectionUid().equals(section);
                                                            finalFieldList.add(
                                                                    SectionViewModel.create(
                                                                            sectionModel.sectionUid(),
                                                                            sectionModel.label(),
                                                                            "",
                                                                            isOpen,
                                                                            finalFields.keySet().size(),
                                                                            cont,
                                                                            sectionModel.renderType()
                                                                    ));
                                                            if (isOpen && fieldMap.get(sectionModel.sectionUid()) != null) {
                                                                finalFieldList.addAll(fieldMap.get(sectionModel.sectionUid()));
                                                            }

                                                        } else if (sectionList.size() == 1 && sectionModel.sectionUid().isEmpty()) {
                                                            int cont = 0;
                                                            HashMap<String, Boolean> finalFields = new HashMap<>();
                                                            for (FieldViewModel fieldViewModel : fields) {
                                                                if (!(fieldViewModel instanceof DisplayViewModel)) {
                                                                    finalFields.put(fieldViewModel.optionSet() == null || !(fieldViewModel instanceof ImageViewModel) ?
                                                                                    fieldViewModel.uid() :
                                                                                    fieldViewModel.optionSet(),
                                                                            !DhisTextUtils.Companion.isEmpty(fieldViewModel.value()));
                                                                }
                                                            }
                                                            for (String key : finalFields.keySet())
                                                                if (finalFields.get(key))
                                                                    cont++;

                                                            eventSectionModels.add(EventSectionModel.create("NO_SECTION", "no_section", cont, finalFields.keySet().size()));
                                                            finalFieldList.addAll(fieldMap.get(sectionModel.sectionUid()));
                                                        }
                                                    }

                                                    if(!eventSectionModels.get(0).sectionName().equals("NO_SECTION")) {
                                                        finalFieldList.add(SectionViewModel.createClosingSection());
                                                    }

                                                    if (fieldMap.containsKey("display") && fieldMap.get("display") != null) {
                                                        finalFieldList.addAll(fieldMap.get("display"));
                                                    }

                                                    return Pair.create(eventSectionModels, finalFieldList);
                                                })))
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(sectionsAndFields -> {
                                    if (assignedValueChanged && errors.isEmpty() && calculationLoop < MAX_LOOP_CALCULATIONS) {
                                        calculationLoop++;
                                        nextCalculation(true);
                                    } else {
                                        if (calculationLoop == 5) {
                                            view.showLoopWarning();
                                        }
                                        calculationLoop = 0;
                                        formFieldsProcessor.onNext(sectionsAndFields.val1());
                                        formAdjustProcessor.onNext(new Unit());
                                        int completedFields = 0;
                                        for (EventSectionModel sectionModel : sectionsAndFields.val0()) {
                                            completedFields += sectionModel.numberOfCompletedFields();
                                        }
                                        view.updatePercentage(
                                                calculateCompletionPercentage(completedFields, totalFields),
                                                calculateCompletionPercentage(unsupportedFields, totalFields));
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
                                    currentSection.set(data);
                                    showCalculationProcessor.onNext(true);
                                },
                                Timber::e
                        )
        );

        fieldFlowable.connect();
    }

    @VisibleForTesting
    public String getFieldSection(FieldViewModel fieldViewModel) {
        String fieldSection;
        if (fieldViewModel instanceof DisplayViewModel) {
            fieldSection = "display";
        } else {
            fieldSection = fieldViewModel.programStageSection() != null ?
                    fieldViewModel.programStageSection() :
                    "";
        }
        return fieldSection;
    }

    private float calculateCompletionPercentage(int completedFields, int totals) {
        if (totals == 0) {
            return 100;
        }
        return (float) completedFields / (float) totals;
    }

    @Override
    public BehaviorSubject<List<FieldViewModel>> formFieldsFlowable() {
        return formFieldsProcessor;
    }

    private ConnectableFlowable<List<FieldViewModel>> getFieldFlowable() {
        return showCalculationProcessor
                .startWith(true)
                .filter(newCalculation -> newCalculation)
                .flatMap(newCalculation -> Flowable.zip(
                        eventCaptureRepository.list(),
                        eventCaptureRepository.calculate(),
                        this::applyEffects)
                ).map(fields ->
                        {
                            emptyMandatoryFields = new HashMap<>();
                            for (FieldViewModel fieldViewModel : fields) {
                                if (fieldViewModel.mandatory() && DhisTextUtils.Companion.isEmpty(fieldViewModel.value()) && !sectionsToHide.contains(fieldViewModel.programStageSection()))
                                    emptyMandatoryFields.put(fieldViewModel.uid(), fieldViewModel);
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
    private synchronized List<FieldViewModel> applyEffects(
            @NonNull List<FieldViewModel> viewModels,
            @NonNull Result<RuleEffect> calcResult) {

        if (calcResult.error() != null) {
            Timber.e(calcResult.error());
            return viewModels;
        }

        //Reset effectsT
        assignedValueChanged = false;
        optionsToHide.clear();
        optionsGroupsToHide.clear();
        optionsGroupToShow.clear();
        sectionsToHide.clear();
        errors.clear();
        completeMessage = null;
        canComplete = true;

        Map<String, FieldViewModel> fieldViewModels = toMap(viewModels);
        rulesUtils.applyRuleEffects(fieldViewModels, calcResult, this);

        //Set/remove for HIDEOPTION/HIDEOPTIONGROUP/SHOWOPTIONGROUP
        Iterator<FieldViewModel> fieldIterator = fieldViewModels.values().iterator();
        while (fieldIterator.hasNext()) {
            FieldViewModel field = fieldIterator.next();
            if (field instanceof ImageViewModel) {
                ImageViewModel imageField = (ImageViewModel) field;
                if (optionsToHide.containsKey(imageField.fieldUid()) && optionsToHide.get(imageField.fieldUid()).contains(imageField.optionUid())) {
                    fieldIterator.remove();
                } else if (optionsGroupToShow.containsKey(imageField.fieldUid()) &&
                        !eventCaptureRepository.getOptionsFromGroups(optionsGroupToShow.get(imageField.fieldUid())).contains(imageField.optionUid())) {
                    fieldIterator.remove();
                }
            } else if (field instanceof SpinnerViewModel) {
                ((SpinnerViewModel) field).setOptionsToHide(
                        optionsToHide.get(field.uid()) != null ? optionsToHide.get(field.uid()) : new ArrayList<>(),
                        optionsGroupsToHide.get(field.uid()) != null ? optionsGroupsToHide.get(field.uid()) : new ArrayList<>()
                );
                if (optionsGroupToShow.keySet().contains(field.uid())) {
                    ((SpinnerViewModel) field).setOptionGroupsToShow(
                            optionsGroupToShow.get(field.uid()) != null ? optionsGroupToShow.get(field.uid()) : new ArrayList<>()
                    );
                }
            } else if (field instanceof OptionSetViewModel) {
                ((OptionSetViewModel) field).setOptionsToHide(
                        optionsToHide.get(field.uid()) != null ? optionsToHide.get(field.uid()) : new ArrayList<>()
                );
                if (optionsGroupToShow.keySet().contains(field.uid())) {
                    ((OptionSetViewModel) field).setOptionsToShow(
                            eventCaptureRepository.getOptionsFromGroups(
                                    optionsGroupToShow.get(field.uid()) != null ? optionsGroupToShow.get(field.uid()) : new ArrayList<>()
                            )
                    );
                }
            }
        }

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

    @Override
    public void onNextSection() {

        view.clearFocus();

        new Handler().postDelayed(
                this::changeSection,
                1000);

    }

    private void changeSection() {


        List<FormSectionViewModel> finalSections = getFinalSections();

        if (currentPosition < finalSections.size() - 1) {
            currentSectionPosition.onNext(++currentPosition);
        } else {

        }
    }

    @Override
    public void attempFinish() {

        if (!errors.isEmpty() && errors.get(currentSection.get()) != null) {
            view.showErrorSnackBar();
        }

        if (eventStatus != EventStatus.ACTIVE) {
            setUpActionByStatus(eventStatus);
        } else {
            view.showCompleteActions(canComplete && eventCaptureRepository.isEnrollmentOpen(), completeMessage, errors, emptyMandatoryFields);
        }
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
            case SCHEDULE:
                break;
            default:
                break;
        }
    }

    @Override
    public void onPreviousSection() {
        if (currentPosition != 0) {
            currentSectionPosition.onNext(--currentPosition);
        }
    }

    private List<FormSectionViewModel> getFinalSections() {
        List<FormSectionViewModel> finalSections = new ArrayList<>();
        for (FormSectionViewModel section : sectionList)
            if (!sectionsToHide.contains(section.sectionUid()))
                finalSections.add(section);
        return finalSections;
    }

    @Override
    public boolean isEnrollmentOpen() {
        return eventCaptureRepository.isEnrollmentOpen();
    }

    @Override
    public void goToSection(String sectionUid) {
        sectionProcessor.onNext(sectionUid);
    }

    @Override
    public void goToSection() {
        String sectionUid = errors.entrySet().iterator().next().getKey();
        for (FormSectionViewModel sectionModel : getFinalSections())
            if (sectionModel.sectionUid() != null && sectionModel.sectionUid().equals(sectionUid))
                currentPosition = getFinalSections().indexOf(sectionModel);
        currentSectionPosition.onNext(currentPosition);
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
                                    else
                                        view.finishDataEntry();
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
                                            currentPosition = 0;
                                            currentSectionPosition.onNext(0);
                                            view.showSnackBar(R.string.event_reopened);
                                            eventStatus = EventStatus.ACTIVE;
                                            goToSection(currentSection.get());
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
    }

    //region ruleActions

    @Override
    public void setCalculatedValue(@NonNull String calculatedValueVariable, @NonNull String value) {

    }

    @Override
    public void setShowError(@NonNull RuleActionShowError showError, @Nullable FieldViewModel model) {
        canComplete = false;
        errors.put(eventCaptureRepository.getSectionFor(showError.field()), showError.field());
        save(showError.field(), null);
    }

    @Override
    public void unsupportedRuleAction() {
        Timber.d(view.getContext().getString(R.string.unsupported_program_rule));
    }

    @SuppressLint("CheckResult")
    @Override
    public void save(@NotNull @NonNull String uid, @Nullable String value) {
        if (value == null || !sectionsToHide.contains(eventCaptureRepository.getSectionFor(uid))) {
            StoreResult result = valueStore.saveWithTypeCheck(uid, value).blockingFirst();
            if (result.component2() == ValueStoreImpl.ValueStoreResult.VALUE_CHANGED) {
                assignedValueChanged = true;
            }
        }
    }

    @Override
    public void setDisplayKeyValue(@NonNull String label, @NonNull String value) {
        //TODO: Implement Indicator tabs to show this field
    }

    @Override
    public void setHideSection(@NonNull String sectionUid) {
        if (!sectionsToHide.contains(sectionUid))
            sectionsToHide.add(sectionUid);
    }

    @Override
    public void setMessageOnComplete(@NonNull String message, boolean canComplete) {
        this.canComplete = canComplete;
        this.completeMessage = message;
    }

    @Override
    public void setHideProgramStage(@NonNull String programStageUid) {
        //do not apply
    }

    @Override
    public void setOptionToHide(@NonNull String optionUid, @NonNull String field) {
        if (!optionsToHide.containsKey(field)) {
            optionsToHide.put(field, new ArrayList<>());
        }
        optionsToHide.get(field).add(optionUid);
        StoreResult result = valueStore.deleteOptionValueIfSelected(field, optionUid);
        if (result.component2() == ValueStoreImpl.ValueStoreResult.VALUE_CHANGED) {
            assignedValueChanged = true;
        }
    }

    @Override
    public void setOptionGroupToHide(@NonNull String optionGroupUid, boolean toHide, @NonNull String field) {
        if (toHide) {
            if (!optionsGroupsToHide.containsKey(field)) {
                optionsGroupsToHide.put(field, new ArrayList<>());
            }
            optionsGroupsToHide.get(field).add(optionGroupUid);
            if (!optionsToHide.containsKey(field)) {
                optionsToHide.put(field, new ArrayList<>());
            }
            optionsToHide.get(field).addAll(eventCaptureRepository.getOptionsFromGroups(Collections.singletonList(optionGroupUid)));
            StoreResult result = valueStore.deleteOptionValueIfSelectedInGroup(field, optionGroupUid, true);
            if (result.component2() == ValueStoreImpl.ValueStoreResult.VALUE_CHANGED) {
                assignedValueChanged = true;
            }
        } else if (!optionsGroupsToHide.containsKey(field) || !optionsGroupsToHide.get(field).contains(optionGroupUid)) {
            if (optionsGroupToShow.get(field) != null) {
                optionsGroupToShow.get(field).add(optionGroupUid);
            } else {
                optionsGroupToShow.put(field, Collections.singletonList(optionGroupUid));
            }
            StoreResult result = valueStore.deleteOptionValueIfSelectedInGroup(field, optionGroupUid, false);
            if (result.component2() == ValueStoreImpl.ValueStoreResult.VALUE_CHANGED) {
                assignedValueChanged = true;
            }
        }
    }

    //endregion

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
    public void setLastUpdatedUid(@NonNull String lastUpdatedUid) {
        eventCaptureRepository.setLastUpdated(lastUpdatedUid);
    }
}
