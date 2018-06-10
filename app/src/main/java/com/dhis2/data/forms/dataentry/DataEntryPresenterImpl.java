package com.dhis2.data.forms.dataentry;

import android.support.annotation.NonNull;

import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel;
import com.dhis2.data.schedulers.SchedulerProvider;
import com.dhis2.utils.CodeGenerator;
import com.dhis2.utils.Result;

import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.rules.models.RuleAction;
import org.hisp.dhis.rules.models.RuleActionDisplayKeyValuePair;
import org.hisp.dhis.rules.models.RuleActionDisplayText;
import org.hisp.dhis.rules.models.RuleActionHideField;
import org.hisp.dhis.rules.models.RuleActionHideSection;
import org.hisp.dhis.rules.models.RuleActionShowError;
import org.hisp.dhis.rules.models.RuleActionShowWarning;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

@SuppressWarnings("PMD")
final class DataEntryPresenterImpl implements DataEntryPresenter {

    @NonNull
    private final CodeGenerator codeGenerator;

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
    private DataEntryView dataEntryView;

    DataEntryPresenterImpl(@NonNull CodeGenerator codeGenerator,
                           @NonNull DataEntryStore dataEntryStore,
                           @NonNull DataEntryRepository dataEntryRepository,
                           @NonNull RuleEngineRepository ruleEngineRepository,
                           @NonNull SchedulerProvider schedulerProvider) {
        this.codeGenerator = codeGenerator;
        this.dataEntryStore = dataEntryStore;
        this.dataEntryRepository = dataEntryRepository;
        this.ruleEngineRepository = ruleEngineRepository;
        this.schedulerProvider = schedulerProvider;
        this.disposable = new CompositeDisposable();
    }

    @Override
    public void onAttach(@NonNull DataEntryView dataEntryView) {
        this.dataEntryView = dataEntryView;
        Flowable<List<FieldViewModel>> fieldsFlowable = dataEntryRepository.list();
        Flowable<Result<RuleEffect>> ruleEffectFlowable = ruleEngineRepository.calculate()
                .subscribeOn(schedulerProvider.computation());

        // Combining results of two repositories into a single stream.
        Flowable<List<FieldViewModel>> viewModelsFlowable = Flowable.zip(
                fieldsFlowable, ruleEffectFlowable, this::applyEffects);

        disposable.add(viewModelsFlowable
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(dataEntryView.showFields(),
                        Timber::d
                ));

        disposable.add(dataEntryView.rowActions().debounce(1500, TimeUnit.MILLISECONDS)
                .subscribeOn(schedulerProvider.ui())
                .observeOn(schedulerProvider.io())
                .switchMap(action ->
                        {
                            Timber.d("dataEntryRepository.save(uid=[%s], value=[%s])",
                                    action.id(), action.value());
                            return dataEntryStore.save(action.id(), action.value());
                        }
                ).subscribe(result -> Timber.d(result.toString()),
                        Timber::d)
        );
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
                } /*else if (model != null && model instanceof EditTextDoubleViewModel) {
                    fieldViewModels.put(showWarning.field(),
                            ((EditTextDoubleViewModel) model).withWarning(showWarning.content()));
                } else if (model != null && model instanceof EditTextIntegerViewModel) {
                    fieldViewModels.put(showWarning.field(), ((EditTextIntegerViewModel) model)
                            .withWarning(showWarning.content()));
                }*/
            } else if (ruleAction instanceof RuleActionShowError) {
                RuleActionShowError showError = (RuleActionShowError) ruleAction;
                FieldViewModel model = fieldViewModels.get(showError.field());

                if (model != null && model instanceof EditTextViewModel) {
                    fieldViewModels.put(showError.field(),
                            ((EditTextViewModel) model).withError(showError.content()));
                } /*else if (model != null && model instanceof EditTextDoubleViewModel) {
                    fieldViewModels.put(showError.field(),
                            ((EditTextDoubleViewModel) model).withWarning(showError.content()));
                } else if (model != null && model instanceof EditTextIntegerViewModel) {
                    fieldViewModels.put(showError.field(), ((EditTextIntegerViewModel) model)
                            .withWarning(showError.content()));
                }*/
            } else if (ruleAction instanceof RuleActionHideField) {
                RuleActionHideField hideField = (RuleActionHideField) ruleAction;
                fieldViewModels.remove(hideField.field());
            } else if (ruleAction instanceof RuleActionDisplayText) {
                String uid = codeGenerator.generate();
                RuleActionDisplayText displayText = (RuleActionDisplayText) ruleAction;
                EditTextViewModel textViewModel = EditTextViewModel.create(uid,
                        displayText.content(), false, displayText.data(), "Information", 1, ValueType.TEXT, null, true);
                fieldViewModels.put(uid, textViewModel);
            } else if (ruleAction instanceof RuleActionDisplayKeyValuePair) {
                String uid = codeGenerator.generate();

                RuleActionDisplayKeyValuePair displayText =
                        (RuleActionDisplayKeyValuePair) ruleAction;
               /* TextViewModel textViewModel = TextViewModel.create(uid,
                        displayText.content(), displayText.data());

                fieldViewModels.put(uid, textViewModel);*/
            } else if (ruleAction instanceof RuleActionHideSection) {
                RuleActionHideSection hideSection = (RuleActionHideSection) ruleAction;
                dataEntryView.removeSection(hideSection.programStageSection());
            }
        }
    }
}
