package org.dhis2.data.forms.dataentry;

import androidx.annotation.NonNull;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.Result;
import org.dhis2.utils.RulesActionCallbacks;
import org.dhis2.utils.RulesUtilsProvider;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.rules.models.RuleAction;
import org.hisp.dhis.rules.models.RuleActionAssign;
import org.hisp.dhis.rules.models.RuleActionCreateEvent;
import org.hisp.dhis.rules.models.RuleActionErrorOnCompletion;
import org.hisp.dhis.rules.models.RuleActionHideField;
import org.hisp.dhis.rules.models.RuleActionHideOption;
import org.hisp.dhis.rules.models.RuleActionHideOptionGroup;
import org.hisp.dhis.rules.models.RuleActionHideSection;
import org.hisp.dhis.rules.models.RuleActionSetMandatoryField;
import org.hisp.dhis.rules.models.RuleActionShowError;
import org.hisp.dhis.rules.models.RuleActionShowWarning;
import org.hisp.dhis.rules.models.RuleActionWarningOnCompletion;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

@SuppressWarnings("PMD")
final class DataEntryPresenterImpl implements DataEntryPresenter, RulesActionCallbacks {

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
    private final RulesUtilsProvider ruleUtils;
    private DataEntryView dataEntryView;
    private List<String> optionsToHide = new ArrayList<>();
    private List<String> optionsGroupsToHide = new ArrayList<>();
    private FlowableProcessor<RowAction> assignProcessor;

    @Override
    public String getLastFocusItem() {
        return lastFocusItem;
    }

    @Override
    public void clearLastFocusItem() {
        this.lastFocusItem = null;
    }

    private String lastFocusItem;

    DataEntryPresenterImpl(@NonNull DataEntryStore dataEntryStore,
                           @NonNull DataEntryRepository dataEntryRepository,
                           @NonNull RuleEngineRepository ruleEngineRepository,
                           @NonNull SchedulerProvider schedulerProvider,
                           @NonNull MetadataRepository metadataRepository, RulesUtilsProvider ruleUtils) {
        this.dataEntryStore = dataEntryStore;
        this.dataEntryRepository = dataEntryRepository;
        this.ruleEngineRepository = ruleEngineRepository;
        this.schedulerProvider = schedulerProvider;
        this.disposable = new CompositeDisposable();
        this.metadataRepository = metadataRepository;
        this.assignProcessor = PublishProcessor.create();
        this.ruleUtils = ruleUtils;

    }

