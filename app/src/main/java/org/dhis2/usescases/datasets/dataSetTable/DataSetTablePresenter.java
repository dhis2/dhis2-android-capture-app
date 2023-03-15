package org.dhis2.usescases.datasets.dataSetTable;

import androidx.annotation.VisibleForTesting;

import org.dhis2.commons.data.tuples.Pair;
import org.dhis2.commons.data.tuples.Trio;
import org.dhis2.commons.matomo.Actions;
import org.dhis2.commons.matomo.Categories;
import org.dhis2.commons.matomo.Labels;
import org.dhis2.commons.schedulers.SchedulerProvider;
import org.dhis2.data.dhislogic.DhisPeriodUtils;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetSection;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.dhis2.utils.validationrules.ValidationRuleResult;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.validation.engine.ValidationResult.ValidationResultStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import kotlin.Unit;
import timber.log.Timber;

public class DataSetTablePresenter implements DataSetTableContract.Presenter {

    private final DataSetTableRepositoryImpl tableRepository;
    private final SchedulerProvider schedulerProvider;
    private final AnalyticsHelper analyticsHelper;
    private final List<DataSetSection> sections = new ArrayList<>();
    private DataSetTableContract.View view;
    public CompositeDisposable disposable;
    private DhisPeriodUtils periodUtils;
    private String orgUnitUid;
    private String catCombo;
    private String periodId;
    private FlowableProcessor<Boolean> validationProcessor;
    private FlowableProcessor<Unit> updateProcessor;

    public DataSetTablePresenter(
            DataSetTableContract.View view,
            DataSetTableRepositoryImpl dataSetTableRepository,
            DhisPeriodUtils periodUtils,
            SchedulerProvider schedulerProvider,
            AnalyticsHelper analyticsHelper,
            FlowableProcessor<Unit> updateProcessor) {
        this.view = view;
        this.tableRepository = dataSetTableRepository;
        this.periodUtils = periodUtils;
        this.schedulerProvider = schedulerProvider;
        this.analyticsHelper = analyticsHelper;
        this.validationProcessor = PublishProcessor.create();
        this.updateProcessor = updateProcessor;
        disposable = new CompositeDisposable();
    }

    @Override
    public void init(String orgUnitUid, String catCombo, String periodId) {
        this.orgUnitUid = orgUnitUid;
        this.catCombo = catCombo;
        this.periodId = periodId;

        disposable.add(
                tableRepository.getSections()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(sections -> {
                            this.sections.clear();
                            this.sections.addAll(sections);
                            view.setSections(sections);
                        }, Timber::e)
        );

        disposable.add(
                Flowable.zip(
                                tableRepository.getDataSet().toFlowable(),
                                tableRepository.getCatComboName(catCombo),
                                tableRepository.getPeriod().map(period -> periodUtils.getPeriodUIString(period.periodType(), period.startDate(), Locale.getDefault())).toFlowable(),
                                tableRepository.getOrgUnit().toFlowable(),
                                tableRepository.isComplete().toFlowable(),
                                this::renderDetails
                        )
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                data -> view.renderDetails(data),
                                Timber::e
                        )
        );

