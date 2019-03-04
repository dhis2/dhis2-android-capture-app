package org.dhis2.data.forms.dataentry;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.rules.models.RuleAction;
import org.hisp.dhis.rules.models.RuleActionAssign;
import org.hisp.dhis.rules.models.RuleActionCreateEvent;
import org.hisp.dhis.rules.models.RuleActionDisplayText;
import org.hisp.dhis.rules.models.RuleActionErrorOnCompletion;
import org.hisp.dhis.rules.models.RuleActionHideField;
import org.hisp.dhis.rules.models.RuleActionHideSection;
import org.hisp.dhis.rules.models.RuleActionSetMandatoryField;
import org.hisp.dhis.rules.models.RuleActionShowError;
import org.hisp.dhis.rules.models.RuleActionShowWarning;
import org.hisp.dhis.rules.models.RuleActionWarningOnCompletion;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

@SuppressWarnings("PMD")
final class DataEntryPresenterImpl implements DataEntryPresenter {

    @NonNull
    private final DataEntryStore dataEntryStore;

    @NonNull
    private final DataEntryRepository dataEntryRepository;

    @NonNull
    private final RuleEngineRepository ruleEngineRepository;

    @NonNull
    private final SchedulerProvider schedulerProvider;

    @NonNull
    private final MetadataRepository metadataRepository;
    @NonNull
    private final CompositeDisposable disposable;
    private DataEntryView dataEntryView;
    private HashMap<String, FieldViewModel> currentFieldViewModels;

    DataEntryPresenterImpl(@NonNull DataEntryStore dataEntryStore,
                           @NonNull DataEntryRepository dataEntryRepository,
                           @NonNull RuleEngineRepository ruleEngineRepository,
                           @NonNull SchedulerProvider schedulerProvider,
                           @NonNull MetadataRepository metadataRepository) {
        this.dataEntryStore = dataEntryStore;
        this.dataEntryRepository = dataEntryRepository;
        this.ruleEngineRepository = ruleEngineRepository;
        this.schedulerProvider = schedulerProvider;
        this.disposable = new CompositeDisposable();
        this.metadataRepository = metadataRepository;
    }

    @Override
    public void onAttach(@NonNull DataEntryView dataEntryView) {
        this.dataEntryView = dataEntryView;
        Observable<List<FieldViewModel>> fieldsFlowable = dataEntryRepository.list();
        Flowable<Result<RuleEffect>> ruleEffectFlowable = ruleEngineRepository.calculate()
                .subscribeOn(schedulerProvider.computation()).onErrorReturn(throwable -> Result.failure(new Exception(throwable)));

        // Combining results of two repositories into a single stream.
        Flowable<List<FieldViewModel>> viewModelsFlowable = Flowable.zip(
                fieldsFlowable.toFlowable(BackpressureStrategy.LATEST), ruleEffectFlowable, this::applyEffects);

        disposable.add(viewModelsFlowable
                .subscribeOn(schedulerProvider.io())//check if computation does better than io
                .observeOn(schedulerProvider.ui())
                .subscribe(dataEntryView.showFields(),
                        Timber::d
                ));

        disposable.add(dataEntryView.rowActions().debounce(500, TimeUnit.MILLISECONDS) //TODO: Check debounce time
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .switchMap(action ->
                        dataEntryStore.save(action.id(), action.value()).
                                map(result -> {
                                    if (result == 5)
                                        dataEntryStore.save(action.id(), null);
                                    return result;
                                })
                ).subscribe(result -> {
                            if (result == -5)
                                dataEntryView.showMessage(R.string.unique_warning);
                            else
                                Timber.d(result.toString());
                        },
                        Timber::d)
        );

        disposable.add(
                dataEntryView.optionSetActions()
                        .flatMap(
                                data -> metadataRepository.searchOptions(data.val0(), data.val1(), data.val2()).toFlowable(BackpressureStrategy.LATEST)
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                dataEntryView::setListOptions,
                                Timber::e
                        ));
    }

    private void save(String uid, String value) {
        CompositeDisposable saveDisposable = new CompositeDisposable();
        if (!uid.isEmpty())
            saveDisposable.add(
                    dataEntryStore.save(uid, value)
                            .subscribeOn(Schedulers.computation())
                            .observeOn(Schedulers.io())
                            .subscribe(
                                    data -> Timber.d("SAVED_DATA - DONE"),
                                    Timber::e,
                                    saveDisposable::clear
                            ));
    }

