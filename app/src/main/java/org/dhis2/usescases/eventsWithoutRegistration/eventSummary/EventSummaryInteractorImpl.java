package org.dhis2.usescases.eventsWithoutRegistration.eventSummary;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.usescases.eventsWithoutRegistration.EventUtils;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.rules.models.RuleAction;
import org.hisp.dhis.rules.models.RuleActionErrorOnCompletion;
import org.hisp.dhis.rules.models.RuleActionHideField;
import org.hisp.dhis.rules.models.RuleActionHideSection;
import org.hisp.dhis.rules.models.RuleActionSetMandatoryField;
import org.hisp.dhis.rules.models.RuleActionShowError;
import org.hisp.dhis.rules.models.RuleActionShowWarning;
import org.hisp.dhis.rules.models.RuleActionWarningOnCompletion;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import rx.exceptions.OnErrorNotImplementedException;
import timber.log.Timber;

/**
 * QUADRAM. Created by Cristian on 01/03/2018.
 */

public class EventSummaryInteractorImpl implements EventSummaryContract.EventSummaryInteractor {
    private EventSummaryContract.EventSummaryView view;
    @NonNull
    private final MetadataRepository metadataRepository;
    @NonNull
    private final EventSummaryRepository eventSummaryRepository;
    @NonNull
    private CompositeDisposable compositeDisposable;
    @NonNull
    private SchedulerProvider schedulerProvider;

    private String eventUid;
    private EventStatus currentStatus;


    EventSummaryInteractorImpl(@NonNull EventSummaryRepository eventSummaryRepository,
                               @NonNull MetadataRepository metadataRepository,
                               @NonNull SchedulerProvider schedulerProvider) {
        this.metadataRepository = metadataRepository;
        this.eventSummaryRepository = eventSummaryRepository;
        this.schedulerProvider = schedulerProvider;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(@NonNull EventSummaryContract.EventSummaryView view, @NonNull String programId, @NonNull String eventId) {
        this.view = view;
        this.eventUid = eventId;
        getEvent(eventId);
        getProgram(programId);
        getEventSections(eventId);

        compositeDisposable.add(
                eventSummaryRepository.accessDataWrite(eventId)
                        .map(hasDataAccess -> hasDataAccess && eventSummaryRepository.isEnrollmentOpen())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view::accessDataWrite,
                                Timber::e
                        ));

    }