    @Override
    public void onAttach(@NonNull DataEntryView dataEntryView) {
        this.dataEntryView = dataEntryView;

        disposable.add(Flowable.zip(
                dataEntryRepository.list().subscribeOn(Schedulers.computation()),
                ruleEngineRepository.calculate().subscribeOn(Schedulers.computation()),
                this::applyEffects)
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .subscribe(dataEntryView.showFields(),
                        Timber::d
                ));

        disposable.add(dataEntryView.rowActions().onBackpressureBuffer()
                .switchMap(action -> {
                            if (action.lastFocusPosition() != null && action.lastFocusPosition() >= 0) { //Triggered by form field
                                this.lastFocusItem = action.id();
                            }
                            ruleEngineRepository.updateRuleAttributeMap(action.id(), action.value());
                            return dataEntryStore.save(action.id(), action.value()).
                                    map(result -> Trio.create(result, action.id(), action.value()));
                        }
                )
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(resultUidValue -> {
                            Timber.d("Value %s saved for uid %s", resultUidValue.val2(), resultUidValue.val1());
                            if (resultUidValue.val0() == 0)
                                dataEntryView.nextFocus();
                        },
                        Timber::d)
        );

        disposable.add(dataEntryView.rowActions()
                .filter(rowAction -> !isEmpty(rowAction.value()))
                .switchMap(rowAction -> dataEntryStore.checkUnique(rowAction.id(), rowAction.value()))
                .filter(checkPass -> !checkPass)
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        result -> dataEntryView.showMessage(R.string.unique_warning), Timber::e)
        );
        disposable.add(
                assignProcessor
                        .distinctUntilChanged()
                        .flatMap(rowAction -> {
                                    Timber.d("Assigned Value %s saved for field %s", rowAction.value(), rowAction.id());
                                    return dataEntryStore.save(rowAction.id(), rowAction.value());
                                }
                        )
                        .subscribeOn(schedulerProvider.computation())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                a -> Timber.d("Value assigned saved with response %s", a),
                                Timber::e
                        )
        );

        disposable.add(
                dataEntryView.optionSetActions()
                        .switchMap(
                                data -> metadataRepository.searchOptions(data.val0(), data.val1(), data.val2(), optionsToHide, optionsGroupsToHide).toFlowable(BackpressureStrategy.LATEST)
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                dataEntryView::setListOptions,
                                Timber::e
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

    @Override
    public Observable<List<OrganisationUnitLevel>> getLevels() {
        return dataEntryRepository.getOrgUnitLevels();
    }

    @NonNull
    private List<FieldViewModel> applyEffects(
            @NonNull List<FieldViewModel> viewModels,
            @NonNull Result<RuleEffect> calcResult) {
        if (calcResult.error() != null) {
            Timber.e(calcResult.error());
            return viewModels;
        }

        optionsToHide.clear();
        optionsGroupsToHide.clear();

        Map<String, FieldViewModel> fieldViewModels = toMap(viewModels);
        ruleUtils.applyRuleEffects(fieldViewModels, calcResult, this);
//        applyRuleEffects(fieldViewModels, calcResult);

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

        for (RuleEffect ruleEffect : calcResult.items()) {
            RuleAction ruleAction = ruleEffect.ruleAction();
            if (ruleAction instanceof RuleActionShowWarning) {
                RuleActionShowWarning showWarning = (RuleActionShowWarning) ruleAction;
                FieldViewModel model = fieldViewModels.get(showWarning.field());

                if (model != null)
                    fieldViewModels.put(showWarning.field(),
                            model.withWarning(showWarning.content() + ruleEffect.data()));
                else
                    Timber.d("Field with uid %s is missing", showWarning.field());

            } else if (ruleAction instanceof RuleActionShowError) {
                RuleActionShowError showError = (RuleActionShowError) ruleAction;
                FieldViewModel model = fieldViewModels.get(showError.field());

                if (model != null)
                    fieldViewModels.put(showError.field(),
                            model.withError(showError.content() + ruleEffect.data()));

            } else if (ruleAction instanceof RuleActionHideField) {
                RuleActionHideField hideField = (RuleActionHideField) ruleAction;
                fieldViewModels.remove(hideField.field());
                save(hideField.field(), null);
            } else if (ruleAction instanceof RuleActionHideSection) {
                RuleActionHideSection hideSection = (RuleActionHideSection) ruleAction;
                dataEntryView.removeSection(hideSection.programStageSection());
            } else if (ruleAction instanceof RuleActionAssign) {
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

            } else if (ruleAction instanceof RuleActionCreateEvent) {
                RuleActionCreateEvent createEvent = (RuleActionCreateEvent) ruleAction;
                //TODO: CREATE event with data from createEvent
            } else if (ruleAction instanceof RuleActionSetMandatoryField) {
                RuleActionSetMandatoryField mandatoryField = (RuleActionSetMandatoryField) ruleAction;
                FieldViewModel model = fieldViewModels.get(mandatoryField.field());
                if (model != null)
                    fieldViewModels.put(mandatoryField.field(), model.setMandatory());
            } else if (ruleAction instanceof RuleActionWarningOnCompletion) {
                RuleActionWarningOnCompletion warningOnCompletion = (RuleActionWarningOnCompletion) ruleAction;
                dataEntryView.messageOnComplete(warningOnCompletion.content(), true);
            } else if (ruleAction instanceof RuleActionErrorOnCompletion) {
                RuleActionErrorOnCompletion errorOnCompletion = (RuleActionErrorOnCompletion) ruleAction;
                dataEntryView.messageOnComplete(errorOnCompletion.content(), false);
            } else if (ruleAction instanceof RuleActionHideOption) {
                RuleActionHideOption hideOption = (RuleActionHideOption) ruleAction;
                dataEntryStore.save(hideOption.field(), null);
                optionsToHide.add(hideOption.field());
            } else if (ruleAction instanceof RuleActionHideOptionGroup) {
                RuleActionHideOptionGroup hideOptionGroup = (RuleActionHideOptionGroup) ruleAction;
                optionsGroupsToHide.add(hideOptionGroup.optionGroup());
            }

            dataEntryView.removeSection(null);

        }
    }

    @Override
    public void setCalculatedValue(String calculatedValueVariable, String value) {

    }

    @Override
    public void setShowError(@NonNull RuleActionShowError showError, FieldViewModel model) {

    }

    @Override
    public void unsupportedRuleAction() {

    }

    @Override
    public void save(String uid, String value) {
        dataEntryView.getActionProcessor().onNext(RowAction.create(uid, value));
    }

    @Override
    public void setDisplayKeyValue(String label, String value) {

    }

    @Override
    public void sethideSection(String sectionUid) {

    }

    @Override
    public void setMessageOnComplete(String content, boolean canComplete) {

    }

    @Override
    public void setHideProgramStage(String programStageUid) {

    }

    @Override
    public void setOptionToHide(String optionUid) {
        optionsToHide.add(optionUid);
    }

    @Override
    public void setOptionGroupToHide(String optionGroupUid) {
        optionsGroupsToHide.add(optionGroupUid);

    }

}
