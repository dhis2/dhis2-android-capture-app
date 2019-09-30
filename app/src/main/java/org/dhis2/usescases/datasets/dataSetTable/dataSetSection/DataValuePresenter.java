package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import androidx.annotation.NonNull;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Quintet;
import org.dhis2.data.tuples.Sextet;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.dataset.DataInputPeriod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class DataValuePresenter implements DataValueContract.Presenter {

    private String orgUnitUid;
    private String periodTypeName;
    private String periodFinalDate;
    private String attributeOptionCombo;

    private Trio<List<DataElement>, Map<String, List<List<Pair<CategoryOption, Category>>>>, List<CategoryCombo>> tableData;

    private DataValueRepository repository;
    private DataValueContract.View view;
    private CompositeDisposable compositeDisposable;

    private List<DataSetTableModel> dataValuesChanged;
    private DataTableModel dataTableModel;
    private String periodId;
    private List<String> tablesNames;

    private List<List<List<FieldViewModel>>> tableCells;
    private List<DataInputPeriod> dataInputPeriodModel;
    @NonNull
    private FlowableProcessor<RowAction> processor;
    private FlowableProcessor<Trio<String, String, Integer>> processorOptionSet;
    private Boolean isApproval;

    public DataValuePresenter(DataValueRepository repository) {
        this.repository = repository;
    }

    @Override
    public void init(DataValueContract.View view, String orgUnitUid, String periodTypeName, String periodFinalDate, String attributeOptionCombo, String section, String periodId) {
        compositeDisposable = new CompositeDisposable();
        this.view = view;
        processor = PublishProcessor.create();
        processorOptionSet = PublishProcessor.create();
        dataValuesChanged = new ArrayList<>();
        this.tableCells = new ArrayList<>();
        this.orgUnitUid = orgUnitUid;
        this.periodTypeName = periodTypeName;
        this.periodFinalDate = periodFinalDate;
        this.attributeOptionCombo = attributeOptionCombo;
        this.periodId = periodId;


        compositeDisposable.add(
                Flowable.zip(
                        repository.getPeriod(periodId),
                        repository.getDataInputPeriod(),
                        Pair::create
                )

                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> {
                                    view.setPeriod(data.val0());
                                    dataInputPeriodModel = data.val1();
                                }
                                ,
                                Timber::e)
        );
    }

    @Override
    public void complete() {
        if (!isApproval) {
            if (view.isOpenOrReopen()) {
                if (((!dataTableModel.dataSet().fieldCombinationRequired()) || checkAllFieldRequired() && dataTableModel.dataSet().fieldCombinationRequired())
                        && checkMandatoryField())
                    compositeDisposable.add(
                            repository.completeDataSet(orgUnitUid, periodId, attributeOptionCombo)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(completed -> {
                                        view.setCompleteReopenText(true);
                                        view.update(completed);
                                    }, Timber::e)
                    );
                else if (!checkMandatoryField())
                    view.showAlertDialog(view.getContext().getString(R.string.missing_mandatory_fields_title), view.getContext().getResources().getString(R.string.field_mandatory));
                else
                    view.showAlertDialog(view.getContext().getString(R.string.missing_mandatory_fields_title), view.getContext().getResources().getString(R.string.field_required));
            } else {
                view.isOpenOrReopen();
                compositeDisposable.add(
                        repository.reopenDataSet(orgUnitUid, periodId, attributeOptionCombo)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(reopen -> {
                                            view.setCompleteReopenText(false);
                                            view.update(reopen);
                                        },
                                        Timber::e));
            }
        }
    }

    private boolean checkAllFieldRequired() {
        boolean checkAllField = true;
        for (int i = 0; i < tableCells.size(); i++) {
            for (List<FieldViewModel> rowFields : tableCells.get(i)) {
                boolean hasValue = false;
                for (FieldViewModel field : rowFields) {
                    if (field.value() != null && !field.value().isEmpty())
                        hasValue = true;

                    if (hasValue && field.editable() && (field.value() == null || field.value().isEmpty())) {
                        checkAllField = false;
                        view.highligthHeaderRow(i, tableCells.get(i).indexOf(rowFields), false);
                    }
                }
            }
        }
        return checkAllField;
    }

    private boolean checkMandatoryField() {
        boolean mandatoryOk = true;
        for (int i = 0; i < tableCells.size(); i++) {
            for (List<FieldViewModel> rowFields : tableCells.get(i)) {
                for (FieldViewModel field : rowFields) {
                    if (field.editable() && field.mandatory() && (field.value() == null || field.value().isEmpty())) {
                        mandatoryOk = false;
                        view.highligthHeaderRow(i, tableCells.get(i).indexOf(rowFields), true);
                    }
                }
            }
        }
        return mandatoryOk;
    }

    @Override
    public void onDettach() {
    }

    @Override
    public void displayMessage(String message) {

    }

    @Override
    public void initializeProcessor(@NonNull DataSetSectionFragment dataSetSectionFragment) {

        compositeDisposable.add(dataSetSectionFragment.rowActions()
                .flatMap(rowAction -> {
                    dataValuesChanged.clear();

                    DataSetTableModel dataSetTableModel = null;

                    for (DataSetTableModel dataValue : dataTableModel.dataValues()) {
                        if (dataValue.dataElement().equals(rowAction.dataElement()) && dataValue.categoryOptionCombo().equals(rowAction.catOptCombo())) {
                            dataSetTableModel = dataValue.setValue(rowAction.value());
                        }
                    }

                    if (dataSetTableModel == null && rowAction.value() != null && !rowAction.value().isEmpty()) {
                        dataSetTableModel = DataSetTableModel.create(Long.parseLong("0"), rowAction.dataElement(), periodId, orgUnitUid,
                                rowAction.catOptCombo(), attributeOptionCombo, rowAction.value(), "",
                                "", rowAction.listCategoryOption(), rowAction.catCombo());

                        dataTableModel.dataValues().add(dataSetTableModel);
                    }

                    return repository.updateValue(dataSetTableModel).toFlowable();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> view.showSnackBar(), Timber::e));
    }

    @Override
    public void getData(@NonNull DataSetSectionFragment dataSetSectionFragment, @Nullable String section) {
        compositeDisposable.add(
                repository.isCompleted(orgUnitUid, periodId, attributeOptionCombo)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> view.setCompleteReopenText(data),
                                Timber::e
                        )
        );

        compositeDisposable.add(
                Flowable.zip(
                        repository.getCatOptionCombo(),
                        repository.getDataElements(section),
                        repository.getCatOptions(section),
                        repository.getCatCombo(section),
                        repository.isApproval(orgUnitUid, periodId, attributeOptionCombo),
                        Quintet::create
                )
                        .flatMap(data -> {
                            tableData = Trio.create(data.val1(), data.val2(), new LinkedList<>(data.val3()));
                            isApproval = data.val4();
                            return Flowable.zip(
                                    repository.getDataValues(orgUnitUid, periodTypeName, periodId, attributeOptionCombo, section),
                                    repository.getDataSet(),
                                    repository.getGreyedFields(getUidCatOptionsCombo(data.val0()), section),
                                    repository.getMandatoryDataElement(getUidCatOptionsCombo(data.val0())),
                                    repository.getSectionByDataSet(section),
                                    repository.getCategoryOptionComboCatOption(),
                                    Sextet::create
                            );})
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                sextet -> {
                                    if (tableData != null) {
                                        dataTableModel = DataTableModel
                                                .create(sextet.val4().id() == null ? null : sextet.val4(), transformCategories(tableData.val1()),
                                                        tableData.val0(), sextet.val0(), getCatOptionsByCatOptionComboDataElement(sextet.val2()),
                                                        sextet.val3(), sextet.val5(), tableData.val1(), sextet.val1(),
                                                        getCatCombos(tableData.val0(), tableData.val2()), getCatOptions(), isApproval);

                                        dataSetSectionFragment.createTable(dataTableModel);
                                    }
                                },
                                Timber::e
                        )
        );
    }

    public Map<String, String> getCatCombos(List<DataElement> dataElements, List<CategoryCombo> catCombos) {
        Map<String, String> list = new HashMap<>();
        for (DataElement dataElement : dataElements) {
            if (!list.keySet().contains(dataElement.categoryCombo())) {
                for (CategoryCombo categoryCombo : catCombos)
                    if (categoryCombo.uid().equals(dataElement.categoryCombo().uid()))
                        list.put(categoryCombo.uid(), categoryCombo.name());
            }
        }
        return list;
    }

    public List<Pair<String, List<String>>> getCatOptionsByCatOptionComboDataElement(Map<String, Map<String, List<String>>> map) {
        List<Pair<String, List<String>>> list = new ArrayList<>();

        for (Map.Entry<String, Map<String, List<String>>> entryDataElement : map.entrySet()) {
            for (Map.Entry<String, List<String>> combination : entryDataElement.getValue().entrySet()) {
                List<String> catOptions = new ArrayList<>();
                for (String option : combination.getValue()) {
                    catOptions.add(option);
                    list.add(Pair.create(entryDataElement.getKey(), catOptions));
                }
            }
        }
        return list;
    }

    public List<CategoryOption> getCatOptions() {
        Map<String, List<List<Pair<CategoryOption, Category>>>> map = tableData.val1();
        List<CategoryOption> listCatOption = new ArrayList<>();
        for (Map.Entry<String, List<List<Pair<CategoryOption, Category>>>> entry : map.entrySet()) {
            for (List<Pair<CategoryOption, Category>> list : entry.getValue()) {
                for (Pair<CategoryOption, Category> listPair : list)
                    listCatOption.add(listPair.val0());
            }
        }
        return listCatOption;
    }

    @Override
    public Map<String, List<List<CategoryOption>>> transformCategories(@NonNull Map<String, List<List<Pair<CategoryOption, Category>>>> map) {
        Map<String, List<List<CategoryOption>>> mapTransform = new HashMap<>();
        for (Map.Entry<String, List<List<Pair<CategoryOption, Category>>>> entry : map.entrySet()) {
            mapTransform.put(entry.getKey(), new ArrayList<>());
            int repeat = 1;
            int nextCategory = 0;
            for (List<Pair<CategoryOption, Category>> list : map.get(entry.getKey())) {
                List<CategoryOption> catOptions = new ArrayList<>();
                for (int x = 0; x < repeat; x++) {
                    for (Pair<CategoryOption, Category> pair : list) {
                        catOptions.add(pair.val0());
                        nextCategory++;
                    }
                }
                repeat = nextCategory;
                nextCategory = 0;
                mapTransform.get(entry.getKey()).add(catOptions);
            }


        }
        return mapTransform;
    }

    @Override
    public List<List<String>> getCatOptionCombos(List<List<Pair<CategoryOption, Category>>> listCategories, int num, List<List<String>> result, List<String> current) {
        if (num == listCategories.size()) {
            List<String> resultHelp = new ArrayList<>();
            for (String option : current)
                resultHelp.add(option);
            result.add(resultHelp);
            return result;
        }
        for (int i = 0; i < listCategories.get(num).size(); i++) {
            if (num == 0)
                current = new ArrayList<>();
            if (current.size() == num + 1)
                current.remove(current.size() - 1);
            current.add(listCategories.get(num).get(i).val0().uid());
            getCatOptionCombos(listCategories, num + 1, result, current);
        }

        return result;
    }

    private List<String> getUidCatOptionsCombo(Map<String, List<CategoryOptionCombo>> map) {
        List<String> catOptionsCombo = new ArrayList<>();

        for (Map.Entry<String, List<CategoryOptionCombo>> entry : map.entrySet()) {
            for (CategoryOptionCombo category : entry.getValue()) {
                catOptionsCombo.add(category.uid());
            }
        }

        return catOptionsCombo;
    }

    public DataInputPeriod checkHasInputPeriod() {
        DataInputPeriod inputPeriodModel = null;
        for (DataInputPeriod inputPeriod : dataInputPeriodModel) {
            if (inputPeriod.period().equals(periodId))
                inputPeriodModel = inputPeriod;
        }
        return inputPeriodModel;
    }

    @Override
    public void addCells(int table, List<List<FieldViewModel>> cells) {
        this.tableCells.add(table, cells);
    }

    @Override
    public void setCurrentNumTables(List<String> tablesNames) {
        this.tablesNames = tablesNames;
    }

    @Override
    public List<String> getCurrentNumTables() {
        return tablesNames;
    }

    @Override
    @NonNull
    public FlowableProcessor<RowAction> getProcessor() {
        return processor;
    }

    @Override
    public FlowableProcessor<Trio<String, String, Integer>> getProcessorOptionSet() {
        return processorOptionSet;
    }

    public List<DataInputPeriod> getDataInputPeriodModel() {
        return dataInputPeriodModel;
    }

}