    @Override
    public void onDetach() {
        disposable.clear();
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> getOrgUnits() {
        return dataEntryRepository.getOrgUnits();
    }

    @NonNull
    private List<FieldViewModel> applyEffects(
            @NonNull List<FieldViewModel> viewModels,
            @NonNull Result<RuleEffect> calcResult) {
        if (calcResult.error() != null) {
            Timber.e(calcResult.error());
            return viewModels;
        }

        Map<String, FieldViewModel> fieldViewModels = toMap(viewModels);
        applyRuleEffects(fieldViewModels, calcResult);

        if (this.currentFieldViewModels == null)
            this.currentFieldViewModels = new HashMap<>();
        this.currentFieldViewModels.putAll(fieldViewModels);

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

    private void applyRuleActionShowWarning(RuleAction ruleAction, Map<String, FieldViewModel> fieldViewModels) {
        RuleActionShowWarning showWarning = (RuleActionShowWarning) ruleAction;
        FieldViewModel model = fieldViewModels.get(showWarning.field());

        if (model != null)
            fieldViewModels.put(showWarning.field(),
                    model.withWarning(showWarning.content()));
        else
            Timber.d("Field with uid %s is missing", showWarning.field());
    }

    private void applyRuleActionShowError(RuleAction ruleAction, Map<String, FieldViewModel> fieldViewModels) {
        RuleActionShowError showError = (RuleActionShowError) ruleAction;
        FieldViewModel model = fieldViewModels.get(showError.field());

        if (model != null)
            fieldViewModels.put(showError.field(),
                    model.withError(showError.content()));
    }

    private void applyRuleActionHideField(RuleAction ruleAction, Map<String, FieldViewModel> fieldViewModels) {
        RuleActionHideField hideField = (RuleActionHideField) ruleAction;
        fieldViewModels.remove(hideField.field());
        dataEntryStore.save(hideField.field(), null);
    }

    private void applyRuleActionHideSection(RuleAction ruleAction) {
        RuleActionHideSection hideSection = (RuleActionHideSection) ruleAction;
        dataEntryView.removeSection(hideSection.programStageSection());
    }

    private void applyRuleActionDisplayText(RuleAction ruleAction, Map<String, FieldViewModel> fieldViewModels, RuleEffect ruleEffect) {
        RuleActionDisplayText displayText = (RuleActionDisplayText) ruleAction;
        String uid = displayText.content();

        EditTextViewModel textViewModel = EditTextViewModel.create(uid,
                displayText.content(), false, ruleEffect.data(), "Information", 1, ValueType.TEXT, null,
                false, null, null, null);

        if (condition1(uid) || condition2(uid, textViewModel)) {
            fieldViewModels.put(uid, textViewModel);
        }
    }

    private boolean condition1(String uid) {
        return this.currentFieldViewModels == null ||
                !this.currentFieldViewModels.containsKey(uid);
    }

    private boolean condition2(String uid, EditTextViewModel textViewModel) {
        return this.currentFieldViewModels.containsKey(uid) &&
                !currentFieldViewModels.get(uid).value().equals(textViewModel.value());
    }

    private void applyRuleActionAssign(RuleAction ruleAction, Map<String, FieldViewModel> fieldViewModels, RuleEffect ruleEffect) {
        RuleActionAssign assign = (RuleActionAssign) ruleAction;

        if (fieldViewModels.get(assign.field()) == null)
            save(assign.field(), ruleEffect.data());
        else {
            String value = fieldViewModels.get(assign.field()).value();

            if (value == null || !value.equals(ruleEffect.data())) {
                save(assign.field(), ruleEffect.data());
            }

            fieldViewModels.put(assign.field(), fieldViewModels.get(assign.field()).withValue(ruleEffect.data()));

        }
    }

    private void applyRuleActionMandatoryField(RuleAction ruleAction, Map<String, FieldViewModel> fieldViewModels) {
        RuleActionSetMandatoryField mandatoryField = (RuleActionSetMandatoryField) ruleAction;
        FieldViewModel model = fieldViewModels.get(mandatoryField.field());
        if (model != null)
            fieldViewModels.put(mandatoryField.field(), model.setMandatory());
    }

    private void applyRuleActionWarningOnComplete(RuleAction ruleAction) {
        RuleActionWarningOnCompletion warningOnCompletion = (RuleActionWarningOnCompletion) ruleAction;
        dataEntryView.messageOnComplete(warningOnCompletion.content(), true);
    }

    private void applyRuleActionErrorOnComplete(RuleAction ruleAction) {
        RuleActionErrorOnCompletion errorOnCompletion = (RuleActionErrorOnCompletion) ruleAction;
        dataEntryView.messageOnComplete(errorOnCompletion.content(), false);
    }

    @SuppressWarnings("squid:CommentedOutCodeLine")
    private void applyRuleEffects(Map<String, FieldViewModel> fieldViewModels, Result<RuleEffect> calcResult) {

        for (RuleEffect ruleEffect : calcResult.items()) {
            RuleAction ruleAction = ruleEffect.ruleAction();
            if (ruleAction instanceof RuleActionShowWarning) {
                applyRuleActionShowWarning(ruleAction, fieldViewModels);

            } else if (ruleAction instanceof RuleActionShowError) {
                applyRuleActionShowError(ruleAction, fieldViewModels);

            } else if (ruleAction instanceof RuleActionHideField) {
                applyRuleActionHideField(ruleAction, fieldViewModels);

            } else if (ruleAction instanceof RuleActionDisplayText) {
                applyRuleActionDisplayText(ruleAction, fieldViewModels, ruleEffect);

            } else if (ruleAction instanceof RuleActionHideSection) {
                applyRuleActionHideSection(ruleAction);

            } else if (ruleAction instanceof RuleActionAssign) {
                applyRuleActionAssign(ruleAction, fieldViewModels, ruleEffect);

            } else if (ruleAction instanceof RuleActionCreateEvent) {
                //TODO: CREATE event with data from createEvent
//                RuleActionCreateEvent createEvent = (RuleActionCreateEvent) ruleAction;
            } else if (ruleAction instanceof RuleActionSetMandatoryField) {
                applyRuleActionMandatoryField(ruleAction, fieldViewModels);

            } else if (ruleAction instanceof RuleActionWarningOnCompletion) {
                applyRuleActionWarningOnComplete(ruleAction);

            } else if (ruleAction instanceof RuleActionErrorOnCompletion) {
                applyRuleActionErrorOnComplete(ruleAction);

            }

            dataEntryView.removeSection(null);
        }
    }
}
