package org.dhis2.usescases.datasets.dataSetTable;

import org.dhis2.data.tuples.Pair;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.dataelement.DataElement;

import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class DataSetTablePresenterImpl implements DataSetTableContract.DataSetTablePresenter {

    private final DataSetTableRepository tableRepository;
    private DataSetTableContract.DataSetTableView view;
    private CompositeDisposable compositeDisposable;
    private Pair<Map<String, List<DataElement>>, Map<String, List<CategoryOptionComboModel>>> tableData;

    public DataSetTablePresenterImpl(DataSetTableRepository dataSetTableRepository) {
        this.tableRepository = dataSetTableRepository;
    }

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void init(DataSetTableContract.DataSetTableView view, String orgUnitUid, String periodTypeName, String periodInitialDate, String catCombo) {
        this.view = view;
        compositeDisposable = new CompositeDisposable();

        compositeDisposable.add(
                tableRepository.getDataValues(orgUnitUid, periodTypeName, periodInitialDate, catCombo)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> Timber.d("VALUES LIST SIZE = %d", data.size()),
                                Timber::e
                        )
        );

        compositeDisposable.add(
                tableRepository.getDataSet()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view::setDataSet,
                                Timber::e
                        )
        );

        compositeDisposable.add(
                Flowable.zip(
                        tableRepository.getDataElements(),
                        tableRepository.getCatOptions(),
                        Pair::create
                )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> {
                                    this.tableData = data;
                                    view.setDataElements(data.val0(), data.val1());
                                },
                                Timber::e
                        )
        );

    }

    @Override
    public List<DataElement> getDataElements(String key) {
        return tableData.val0().get(key);
    }

    @Override
    public List<CategoryOptionComboModel> getCatOptionCombos(String key) {
        return tableData.val1().get(tableData.val0().get(key).get(0).categoryCombo());
    }

    @Override
    public void onDettach() {
        compositeDisposable.dispose();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }


}
