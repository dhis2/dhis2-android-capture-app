package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;


import com.google.android.material.snackbar.Snackbar;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Quintet;
import org.dhis2.data.tuples.Sextet;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.hisp.dhis.android.core.category.CategoryModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.dataset.SectionModel;
import org.hisp.dhis.android.core.datavalue.DataValueModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import androidx.annotation.NonNull;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class DataValuePresenter implements DataValueContract.Presenter{

    private String orgUnitUid;
    private String periodTypeName;
    private String periodFinalDate;
    private String catCombo;

    private Pair<Map<String, List<DataElementModel>>, Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>>> tableData;

    private DataValueRepository repository;
    private DataValueContract.View view;
    private CompositeDisposable compositeDisposable;

    private List<DataSetTableModel> dataValuesChanged;
    private DataTableModel dataTableModel;

    public DataValuePresenter(DataValueRepository repository){
        this.repository = repository;
    }

    @Override
    public void init(DataValueContract.View view, String orgUnitUid, String periodTypeName, String periodFinalDate, String catCombo) {
        compositeDisposable = new CompositeDisposable();
        this.view = view;
        dataValuesChanged = new ArrayList<>();
        this.orgUnitUid = orgUnitUid;
        this.periodTypeName = periodTypeName;
        this.periodFinalDate = periodFinalDate;
        this.catCombo = catCombo;

        compositeDisposable.add(
                Flowable.zip(
                        repository.getDataElements(),
                        repository.getCatOptions(),
                        Pair::create
                )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> this.tableData = data,
                                Timber::e
                        )
        );

        if(periodFinalDate == null)
            compositeDisposable.add(
                    Flowable.zip(
                            repository.getPeriod(periodTypeName),
                            repository.getDataInputPeriod(periodTypeName),
                            Pair::create
                    )

                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    data ->{
                                        view.setPeriod(data.val0());
                                        view.setDataInputPeriod(data.val1());
                                    }
                                    ,
                                    Timber::e)
            );
    }

    @Override
    public void save() {
        compositeDisposable.add(
                repository.insertDataValue(tranformDataSetTableModelToDataValueModel())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe( aLong -> view.showSnackBar(), Timber::e)
        );
    }

    private List<DataValueModel> tranformDataSetTableModelToDataValueModel(){
        List<DataValueModel> listDataValue = new ArrayList<>();
        Date currentDate = Calendar.getInstance().getTime();
        for(DataSetTableModel dataSetTableModel: dataValuesChanged){
            listDataValue.add(DataValueModel.builder()
                    .dataElement(dataSetTableModel.dataElement())
                    .period(dataSetTableModel.period())
                    .organisationUnit(dataSetTableModel.organisationUnit())
                    .categoryOptionCombo(dataSetTableModel.categoryOptionCombo())
                    .attributeOptionCombo(dataSetTableModel.attributeOptionCombo())
                    .value(dataSetTableModel.value())
                    .storedBy(dataSetTableModel.storedBy())
                    .created(currentDate)
                    .lastUpdated(currentDate)
                    .comment("")
                    .followUp(false).build());
        }

        return listDataValue;
    }

    @Override
    public void onDettach() {

    }

    @Override
    public void displayMessage(String message) {

    }

    @Override
    public void initializeProcessor(@NonNull DataSetSectionFragment dataSetSectionFragment){
        compositeDisposable.add(dataSetSectionFragment.rowActions()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rowAction -> {
                            boolean exists = false;
                            if(rowAction.value() != null && !rowAction.value().isEmpty()) {
                                DataSetTableModel dataSetTableModel = null;
                                for (DataSetTableModel dataValue : dataTableModel.dataValues()) {
                                    if (dataValue.dataElement().equals(rowAction.dataElement()) &&
                                            dataValue.listCategoryOption().containsAll(rowAction.listCategoryOption())) {
                                        dataSetTableModel = DataSetTableModel.create(dataValue.id(), dataValue.dataElement(),
                                                dataValue.period(), dataValue.organisationUnit(),
                                                dataValue.categoryOptionCombo(), dataValue.attributeOptionCombo(),
                                                rowAction.value(), dataValue.storedBy(),
                                                dataValue.catOption(), dataValue.listCategoryOption());
                                        dataTableModel.dataValues().remove(dataValue);
                                        dataTableModel.dataValues().add(dataSetTableModel);
                                        exists = true;
                                        break;
                                    }
                                }
                                if (!exists && rowAction.value() != null) {
                                    String catOptionCombo = "";
                                    for (Map.Entry<String, List<String>> entry : dataTableModel.catOptionComboCatOption().entrySet()) {
                                        if (entry.getValue().containsAll(rowAction.listCategoryOption()))
                                            catOptionCombo = entry.getKey();
                                    }
                                    dataSetTableModel = DataSetTableModel.create(Long.parseLong("0"), rowAction.dataElement(), periodTypeName, orgUnitUid,
                                            catOptionCombo, catCombo, rowAction.value() != null ? rowAction.value() : "", "",
                                            "", rowAction.listCategoryOption());
                                    dataTableModel.dataValues().add(dataSetTableModel);
                                }
                                dataValuesChanged.add(dataSetTableModel);
                                dataSetSectionFragment.updateData(rowAction);
                            }
                        },
                        Timber::e));
    }

    @Override
    public void getData(@NonNull DataSetSectionFragment dataSetSectionFragment, @Nullable String section) {

        compositeDisposable.add(
                repository.getCatOptionCombo()
                        .flatMap(data ->
                                Flowable.zip(
                                        repository.getDataValues(orgUnitUid, periodTypeName, periodFinalDate, catCombo),
                                        repository.getDataSet(),
                                        repository.getGreyedFields(getUidCatOptionsCombo(data)),
                                        repository.getMandatoryDataElement(getUidCatOptionsCombo(data)),
                                        repository.getSectionByDataSet(),
                                        repository.getCategoryOptionComboCatOption(),
                                        Sextet::create
                                ))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                sextet -> {
                                    dataTableModel = DataTableModel
                                            .create(sextet.val4(), transformCategories(tableData.val1()),
                                                    tableData.val0(), sextet.val0(), sextet.val2(), sextet.val3(), sextet.val5(), tableData.val1(), sextet.val1());

                                    dataSetSectionFragment.createTable(dataTableModel);
                                },
                                Timber::e
                        )
        );
    }

    @Override
    public Map<String, List<List<CategoryOptionModel>>> transformCategories(@NonNull Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>> map) {
        Map<String, List<List<CategoryOptionModel>>> mapTransform = new HashMap<>();
        for (Map.Entry<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>> entry : map.entrySet()) {
            mapTransform.put(entry.getKey(), new ArrayList<>());
            int repeat = 0;
            for (List<Pair<CategoryOptionModel, CategoryModel>> list : map.get(entry.getKey())) {
                repeat++;
                List<CategoryOptionModel> catOptions = new ArrayList<>();
                for (int x = 0; x < repeat; x++) {
                    for (Pair<CategoryOptionModel, CategoryModel> pair : list) {
                        catOptions.add(pair.val0());
                    }
                }
                mapTransform.get(entry.getKey()).add(catOptions);
            }


        }
        return mapTransform;
    }

    @Override
    public List<FieldViewModel> transformToFieldViewModels(List<DataSetTableModel> dataValues) {
        List<FieldViewModel> listFields = new ArrayList<>();
        for (DataSetTableModel datavalue : dataValues) {
            FieldViewModelFactoryImpl fieldFactory = new FieldViewModelFactoryImpl(
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "");

            /*listFields.add(fieldFactory.create(datavalue.id(), "", datavalue.,
                    mandatory, optionSetUid, dataValue, section, allowFutureDates,
                    status == EventStatus.ACTIVE, null, description));*/
        }
        return null;
    }

    @Override
    public List<List<String>> getCatOptionCombos(List<List<Pair<CategoryOptionModel, CategoryModel>>> listCategories, int num, List<List<String>> result, List<String> current) {
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

    private List<String> getUidCatOptionsCombo(Map<String, List<CategoryOptionComboModel>> map) {
        List<String> catOptionsCombo = new ArrayList<>();

        for (Map.Entry<String, List<CategoryOptionComboModel>> entry : map.entrySet()) {
            for (CategoryOptionComboModel category : entry.getValue()) {
                catOptionsCombo.add("'" + category.uid() + "'");
            }
        }

        return catOptionsCombo;
    }

}
