package org.dhis2.data.forms.dataentry;

import androidx.annotation.NonNull;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.forms.dataentry.fields.spinner.SpinnerViewModel;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.Result;
import org.dhis2.utils.RulesActionCallbacks;
import org.dhis2.utils.RulesUtilsProvider;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitLevel;
import org.hisp.dhis.rules.models.RuleActionShowError;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Observable;
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
    private final CompositeDisposable disposable;
    private final RulesUtilsProvider ruleUtils;
    private DataEntryView dataEntryView;
    private List<String> optionsToHide = new ArrayList<>();
    private List<String> optionsGroupsToHide = new ArrayList<>();
    private FlowableProcessor<RowAction> assignProcessor;
    private Map<String, List<String>> optionsGroupToShow = new HashMap<>();
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
                           @NonNull SchedulerProvider schedulerProvider, RulesUtilsProvider ruleUtils) {
        this.dataEntryStore = dataEntryStore;
        this.dataEntryRepository = dataEntryRepository;
        this.ruleEngineRepository = ruleEngineRepository;
        this.schedulerProvider = schedulerProvider;
        this.disposable = new CompositeDisposable();
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
                .switchMap(rowAction -> dataEntryStore.checkUnique(rowAction.id(), rowAction.value())
                        .filter(checkPass -> checkPass) //If not unique
                        .map(checkPass -> rowAction)) //save value
                .switchMap(action -> {
                            if (action.lastFocusPosition() != null && action.lastFocusPosition() >= 0) { //Triggered by form field
                                this.lastFocusItem = action.id();
                            }
                            ruleEngineRepository.updateRuleAttributeMap(action.id(), action.value());
                            onAttach(dataEntryView);
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
    }

    @Override
    public void onDetach() {
        disposable.clear();
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnit>> getOrgUnits() {
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
        optionsGroupToShow.clear();
        Map<String, FieldViewModel> fieldViewModels = toMap(viewModels);
        ruleUtils.applyRuleEffects(fieldViewModels, calcResult, this);

        for (FieldViewModel fieldViewModel : fieldViewModels.values())
            if (fieldViewModel instanceof SpinnerViewModel) {
                ((SpinnerViewModel) fieldViewModel).setOptionsToHide(optionsToHide, optionsGroupsToHide);
                if(optionsGroupToShow.keySet().contains(fieldViewModel.uid()))
                    ((SpinnerViewModel) fieldViewModel).setOptionGroupsToShow(optionsGroupToShow.get(fieldViewModel.uid()));
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
    public void setHideSection(String sectionUid) {

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
    public void setOptionGroupToHide(String optionGroupUid, boolean toHide, String field) {
        if (toHide)
            optionsGroupsToHide.add(optionGroupUid);
        else if(!optionsGroupsToHide.contains(optionGroupUid))//When combined with show option group the hide option group takes precedence.
            if(optionsGroupToShow.get(field) != null)
                optionsGroupToShow.get(field).add(optionGroupUid);
            else
                optionsGroupToShow.put(field, Collections.singletonList(optionGroupUid));

    }

}
