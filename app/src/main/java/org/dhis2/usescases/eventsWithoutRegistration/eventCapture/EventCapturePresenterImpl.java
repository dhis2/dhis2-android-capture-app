package org.dhis2.usescases.eventsWithoutRegistration.eventCapture;

import android.os.Handler;

import com.google.android.material.snackbar.Snackbar;

import org.dhis2.R;
import org.dhis2.data.forms.FormSectionViewModel;
import org.dhis2.data.forms.dataentry.DataEntryArguments;
import org.dhis2.data.forms.dataentry.DataEntryStore;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.tuples.Quartet;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureFragment.EventCaptureFormFragment;
import org.dhis2.utils.Result;
import org.dhis2.utils.RulesActionCallbacks;
import org.dhis2.utils.RulesUtilsProvider;
import org.dhis2.utils.custom_views.OptionSetDialog;
import org.dhis2.utils.custom_views.OptionSetPopUp;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.rules.models.RuleActionShowError;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 19/11/2018.
 */
public class EventCapturePresenterImpl implements EventCaptureContract.Presenter, RulesActionCallbacks {

    private final EventCaptureContract.EventCaptureRepository eventCaptureRepository;
    private final RulesUtilsProvider rulesUtils;
    private final DataEntryStore dataEntryStore;
    private final MetadataRepository metadataRepository;
    private final String eventUid;
    private CompositeDisposable compositeDisposable;
    private EventCaptureContract.View view;
    private int currentPosition;
    private ObservableField<String> currentSection;
    private FlowableProcessor<Integer> currentSectionPosition;
    private List<FormSectionViewModel> sectionList;
    private Map<String, FieldViewModel> emptyMandatoryFields;
    //Rules data
    private List<String> sectionsToHide;
    private List<String> optionsToHide = new ArrayList<>();
    private List<String> optionsGroupsToHide = new ArrayList<>();
    private boolean canComplete;
    private String completeMessage;
    private Map<String, String> errors;
    private EventStatus eventStatus;
    private boolean hasExpired;
    private boolean snackBarIsShowing;
    private final FlowableProcessor<String> sectionProcessor;
    private boolean isSubscribed;
    private long ruleInitTime;

    public EventCapturePresenterImpl(String eventUid, EventCaptureContract.EventCaptureRepository eventCaptureRepository, MetadataRepository metadataRepository, RulesUtilsProvider rulesUtils, DataEntryStore dataEntryStore) {
        this.eventUid = eventUid;
        this.eventCaptureRepository = eventCaptureRepository;
        this.metadataRepository = metadataRepository;
        this.rulesUtils = rulesUtils;
        this.dataEntryStore = dataEntryStore;
        this.currentPosition = 0;
        this.sectionsToHide = new ArrayList<>();
        this.currentSection = new ObservableField<>("");
        this.errors = new HashMap<>();
        this.emptyMandatoryFields = new HashMap<>();
        this.canComplete = true;
        this.sectionList = new ArrayList<>();
        currentSectionPosition = PublishProcessor.create();
        sectionProcessor = PublishProcessor.create();

    }

    @Override
    public void init(EventCaptureContract.View view) {
        this.compositeDisposable = new CompositeDisposable();
        this.view = view;

        compositeDisposable.add(
                Flowable.zip(
                        eventCaptureRepository.programStageName(),
                        eventCaptureRepository.eventDate(),
                        eventCaptureRepository.orgUnit(),
                        eventCaptureRepository.catOption(),
                        Quartet::create
                )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> view.renderInitialInfo(data.val0(), data.val1(), data.val2(), data.val3()),
                                Timber::e
                        )

        );

