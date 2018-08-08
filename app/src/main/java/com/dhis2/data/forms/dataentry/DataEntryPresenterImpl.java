package com.dhis2.data.forms.dataentry;

import android.support.annotation.NonNull;
import android.util.Log;

import com.dhis2.data.forms.dataentry.fields.FieldViewModel;
import com.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel;
import com.dhis2.data.schedulers.SchedulerProvider;
import com.dhis2.utils.CodeGenerator;
import com.dhis2.utils.Result;

import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.rules.models.RuleAction;
import org.hisp.dhis.rules.models.RuleActionAssign;
import org.hisp.dhis.rules.models.RuleActionCreateEvent;
import org.hisp.dhis.rules.models.RuleActionDisplayKeyValuePair;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
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
                .subscribeOn(schedulerProvider.io())//check if computation does better than io
                .observeOn(schedulerProvider.ui())
                .subscribe(dataEntryView.showFields(),
                        Timber::d
                ));

        disposable.add(dataEntryView.rowActions().debounce(500, TimeUnit.MILLISECONDS) //TODO: Check debounce time
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

    private void save(String uid, String value) {
        CompositeDisposable saveDisposable = new CompositeDisposable();
        saveDisposable.add(
                dataEntryStore.save(uid, value)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(Schedulers.io())
                        .subscribe(
                                data -> Log.d("SAVED_DATA", "DONE"),
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
        for (RuleEffect ruleEffect : calcResult.items()) {
            RuleAction ruleAction = ruleEffect.ruleAction();
            if (ruleAction instanceof RuleActionShowWarning) {
                RuleActionShowWarning showWarning = (RuleActionShowWarning) ruleAction;
                FieldViewModel model = fieldViewModels.get(showWarning.field());

                if (model != null)
                    fieldViewModels.put(showWarning.field(),
                            model.withWarning(showWarning.content()));
                else
                    Log.d("PR_FIELD_ERROR", String.format("Field with uid %s is missing", showWarning.field()));

            } else if (ruleAction instanceof RuleActionShowError) {
                RuleActionShowError showError = (RuleActionShowError) ruleAction;
                FieldViewModel model = fieldViewModels.get(showError.field());

                if (model != null)
                    fieldViewModels.put(showError.field(),
                            model.withError(showError.content()));

            } else if (ruleAction instanceof RuleActionHideField) {
                RuleActionHideField hideField = (RuleActionHideField) ruleAction;
                fieldViewModels.remove(hideField.field());
                dataEntryStore.save(hideField.field(), null);
            } else if (ruleAction instanceof RuleActionDisplayText) {
                String uid = codeGenerator.generate();
                RuleActionDisplayText displayText = (RuleActionDisplayText) ruleAction;
                EditTextViewModel textViewModel = EditTextViewModel.create(uid,
                        displayText.content(), false, ruleEffect.data(), "Information", 1, ValueType.TEXT, null, false);
                fieldViewModels.put(uid, textViewModel);
            } else if (ruleAction instanceof RuleActionDisplayKeyValuePair) {
                String uid = codeGenerator.generate();
                RuleActionDisplayKeyValuePair displayKeyValuePair = (RuleActionDisplayKeyValuePair) ruleAction;
                EditTextViewModel textViewModel = EditTextViewModel.create(uid,
                        displayKeyValuePair.content(), false, ruleEffect.data(), "Information", 1, ValueType.TEXT, null, false);
                fieldViewModels.put(uid, textViewModel);

            } else if (ruleAction instanceof RuleActionHideSection) {
                RuleActionHideSection hideSection = (RuleActionHideSection) ruleAction;
                dataEntryView.removeSection(hideSection.programStageSection());
            } else if (ruleAction instanceof RuleActionAssign) {
                RuleActionAssign assign = (RuleActionAssign) ruleAction;

                if (fieldViewModels.get(assign.field()) == null)
                    save(assign.field(), ruleEffect.data());
                else {
                    String value = fieldViewModels.get(assign.field()).value();

                    if (value == null || !value.equals(ruleEffect.data()))
                        save(assign.field(), ruleEffect.data());

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
            }

            dataEntryView.removeSection(null);

        }
    }
}
