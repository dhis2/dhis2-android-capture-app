package org.dhis2.usescases.datasets.dataSetTable;

import android.support.annotation.NonNull;

import org.dhis2.data.forms.dataentry.fields.FieldViewModel;
import org.dhis2.data.forms.dataentry.fields.FieldViewModelFactoryImpl;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Quintet;
import org.dhis2.usescases.datasets.dataSetTable.dataSetSection.DataSetSectionFragment;
import org.hisp.dhis.android.core.category.CategoryModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.dataset.SectionModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class DataSetTablePresenter implements DataSetTableContract.Presenter {

    private final DataSetTableRepository tableRepository;
    DataSetTableContract.View view;
    private CompositeDisposable compositeDisposable;
    private Pair<Map<String, List<DataElementModel>>, Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>>> tableData;
    //private List<DataSetTableModel> dataValues;
    private String orgUnitUid;
    private String periodTypeName;
    private String periodInitialDate;
    private String catCombo;
    private Map<String, List<DataElementModel>> dataElements;
    Map<String, List<List<CategoryOptionModel>>> catOptions;
    List<DataSetTableModel> dataValues;
    Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>> mapWithoutTransform;
    Map<String, Map<String, List<String>>> dataElementDisabled;
    Map<String, List<String>> compulsoryDataElement;
    List<SectionModel> sections;

    public DataSetTablePresenter(DataSetTableRepository dataSetTableRepository) {
        this.tableRepository = dataSetTableRepository;
    }

    @Override
    public void onBackClick() {
        view.back();
    }

    @Override
    public void init(DataSetTableContract.View view, String orgUnitUid, String periodTypeName, String periodInitialDate, String catCombo) {
        this.view = view;
        compositeDisposable = new CompositeDisposable();
        this.orgUnitUid = orgUnitUid;
        this.periodTypeName = periodTypeName;
        this.periodInitialDate = periodInitialDate;
        this.catCombo = catCombo;


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
    public void initializeProcessor(@NonNull DataSetSectionFragment dataSetSectionFragment){
        compositeDisposable.add(dataSetSectionFragment.rowActions()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rowAction -> {
                    boolean exists = false;
                    for(DataSetTableModel dataValue: dataValues){
                        if(dataValue.dataElement().equals(rowAction.dataElement()) &&
                                dataValue.listCategoryOption().containsAll(rowAction.listCategoryOption())){
                            DataSetTableModel dataSetTableModel = DataSetTableModel.create(dataValue.id(), dataValue.dataElement(),
                                    dataValue.period(), dataValue.organisationUnit(),
                                    dataValue.categoryOptionCombo(), dataValue.attributeOptionCombo(),
                                    rowAction.value(), dataValue.storedBy(),
                                    dataValue.catOption(), dataValue.listCategoryOption() );
                            dataValues.remove(dataValue);
                            dataValues.add(dataSetTableModel);
                            exists = true;
                            break;
                        }
                    }
                    if(!exists && rowAction.value() != null) {
                        DataSetTableModel dataSetTableModel = DataSetTableModel.create(Long.parseLong("0"), rowAction.dataElement(), periodTypeName, orgUnitUid,
                                "", catCombo, rowAction.value()!= null ? rowAction.value(): "", "",
                                "", rowAction.listCategoryOption());
                        dataValues.add(dataSetTableModel);
                    }
                    dataSetSectionFragment.updateData(rowAction);
                    },
                        Timber::e));
    }

    @Override
    public void getData(@NonNull DataSetSectionFragment dataSetSectionFragment, @Nullable String sectionUid) {

        compositeDisposable.add(
                tableRepository.getCatOptionCombo()
                        .flatMap(data ->
                                        Flowable.zip(
                                                tableRepository.getDataValues(orgUnitUid, periodTypeName, periodInitialDate, catCombo),
                                                tableRepository.getDataSet(),
                                                tableRepository.getGreyedFields(getUidCatOptionsCombo(data)),
                                                tableRepository.getMandatoryDataElement(getUidCatOptionsCombo(data)),
                                                tableRepository.getSectionByDataSet(),
                                                Quintet::create
                                        ))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                quintet -> {
                                    view.setDataValue(quintet.val0());
                                    view.setDataSet(quintet.val1());
                                    dataElements = tableData.val0();
                                    catOptions =  transformCategories(tableData.val1());
                                    dataValues = quintet.val0();
                                    mapWithoutTransform = tableData.val1();
                                    dataElementDisabled = quintet.val2();
                                    compulsoryDataElement = quintet.val3();
                                    sections = quintet.val4();
                                    dataSetSectionFragment.createTable(null);
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

    @Override
    public void onDettach() {
        compositeDisposable.dispose();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
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


    public Map<String, List<DataElementModel>> getDataElements() {
        return dataElements;
    }

    public Map<String, List<List<CategoryOptionModel>>> getCatOptions() {
        return catOptions;
    }

    public List<DataSetTableModel> getDataValues() {
        return dataValues;
    }

    public Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>> getMapWithoutTransform() {
        return mapWithoutTransform;
    }

    public Map<String, Map<String, List<String>>> getDataElementDisabled() {
        return dataElementDisabled;
    }

    public Map<String, List<String>> getCompulsoryDataElement() {
        return compulsoryDataElement;
    }

    public List<SectionModel> getSections() {
        return sections;
    }
}
