package org.dhis2.usescases.datasets.dataSetTable;

import androidx.annotation.VisibleForTesting;

import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Trio;
import org.dhis2.utils.analytics.AnalyticsHelper;
import org.hisp.dhis.android.core.validation.engine.ValidationResult;
import org.hisp.dhis.android.core.validation.engine.ValidationResult.ValidationResultStatus;
import org.hisp.dhis.android.core.validation.engine.ValidationResultViolation;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

public class DataSetTablePresenter implements DataSetTableContract.Presenter {

    private final DataSetTableRepositoryImpl tableRepository;
    private final SchedulerProvider schedulerProvider;
    private final AnalyticsHelper analyticsHelper;
    private DataSetTableContract.View view;
    public CompositeDisposable disposable;

    private String orgUnitUid;
    private String periodTypeName;
    private String periodFinalDate;
    private String catCombo;
    private String periodId;

    public DataSetTablePresenter(
            DataSetTableContract.View view,
            DataSetTableRepositoryImpl dataSetTableRepository,
            SchedulerProvider schedulerProvider,
            AnalyticsHelper analyticsHelper) {
        this.view = view;
        this.tableRepository = dataSetTableRepository;
        this.schedulerProvider = schedulerProvider;
        this.analyticsHelper = analyticsHelper;
        disposable = new CompositeDisposable();
    }

    @Override
    public void init(String orgUnitUid, String periodTypeName, String catCombo,
                     String periodFinalDate, String periodId) {
        this.orgUnitUid = orgUnitUid;
        this.periodTypeName = periodTypeName;
        this.periodFinalDate = periodFinalDate;
        this.catCombo = catCombo;
        this.periodId = periodId;

        disposable.add(
                tableRepository.getSections()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(view::setSections, Timber::e)
        );

        disposable.add(
                Flowable.zip(
                        tableRepository.getDataSet().toFlowable(),
                        tableRepository.getCatComboName(catCombo),
                        tableRepository.getPeriod().toFlowable(),
                        Trio::create
                )
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                data -> view.renderDetails(data.val0(), data.val1(), data.val2()),
                                Timber::e
                        )
        );

        disposable.add(
                view.observeSaveButtonClicks()
                        .subscribeOn(schedulerProvider.ui())
                        .toFlowable(BackpressureStrategy.LATEST)
                        .debounce(500, TimeUnit.MILLISECONDS, schedulerProvider.io())
                        .flatMapSingle(o -> tableRepository.checkMandatoryFields())
                        .flatMapSingle(missingMandatoryFields -> {
                            if (missingMandatoryFields.isEmpty()) {
                                return tableRepository.checkFieldCombination();
                            } else {
                                return Single.error(new Exception());
                            }
                        })
                        .flatMapSingle(missingCompleteDataElements -> {
                            if (missingCompleteDataElements.isEmpty()) {
                                return Single.just(true);
                            } else {
                                return Single.error(new Exception());
                            }
                        })
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.io())
                        .subscribe(
                                o -> {
                                    if (tableRepository.hasToRunValidationRules()) {
                                        checkValidationRules();
                                    } else {
                                        completeDataSet();
                                    }
                                },
                                Timber::e
                        )
        );
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

    public String getPeriodTypeName() {
        return periodTypeName;
    }

    public String getPeriodFinalDate() {
        return periodFinalDate;
    }

    public String getPeriodId() {
        return periodId;
    }

    public String getCatCombo() {
        return catCombo;
    }

    @VisibleForTesting
    public void checkValidationRules() {
        if (tableRepository.isValidationRuleOptional()) {
            view.showValidationRuleDialog();
        } else {
            executeValidationRules();
        }
    }

    @Override
    public void executeValidationRules() {
        disposable.add(
                tableRepository.executeValidationRules()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        result -> {
                            if (result.status() == ValidationResultStatus.OK) {
                                view.showSuccessValidationDialog();
                            } else {
                                List<ValidationResultViolation> violations = result.violations();
                                view.showErrorsValidationDialog();
                            }
                        },
                        Timber::e
                )
        );
    }

    @Override
    public void completeDataSet() {
        disposable.add(
                tableRepository.completeDataSetInstance()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                () -> {
                                },
                                Timber::e)
        );
    }
}