        disposable.add(
                validationProcessor
                        .flatMap(runValidation -> tableRepository.executeValidationRules())
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                this::handleValidationResult,
                                t -> {
                                    Timber.e(t);
                                    view.showInternalValidationError();
                                }
                        )
        );

        disposable.add(
                view.observeSaveButtonClicks()
                        .subscribeOn(schedulerProvider.ui())
                        .toFlowable(BackpressureStrategy.LATEST)
                        .debounce(500, TimeUnit.MILLISECONDS, schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                o -> handleSaveClick(),
                                Timber::e));
    }

    private DataSetRenderDetails renderDetails(
            DataSet dataSet,
            String attrOptionComboName,
            String periodLabel,
            OrganisationUnit organisationUnit,
            Boolean isComplete
    ){
        return new DataSetRenderDetails(
                dataSet.displayName(),
                organisationUnit.displayName(),
                periodLabel,
                attrOptionComboName,
                isComplete
        );
    }

    @VisibleForTesting
    public void handleValidationResult(ValidationRuleResult result) {
        if (result.getValidationResultStatus() == ValidationResultStatus.OK) {
            if (!isComplete()) {
                view.showSuccessValidationDialog();
            } else {
                view.saveAndFinish();
            }
        } else {
            view.showErrorsValidationDialog(result.getViolations());
        }
    }

    @VisibleForTesting
    public void handleSaveClick() {
        if (view.isErrorBottomSheetShowing()) {
            closeBottomSheet();
        }
        if (tableRepository.hasValidationRules()) {
            if (tableRepository.areValidationRulesMandatory()) {
                validationProcessor.onNext(true);
            } else {
                view.showValidationRuleDialog();
            }
        } else if (!isComplete()) {
            view.showSuccessValidationDialog();
        } else {
            view.saveAndFinish();
        }
    }

    @VisibleForTesting
    public Flowable<Boolean> runValidationProcessor() {
        return validationProcessor;
    }

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void onDettach() {
        disposable.dispose();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }

    public String getOrgUnitUid() {
        return orgUnitUid;
    }

    public String getPeriodId() {
        return periodId;
    }

    public String getCatCombo() {
        return catCombo;
    }

    @Override
    public void executeValidationRules() {
        validationProcessor.onNext(true);
    }

    @Override
    public void completeDataSet() {
        disposable.add(
                Single.zip(
                                tableRepository.checkMandatoryFields(),
                                tableRepository.checkFieldCombination(),
                                Pair::create)
                        .flatMap(missingAndCombination -> {
                            boolean mandatoryFieldOk = missingAndCombination.val0().isEmpty();
                            boolean fieldCombinationOk = missingAndCombination.val1().val0();
                            if (mandatoryFieldOk && fieldCombinationOk) {
                                return tableRepository.completeDataSetInstance()
                                        .map(alreadyCompleted ->
                                                Trio.create(alreadyCompleted, true, true));
                            } else {
                                return Single.just(
                                        Trio.create(false, mandatoryFieldOk, fieldCombinationOk)
                                );
                            }
                        })
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(completedMissingAndCombination -> {
                            boolean alreadyCompleted = completedMissingAndCombination.val0();
                            boolean mandatoryFieldOk = completedMissingAndCombination.val1();
                            boolean fieldCombinationOk = completedMissingAndCombination.val2();
                            if (!mandatoryFieldOk) {
                                view.showMandatoryMessage(true);
                            } else if (!fieldCombinationOk) {
                                view.showMandatoryMessage(false);
                            } else if (!alreadyCompleted) {
                                view.savedAndCompleteMessage();
                            } else {
                                view.saveAndFinish();
                            }
                        }, Timber::e)
        );
    }

    @Override
    public void reopenDataSet() {
        disposable.add(
                tableRepository.reopenDataSet()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                done -> view.displayReopenedMessage(done),
                                Timber::e)
        );
    }

    @Override
    public boolean shouldAllowCompleteAnyway() {
        return !tableRepository.isComplete().blockingGet() && !isValidationMandatoryToComplete();
    }

    @Override
    public void collapseExpandBottomSheet() {
        view.collapseExpandBottom();
    }

    @Override
    public void closeBottomSheet() {
        view.closeBottomSheet();
    }

    @Override
    public void onCompleteBottomSheet() {
        view.completeBottomSheet();
    }

    @Override
    public boolean isValidationMandatoryToComplete() {
        return tableRepository.areValidationRulesMandatory();
    }

    @Override
    public boolean isComplete() {
        return tableRepository.isComplete().blockingGet();
    }

    @Override
    public void updateData() {
        updateProcessor.onNext(Unit.INSTANCE);
    }

    @Override
    public void onClickSyncStatus() {
        analyticsHelper.trackMatomoEvent(
                Categories.DATASET_DETAIL,
                Actions.SYNC_DATASET,
                Labels.CLICK);
    }

    @Override
    public boolean dataSetHasDataElementDecoration() {
        return tableRepository.hasDataElementDecoration();
    }

    @Override
    public void editingCellValue(boolean isEditing) {
        if (isEditing) {
            view.startInputEdition();
        } else {
            view.finishInputEdition();
        }
    }

    @Override
    public String getFirstSection() {
        if (sections.isEmpty()) {
            return tableRepository.getSections().blockingFirst().get(0).getUid();
        } else {
            return sections.get(0).getUid();
        }
    }
}