    private void getEvent(String eventId) {
        compositeDisposable.add(eventSummaryRepository.getEvent(eventId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(event -> {
                            this.currentStatus = event.status();
                            view.setActionButton(event);
                        },
                        Timber::e
                ));
    }

    @Override
    public void getProgram(@NonNull String programUid) {
        compositeDisposable.add(metadataRepository.getProgramWithId(programUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::setProgram,
                        Timber::e
                ));
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
    public void onDettach() {
        compositeDisposable.clear();
    }

    @Override
    public void getSectionCompletion(@Nullable String sectionUid) {
        Flowable<List<FieldViewModel>> fieldsFlowable = eventSummaryRepository.list(sectionUid, eventUid);

        Flowable<Result<RuleEffect>> ruleEffectFlowable = eventSummaryRepository.calculate().subscribeOn(schedulerProvider.computation())
                .onErrorReturn(throwable -> Result.failure(new Exception(throwable)));

        // Combining results of two repositories into a single stream.
        Flowable<List<FieldViewModel>> viewModelsFlowable = Flowable.zip(fieldsFlowable, ruleEffectFlowable, this::applyEventSummaryEffects);

        compositeDisposable.add(viewModelsFlowable
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(view.showFields(sectionUid), throwable -> {
                    throw new OnErrorNotImplementedException(throwable);
                }));
    }

    @Override
    public void onDoAction() {
        if (currentStatus != EventStatus.COMPLETED)
            view.checkAction();
        else
            doOnComple();
    }

    @Override
    public void doOnComple() {
        compositeDisposable.add(eventSummaryRepository.changeStatus(eventUid)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        event -> view.onStatusChanged(event),
                        throwable -> {
                            throw new OnErrorNotImplementedException(throwable);
                        }));
    }

    @NonNull
    private List<FieldViewModel> applyEventSummaryEffects(
            @NonNull List<FieldViewModel> viewModels,
            @NonNull Result<RuleEffect> calcResult) {
        if (calcResult.error() != null) {
            Timber.e(calcResult.error());
            return viewModels;
        }

        Map<String, FieldViewModel> fieldViewModels = EventUtils.toMap(viewModels);
        applyEventSummaryRuleEffects(fieldViewModels, calcResult);

        return new ArrayList<>(fieldViewModels.values());
    }

    private void applyWarning(Map<String, FieldViewModel> fieldViewModels, RuleAction ruleAction) {
        RuleActionShowWarning showWarning = (RuleActionShowWarning) ruleAction;
        FieldViewModel model = fieldViewModels.get(showWarning.field());
        if (model != null)
            fieldViewModels.put(showWarning.field(), model.withWarning(showWarning.content()));
        else
            Timber.d("Field with uid %s is missing", showWarning.field());
    }

    private void applyError(Map<String, FieldViewModel> fieldViewModels, RuleAction ruleAction) {
        RuleActionShowError showError = (RuleActionShowError) ruleAction;
        FieldViewModel model = fieldViewModels.get(showError.field());
        if (model != null)
            fieldViewModels.put(showError.field(), model.withError(showError.content()));
        else
            Timber.d("Field with uid %s is missing", showError.field());
        view.fieldWithError(true);
    }

    private void applyMandatory(Map<String, FieldViewModel> fieldViewModels, RuleAction ruleAction) {
        RuleActionSetMandatoryField mandatoryField = (RuleActionSetMandatoryField) ruleAction;
        FieldViewModel model = fieldViewModels.get(mandatoryField.field());
        if (model != null)
            fieldViewModels.put(mandatoryField.field(), model.setMandatory());
    }

    private void applyRuleEffectCases(Map<String, FieldViewModel> fieldViewModels, RuleEffect ruleEffect) {
        RuleAction ruleAction = ruleEffect.ruleAction();
        if (ruleAction instanceof RuleActionShowWarning) {
            applyWarning(fieldViewModels, ruleAction);
        } else if (ruleAction instanceof RuleActionShowError) {
            applyError(fieldViewModels, ruleAction);
        } else if (ruleAction instanceof RuleActionHideField) {
            RuleActionHideField hideField = (RuleActionHideField) ruleAction;
            fieldViewModels.remove(hideField.field());
        } else if (ruleAction instanceof RuleActionWarningOnCompletion) {
            RuleActionWarningOnCompletion warningOnCompletion = (RuleActionWarningOnCompletion) ruleAction;
            view.messageOnComplete(warningOnCompletion.content(), true);
        } else if (ruleAction instanceof RuleActionErrorOnCompletion) {
            RuleActionErrorOnCompletion errorOnCompletion = (RuleActionErrorOnCompletion) ruleAction;
            view.messageOnComplete(errorOnCompletion.content(), false);
        } else if (ruleAction instanceof RuleActionSetMandatoryField) {
            applyMandatory(fieldViewModels, ruleAction);
        } else if (ruleAction instanceof RuleActionHideSection) {
            RuleActionHideSection hideSection = (RuleActionHideSection) ruleAction;
            view.setHideSection(hideSection.programStageSection());
        }
    }

    private void applyEventSummaryRuleEffects(Map<String, FieldViewModel> fieldViewModels, Result<RuleEffect> calcResult) {
        //TODO: APPLY RULE EFFECTS TO ALL MODELS
        view.messageOnComplete(null, true);
        view.fieldWithError(false);
        view.setHideSection(null);

        for (RuleEffect ruleEffect : calcResult.items()) {
            applyRuleEffectCases(fieldViewModels, ruleEffect);
        }
    }
}
