package org.dhis2.data.forms.dataentry;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.RowAction;
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.CodeGenerator;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.common.ObjectStyleModel;
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

import androidx.annotation.NonNull;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
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
    private final MetadataRepository metadataRepository;
    @NonNull
    private final CompositeDisposable disposable;
    private DataEntryView dataEntryView;
    private Map<String, FieldViewModel> currentFieldViewModels;
    private Result<RuleEffect> lastEffects;
    private FlowableProcessor<RowAction> assignProcessor;

    DataEntryPresenterImpl(@NonNull CodeGenerator codeGenerator,
                           @NonNull DataEntryStore dataEntryStore,
                           @NonNull DataEntryRepository dataEntryRepository,
                           @NonNull RuleEngineRepository ruleEngineRepository,
                           @NonNull SchedulerProvider schedulerProvider,
                           @NonNull MetadataRepository metadataRepository) {
        this.codeGenerator = codeGenerator;
        this.dataEntryStore = dataEntryStore;
        this.dataEntryRepository = dataEntryRepository;
        this.ruleEngineRepository = ruleEngineRepository;
        this.schedulerProvider = schedulerProvider;
        this.disposable = new CompositeDisposable();
        this.metadataRepository = metadataRepository;
        this.currentFieldViewModels = new HashMap<>();
        this.assignProcessor = PublishProcessor.create();
    }

    @Override
    public void onAttach(@NonNull DataEntryView dataEntryView) {
        this.dataEntryView = dataEntryView;
        Observable<List<FieldViewModel>> fieldsFlowable = dataEntryRepository.list();
        Flowable<Result<RuleEffect>> ruleEffectFlowable = ruleEngineRepository.calculate()
                .onErrorReturn(throwable -> Result.failure(new Exception(throwable)));

        // Combining results of two repositories into a single stream.
        Flowable<List<FieldViewModel>> viewModelsFlowable = Flowable.zip(
                fieldsFlowable.toFlowable(BackpressureStrategy.LATEST), ruleEffectFlowable, this::applyEffects);

        disposable.add(viewModelsFlowable
                .subscribeOn(schedulerProvider.computation())//check if computation does better than io
                .observeOn(schedulerProvider.ui())
                .subscribe(dataEntryView.showFields(),
                        Timber::d
                ));

        disposable.add(dataEntryView.rowActions()
                .subscribeOn(schedulerProvider.computation())
                .observeOn(schedulerProvider.ui())
                .switchMap(action ->
                        dataEntryStore.save(action.id(), action.value()).
                                map(result -> {
                                    if (result == 5)
                                        dataEntryStore.save(action.id(), null);
                                    return Trio.create(result, action.id(), action.value());
                                })
                ).subscribe(resultUidValue -> {
                            if (resultUidValue.val0() == -5)
                                dataEntryView.showMessage(R.string.unique_warning);
                            else
                                Timber.d("Value %s saved for uid %s", resultUidValue.val2(), resultUidValue.val1());
                        },
                        Timber::d)
        );

        disposable.add(
                assignProcessor
                        .distinctUntilChanged()
                        .flatMap(rowAction ->
                                dataEntryStore.save(rowAction.id(), rowAction.value())
                        )
                        .subscribeOn(schedulerProvider.computation())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                a -> Timber.d("Value assigned saved"),
                                Timber::e
                        )
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
        assignProcessor.onNext(RowAction.create(uid, value));
       /* CompositeDisposable saveDisposable = new CompositeDisposable();
        if (!uid.isEmpty())
            saveDisposable.add(
                    dataEntryStore.save(uid, value)
                            .subscribeOn(Schedulers.computation())
                            .observeOn(Schedulers.io())
                            .subscribe(
                                    data -> Timber.d("Saved uid %s value %s", uid, value),
                                    Timber::e,
                                    saveDisposable::clear
                            ));*/
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

//        if (lastEffects == null || !Objects.equals(lastEffects, calcResult)) {

        this.lastEffects = calcResult;

        Map<String, FieldViewModel> fieldViewModels = toMap(viewModels);
        applyRuleEffects(fieldViewModels, calcResult);

        this.currentFieldViewModels.clear();
        this.currentFieldViewModels = fieldViewModels;

        return new ArrayList<>(fieldViewModels.values());
       /* } else {
            for (FieldViewModel field : viewModels)
                if (currentFieldViewModels.keySet().contains(field.uid()))
                    currentFieldViewModels.put(field.uid(), field.withEditMode(currentFieldViewModels.get(field.uid()).editable()));
            return new ArrayList<>(currentFieldViewModels.values());
        }*/

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
            } else if (ruleAction instanceof RuleActionDisplayText) {
                RuleActionDisplayText displayText = (RuleActionDisplayText) ruleAction;
                String uid = displayText.content();

                EditTextViewModel textViewModel = EditTextViewModel.create(uid,
                        displayText.content(), false, ruleEffect.data(), "Information", 1, ValueType.TEXT, null, false, null, null, ObjectStyleModel.builder().build());

                if (this.currentFieldViewModels == null ||
                        !this.currentFieldViewModels.containsKey(uid)) {
                    fieldViewModels.put(uid, textViewModel);
                } else if (this.currentFieldViewModels.containsKey(uid) &&
                        !currentFieldViewModels.get(uid).value().equals(textViewModel.value())) {
                    fieldViewModels.put(uid, textViewModel);
                } else {

                }
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
            }

            dataEntryView.removeSection(null);

        }
    }
}
