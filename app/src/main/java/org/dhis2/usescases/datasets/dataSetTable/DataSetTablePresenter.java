package org.dhis2.usescases.datasets.dataSetTable;

import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.analytics.AnalyticsHelper;

import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.INFO_DATASET_TABLE;

public class DataSetTablePresenter implements DataSetTableContract.Presenter {

    private final DataSetTableRepository tableRepository;
    private final SchedulerProvider schedulerProvider;
    private final AnalyticsHelper analyticsHelper;
    DataSetTableContract.View view;
    public CompositeDisposable disposable;

    public DataSetTablePresenter(DataSetTableContract.View view,
                                 DataSetTableRepository dataSetTableRepository,
                                 SchedulerProvider schedulerProvider,
                                 AnalyticsHelper analyticsHelper) {
        this.view = view;
        this.tableRepository = dataSetTableRepository;
        this.schedulerProvider = schedulerProvider;
        this.analyticsHelper = analyticsHelper;
        disposable = new CompositeDisposable();
    }

    @Override
    public void init(String catCombo) {

        disposable.add(
                tableRepository.getSections()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(view::setSections, Timber::e)
        );

        disposable.add(
                Flowable.zip(
                        tableRepository.getDataSet(),
                        tableRepository.getCatComboName(catCombo),
                        Pair::create
                )
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                data -> view.renderDetails(data.val0(), data.val1()),
                                Timber::e
                        )
        );

        disposable.add(
                tableRepository.dataSetStatus()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(view::isDataSetOpen,
                                Timber::d
                        )
        );

        disposable.add(
                tableRepository.dataSetState()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(view::setDataSetState,
                                Timber::d
                        )
        );
    }

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void onSyncClick() {
        view.showSyncDialog();
    }

    @Override
    public void onDettach() {
        disposable.dispose();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }

    @Override
    public void optionsClick() {
        analyticsHelper.setEvent(INFO_DATASET_TABLE, CLICK, INFO_DATASET_TABLE);
        view.showOptions();
    }

    @Override
    public void onClickSelectTable(int numTable) {
        view.goToTable(numTable);
    }

    @Override
    public String getCatOptComboFromOptionList(List<String> catOpts) {
        return tableRepository.getCatOptComboFromOptionList(catOpts);
    }

    @Override
    public void updateState(){
        disposable.add(
                tableRepository.dataSetState()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(view::setDataSetState,
                                Timber::d
                        )
        );
    }

}