        compositeDisposable.add(
                eventCaptureRepository.programStage()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view::setProgramStage,
                                Timber::e
                        )
        );

        compositeDisposable.add(
                eventCaptureRepository.eventStatus()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> {
                                    this.eventStatus = data;
                                    if (eventStatus == EventStatus.COMPLETED)
                                        checkExpiration();
                                },
                                Timber::e
                        )
        );

        compositeDisposable.add(
                eventCaptureRepository.eventSections()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> {
                                    this.sectionList = data;
                                    view.setUp();
                                },
                                Timber::e
                        )
        );

        compositeDisposable.add(
                getFieldFlowable(null)
                        .map(fields -> {
                            HashMap<String, List<FieldViewModel>> fieldMap = new HashMap<>();

                            for (FieldViewModel fieldViewModel : fields) {
                                if (!fieldMap.containsKey(fieldViewModel.programStageSection()))
                                    fieldMap.put(fieldViewModel.programStageSection(), new ArrayList<>());
                                fieldMap.get(fieldViewModel.programStageSection()).add(fieldViewModel);
                            }

                            List<EventSectionModel> eventSectionModels = new ArrayList<>();
                            for (FormSectionViewModel sectionModel : sectionList) {
                                if (sectionList.size() > 1 && !sectionsToHide.contains(sectionModel.sectionUid())) {
                                    List<FieldViewModel> fieldViewModels = new ArrayList<>();
                                    if (fieldMap.get(sectionModel.sectionUid()) != null)
                                        fieldViewModels.addAll(fieldMap.get(sectionModel.sectionUid()));

                                    int cont = 0;

                                    HashMap<String, Boolean> finalFields = new HashMap<>();
                                    for (FieldViewModel fieldViewModel : fieldViewModels) {
                                        finalFields.put(fieldViewModel.optionSet() == null ? fieldViewModel.uid() : fieldViewModel.optionSet(), !isEmpty(fieldViewModel.value()));
                                    }
                                    for (String key : finalFields.keySet())
                                        if (finalFields.get(key))
                                            cont++;

                                    eventSectionModels.add(EventSectionModel.create(sectionModel.label(), sectionModel.sectionUid(), cont, finalFields.keySet().size()));
                                } else if (sectionList.size() == 1) {
                                    int cont = 0;
                                    HashMap<String, Boolean> finalFields = new HashMap<>();
                                    for (FieldViewModel fieldViewModel : fields) {
                                        finalFields.put(fieldViewModel.optionSet() == null ? fieldViewModel.uid() : fieldViewModel.optionSet(), !isEmpty(fieldViewModel.value()));
                                    }
                                    for (String key : finalFields.keySet())
                                        if (finalFields.get(key))
                                            cont++;

                                    eventSectionModels.add(EventSectionModel.create("NO_SECTION", "no_section", cont, finalFields.keySet().size()));
                                }
                            }

                            return eventSectionModels;
                        })
                        .observeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(data -> {
                                    subscribeToSection();
                                    EventCaptureFormFragment.getInstance().setSectionSelector(data);
                                    checkProgress();
                                }
                                ,
                                Timber::e
                        ));

        compositeDisposable.add(
                sectionProcessor
                        .flatMap(section -> getFieldFlowable(section)
                                .map(fields -> {
                                    HashMap<String, List<FieldViewModel>> fieldMap = new HashMap<>();
                                    for (FieldViewModel fieldViewModel : fields) {
                                        if (!fieldMap.containsKey(fieldViewModel.programStageSection()))
                                            fieldMap.put(fieldViewModel.programStageSection(), new ArrayList<>());
                                        fieldMap.get(fieldViewModel.programStageSection()).add(fieldViewModel);
                                    }
                                    if (fieldMap.containsKey(null) && fieldMap.containsKey(section))
                                        for (FieldViewModel fieldViewModel : fieldMap.get(null))
                                            fieldMap.get(section).add(fieldViewModel);

                                    List<FieldViewModel> fieldsToShow = fieldMap.get(section.equals("NO_SECTION") ? null : section);
                                    return fieldsToShow != null ? fieldsToShow : new ArrayList<FieldViewModel>();
                                }))
                        .observeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                updates -> {
                                    EventCaptureFormFragment.getInstance().showFields(updates);
                                    checkProgress();
                                },
                                Timber::e
                        ));
    }

    private Flowable<List<FieldViewModel>> getFieldFlowable(@Nullable String sectionUid) {
        if (sectionUid == null || sectionUid.equals("NO_SECTION")) {
            return Flowable.zip(
                    eventCaptureRepository.list().subscribeOn(Schedulers.computation()),
                    eventCaptureRepository.calculate().subscribeOn(Schedulers.computation()),
                    this::applyEffects)
                    .map(fields -> {
                        emptyMandatoryFields = new HashMap<>();
                        for (FieldViewModel fieldViewModel : fields) {
                            if (fieldViewModel.mandatory() && isEmpty(fieldViewModel.value()))
                                emptyMandatoryFields.put(fieldViewModel.uid(), fieldViewModel);
                        }
                        return fields;
                    });
        } else {
            return Flowable.zip(
                    eventCaptureRepository.list(sectionUid),
                    eventCaptureRepository.calculate().subscribeOn(Schedulers.computation()),
                    this::applyEffects)
                    .map(fields -> {
                        //Clear all sections fields from map
                        List<String> toRemoveKeys = new ArrayList<>();
                        for (Map.Entry<String, FieldViewModel> entry : emptyMandatoryFields.entrySet()) {
                            if (entry.getValue().programStageSection().equals(sectionUid))
                                toRemoveKeys.add(entry.getKey());
                        }
                        for (String key : toRemoveKeys)
                            emptyMandatoryFields.remove(key);

                        for (FieldViewModel fieldViewModel : fields) {
                            if (fieldViewModel.mandatory() && isEmpty(fieldViewModel.value()))
                                emptyMandatoryFields.put(fieldViewModel.uid(), fieldViewModel);
                        }
                        return fields;
                    });
        }
    }

    private void checkExpiration() {
        compositeDisposable.add(
                metadataRepository.isCompletedEventExpired(eventUid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                hasExpiredResult -> this.hasExpired = hasExpiredResult,
                                Timber::e
                        )
        );
    }

    @Override
    public void onBackClick() {
        view.clearFocus();

        new Handler().postDelayed(
                () -> view.back(),
                1000);
    }

    @Override
    public void subscribeToSection() {
        if (!isSubscribed) {
            isSubscribed = true;
            compositeDisposable.add(
                    currentSectionPosition
                            .startWith(0)
                            .flatMap(position -> {
                                eventCaptureRepository.setLastUpdated(null);
                                if (sectionList == null) {
                                    return eventCaptureRepository.eventSections()
                                            .map(list -> {
                                                sectionList = list;
                                                return position;
                                            });
                                } else {
                                    return Flowable.just(position);
                                }
                            })
                            .flatMap(position -> {
                                FormSectionViewModel formSectionViewModel = getFinalSections().get(position);
                                currentSection.set(formSectionViewModel.sectionUid());
                                if (getFinalSections().size() > 1) {
                                    DataEntryArguments arguments =
                                            DataEntryArguments.forEventSection(formSectionViewModel.uid(),
                                                    formSectionViewModel.sectionUid(),
                                                    formSectionViewModel.renderType());
                                    EventCaptureFormFragment.getInstance().setSectionTitle(arguments, formSectionViewModel);
                                } else {
                                    DataEntryArguments arguments =
                                            DataEntryArguments.forEvent(formSectionViewModel.uid(), formSectionViewModel.renderType());
                                    EventCaptureFormFragment.getInstance().setSingleSection(arguments, formSectionViewModel);
                                }

                                EventCaptureFormFragment.getInstance().setSectionProgress(
                                        getFinalSections().indexOf(formSectionViewModel),
                                        getFinalSections().size());

                                List<FormSectionViewModel> finalSectionList = getFinalSections();

                                return Flowable.just(finalSectionList.size() > 0 ?
                                        finalSectionList.get(position).sectionUid() != null ?
                                                finalSectionList.get(position).sectionUid() :
                                                "NO_SECTION" :
                                        "NO_SECTION");
                            })
                            .observeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    sectionProcessor::onNext,
                                    Timber::e
                            )
            );

            compositeDisposable.add(EventCaptureFormFragment.getInstance().dataEntryFlowable().onBackpressureBuffer()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .switchMap(action -> {
                                eventCaptureRepository.setLastUpdated(action.id());
                                ruleInitTime = System.currentTimeMillis();
                                return dataEntryStore.save(action.id(), action.value());
                            }
                    ).subscribe(result -> Timber.d(result.toString()),
                            Timber::d)
            );

            compositeDisposable.add(
                    EventCaptureFormFragment.getInstance().optionSetActions()
                            .flatMap(
                                    data -> metadataRepository.searchOptions(data.val0(), data.val1(), data.val2(), optionsToHide, optionsGroupsToHide).toFlowable(BackpressureStrategy.LATEST)
                            )
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    options -> {
                                        if (OptionSetDialog.isCreated())
                                            OptionSetDialog.newInstance().setOptions(options);
                                        else if (OptionSetPopUp.isCreated())
                                            OptionSetPopUp.getInstance().setOptions(options);
                                    },
                                    Timber::e
                            ));
        }

    }

    @NonNull
    private List<FieldViewModel> applyEffects(
            @NonNull List<FieldViewModel> viewModels,
            @NonNull Result<RuleEffect> calcResult) {
        Timber.d("RULE EFFECTS INIT TOOK %s ms to execute", System.currentTimeMillis() - ruleInitTime);
        long currentTime = System.currentTimeMillis();

        if (calcResult.error() != null) {
            Timber.e(calcResult.error());
            return viewModels;
        }

        //Reset effects
        optionsToHide.clear();
        optionsGroupsToHide.clear();
        sectionsToHide.clear();
        completeMessage = null;
        canComplete = true;

        Map<String, FieldViewModel> fieldViewModels = toMap(viewModels);
        rulesUtils.applyRuleEffects(fieldViewModels, calcResult, this);

        Timber.d("RULE EFFECTS TOOK %s ms to execute", System.currentTimeMillis() - currentTime);
        return new ArrayList<>(fieldViewModels.values());
    }

    private void checkProgress() {

        if (getFinalSections().size() > 1)
            for (FormSectionViewModel formSectionViewModel : getFinalSections())
                if (formSectionViewModel.sectionUid().equals(currentSection.get()))
                    EventCaptureFormFragment.getInstance().setSectionProgress(
                            getFinalSections().indexOf(formSectionViewModel),
                            getFinalSections().size());
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
                () -> changeSection(),
                1000);

    }

    private void changeSection() {
        List<FormSectionViewModel> finalSections = getFinalSections();

        if (currentPosition < finalSections.size() - 1) {
            currentSectionPosition.onNext(++currentPosition);
        } else {
            if (eventStatus != EventStatus.ACTIVE) {
                setUpActionByStatus(eventStatus);
            } else if (!emptyMandatoryFields.isEmpty()) {
                view.setMandatoryWarning(emptyMandatoryFields);
            } else if (!this.errors.isEmpty()) {
                view.setShowError(errors);
            } else {
                compositeDisposable.add(
                        Observable.just(completeMessage != null ? completeMessage : "")
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .filter(completeMessage -> !isEmpty(completeMessage))
                                .subscribe(
                                        data -> view.showMessageOnComplete(canComplete, completeMessage),
                                        Timber::e,
                                        () -> view.attemptToFinish(canComplete && eventCaptureRepository.isEnrollmentOpen())
                                )
                );
            }
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
    public Observable<List<OrganisationUnitModel>> getOrgUnits() {
        return metadataRepository.getOrganisationUnits();
    }

    @Override
    public ObservableField<String> getCurrentSection() {
        return currentSection;
    }

    @Override
    public boolean isEnrollmentOpen() {
        return eventCaptureRepository.isEnrollmentOpen();
    }

    @Override
    public void onSectionSelectorClick(boolean isCurrentSection, int position, String
            sectionUid) {

        EventCaptureFormFragment.getInstance().showSectionSelector();
        if (!currentSection.get().equals(sectionUid) && position != -1) {

            goToSection(sectionUid);
        }
    }


    @Override
    public void goToSection(String sectionUid) {
        for (FormSectionViewModel sectionModel : getFinalSections())
            if (sectionModel.sectionUid() != null && sectionModel.sectionUid().equals(sectionUid))
                currentPosition = getFinalSections().indexOf(sectionModel);
        currentSectionPosition.onNext(currentPosition);
    }

    @Override
    public void completeEvent(boolean addNew) {
        compositeDisposable.add(
                eventCaptureRepository.completeEvent()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
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
        if (eventCaptureRepository.reopenEvent()) {
            currentSectionPosition.onNext(0);
            view.showSnackBar(R.string.event_reopened);
        }
    }

    @Override
    public void deleteEvent() {
        compositeDisposable.add(eventCaptureRepository.deleteEvent()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
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
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
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
    public void initCompletionPercentage(FlowableProcessor<Float> completionPercentage) {
        compositeDisposable.add(
                completionPercentage
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                view.updatePercentage(),
                                Timber::e
                        )
        );
    }


    @Override
    public void onDettach() {
        this.compositeDisposable.clear();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }

    //region ruleActions

    @Override
    public void setCalculatedValue(String calculatedValueVariable, String value) {

    }

    @Override
    public void setShowError(@NonNull RuleActionShowError showError, FieldViewModel model) {
        canComplete = false;
        if (!snackBarIsShowing) {
            snackBarIsShowing = true;
            Snackbar.make(view.getSnackbarAnchor(), showError.content(), Snackbar.LENGTH_INDEFINITE)
                    .setAction(view.getAbstracContext().getString(R.string.delete), v1 -> {
                        if (model != null)
                            save(model.uid(), null);
                    })
                    .addCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            snackBarIsShowing = false;
                            super.onDismissed(transientBottomBar, event);
                        }
                    })
                    .show();
        }

    }

    @Override
    public void unsupportedRuleAction() {
//        view.displayMessage(view.getContext().getString(R.string.unsupported_program_rule));
        Timber.d(view.getContext().getString(R.string.unsupported_program_rule));
    }

    @Override
    public void save(@NonNull String uid, @Nullable String value) {
        EventCaptureFormFragment.getInstance().dataEntryFlowable().onNext(RowAction.create(uid, value));
    }

    @Override
    public void setDisplayKeyValue(String label, String value) {
        //TODO: Implement Indicator tabs to show this field
    }

    @Override
    public void sethideSection(String sectionUid) {
        if (!sectionsToHide.contains(sectionUid))
            sectionsToHide.add(sectionUid);
    }

    @Override
    public void setMessageOnComplete(String message, boolean canComplete) {
        this.canComplete = canComplete;
        this.completeMessage = message;
    }

    @Override
    public void setHideProgramStage(String programStageUid) {
        //do not apply
    }

    @Override
    public void setOptionToHide(String optionUid) {
        optionsToHide.add(optionUid);
    }

    @Override
    public void setOptionGroupToHide(String optionGroupUid) {
        optionsGroupsToHide.add(optionGroupUid);
    }

    //endregion
}
