package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import androidx.annotation.NonNull;

import org.dhis2.R;
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModelFactoryImpl;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Sextet;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.dataelement.DataElementOperand;
import org.hisp.dhis.android.core.dataset.DataInputPeriod;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.dataset.Section;
import org.hisp.dhis.android.core.period.Period;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import timber.log.Timber;

import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.COMPLETE_REOPEN;

public class DataValuePresenter implements DataValueContract.Presenter {

    private final SchedulerProvider schedulerProvider;
    private String orgUnitUid;
    private String periodTypeName;
    private String attributeOptionCombo;

    private DataValueRepository repository;
    private DataValueContract.View view;
    private CompositeDisposable compositeDisposable;

    private List<DataSetTableModel> dataValuesChanged;
    private DataTableModel dataTableModel;
    private String periodId;
    private Period period;

    private List<List<List<FieldViewModel>>> tableCells;
    private List<DataInputPeriod> dataInputPeriodModel;
    @NonNull
    private FlowableProcessor<RowAction> processor;
    private FlowableProcessor<Trio<String, String, Integer>> processorOptionSet;
    private Boolean isApproval;
    private Boolean accessDataWrite;
    private String sectionName;
    private DataSet dataSet;
    private Section section;
    private List<List<CategoryOption>> catOptionOrder;
    private List<List<CategoryOption>> transformCategories;


    public DataValuePresenter(DataValueRepository repository, SchedulerProvider schedulerProvider) {
        this.repository = repository;
        this.schedulerProvider = schedulerProvider;
    }

    @Override
    public void init(DataValueContract.View view, String orgUnitUid, String periodTypeName, String periodFinalDate, String attributeOptionCombo, String sectionName, String periodId) {
        compositeDisposable = new CompositeDisposable();
        this.view = view;
        processor = PublishProcessor.create();
        processorOptionSet = PublishProcessor.create();
        dataValuesChanged = new ArrayList<>();
        this.tableCells = new ArrayList<>();
        this.orgUnitUid = orgUnitUid;
        this.periodTypeName = periodTypeName;
        this.attributeOptionCombo = attributeOptionCombo;
        this.periodId = periodId;
        this.sectionName = sectionName;
        this.accessDataWrite = true;

        compositeDisposable.add(
                repository.canWriteAny()
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe( accessDataWrite -> this.accessDataWrite = accessDataWrite,
                                Timber::e
                        ));

        compositeDisposable.add(repository.getDataSet()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(dataSet -> {
                    this.dataSet = dataSet;
                    view.setDataSet(dataSet);
                }, Timber::e)
        );

        compositeDisposable.add(repository.getSectionByDataSet(sectionName)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(section -> {
                    this.section = section;
                    view.setSection(section);
                }, Timber::e)
        );

        compositeDisposable.add(
                Flowable.zip(
                        repository.getPeriod(periodId),
                        repository.getDataInputPeriod(),
                        repository.isApproval(orgUnitUid, periodId, attributeOptionCombo),
                        Trio::create
                )

                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                data -> {
                                    period = data.val0();
                                    dataInputPeriodModel = data.val1();
                                    isApproval = data.val2();
                                }
                                ,
                                Timber::e)
        );

        compositeDisposable.add(repository.getCatCombo(sectionName).map(List::size)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(view::updateTabLayout, Timber::e)
        );

        compositeDisposable.add(repository.getCatCombo(sectionName)
                .flatMapIterable(categoryCombos -> categoryCombos)
                .map(categoryCombo -> Flowable.zip(
                        Flowable.just(categoryCombo),
                        repository.getDataElements(categoryCombo, sectionName),
                        repository.getCatOptions(sectionName, categoryCombo.uid()),
                        repository.getDataValues(orgUnitUid, periodTypeName, periodId, attributeOptionCombo, sectionName),
                        repository.getGreyFields(sectionName),
                        repository.getCompulsoryDataElements(),
                        Sextet::create
                ).toObservable().blockingFirst())
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        data -> {
                            List<List<String>> options = new ArrayList<>();
                            for (Map.Entry<String, List<List<Pair<CategoryOption, Category>>>> map : data.val2().entrySet()) {
                                options = getCatOptionCombos(map.getValue(), 0, new ArrayList<>(), null);
                            }
                            transformCategories = new ArrayList<>();
                            catOptionOrder = getCatOptionOrder(options);
                            for (Map.Entry<String, List<List<CategoryOption>>> map : transformCategories(data.val2()).entrySet()) {
                                transformCategories.addAll(map.getValue());
                            }

                            dataTableModel = DataTableModel.create(
                                    data.val1(), data.val3(), data.val4(),
                                    data.val5(), data.val0(), transformCategories);

                            setTableData(dataTableModel);
                        },
                        Timber::e
                )
        );
        compositeDisposable.add(
                repository.isCompleted(orgUnitUid, periodId, attributeOptionCombo)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                data -> view.setCompleteReopenText(data),
                                Timber::e
                        )
        );


    }

    private List<List<CategoryOption>> getCatOptionOrder(List<List<String>> options) {
        List<List<CategoryOption>> list = new ArrayList<>();
        for (List<String> combo : options) {
            List<CategoryOption> categoryOptions = new ArrayList<>();
            for (String option : combo) {
                categoryOptions.add(repository.getCatOptionFromUid(option));
            }
            list.add(categoryOptions);
        }
        return list;
    }

    private List<CategoryOptionCombo> getCatOptionComboOrder(List<CategoryOptionCombo> catOptionCombos) {
        List<CategoryOptionCombo> categoryOptionCombosOrder = new ArrayList<>();
        for (List<CategoryOption> catOptions : catOptionOrder) {
            for (CategoryOptionCombo categoryOptionCombo : catOptionCombos) {
                if (catOptions.containsAll(repository.getCatOptionFromCatOptionCombo(categoryOptionCombo))) {
                    categoryOptionCombosOrder.add(categoryOptionCombo);
                }
            }
        }
        return categoryOptionCombosOrder;
    }


    private void setTableData(DataTableModel dataTableModel) {
        ArrayList<List<String>> cells = new ArrayList<>();
        List<List<FieldViewModel>> listFields = new ArrayList<>();
        int row = 0, column = 0;
        boolean isNumber = false;


        for (DataElement dataElement : dataTableModel.rows()) {

            ArrayList<String> values = new ArrayList<>();
            ArrayList<FieldViewModel> fields = new ArrayList<>();
            int totalRow = 0;
            isNumber = dataElement.valueType() == ValueType.NUMBER || dataElement.valueType() == ValueType.INTEGER;
            FieldViewModelFactoryImpl fieldFactory = new FieldViewModelFactoryImpl("", "");

            for (CategoryOptionCombo categoryOptionCombo : getCatOptionComboOrder(dataTableModel.catCombo().categoryOptionCombos())) {

                boolean editable = true;
                for (DataElementOperand disabledDataElement : dataTableModel.dataElementDisabled())
                    if ((disabledDataElement.categoryOptionCombo() != null &&
                            (disabledDataElement.categoryOptionCombo().uid().equals(categoryOptionCombo.uid()) &&
                                    disabledDataElement.dataElement().uid().equals(dataElement.uid()))) ||
                         disabledDataElement.dataElement().uid().equals(dataElement.uid()))
                        editable = false;

                for (CategoryOption categoryOption : repository.getCatOptionFromCatOptionCombo(categoryOptionCombo))
                    if (!categoryOption.access().data().write())
                        editable = false;

                FieldViewModel fieldViewModel = null;
                for (DataSetTableModel dataValue : dataTableModel.dataValues())
                    if (dataValue.dataElement().equals(dataElement.uid()) && dataValue.categoryOptionCombo().equals(categoryOptionCombo.uid())) {

                        fieldViewModel = fieldFactory.create(dataValue.id().toString(), dataElement.displayFormName(), dataElement.valueType(),
                                false, dataElement.optionSetUid(), dataValue.value(), sectionName, true,
                                editable, null, categoryOptionCombo.displayName(), dataElement.uid(), new ArrayList<>(), "android",
                                row, column, dataValue.categoryOptionCombo(), dataValue.catCombo());
                    }


                if (fieldViewModel == null)
                    fieldViewModel = fieldFactory.create("", dataElement.displayFormName(), dataElement.valueType(),
                            false, dataElement.optionSetUid(), "", sectionName, true,
                            editable, null, categoryOptionCombo.displayName(), dataElement.uid(), new ArrayList<>(),
                            "android", row, column, categoryOptionCombo.uid(), dataTableModel.catCombo().uid());

                fields.add(fieldViewModel);
                values.add(fieldViewModel.value());

                if (!section.uid().isEmpty() && section.showRowTotals() && isNumber && !fieldViewModel.value().isEmpty()) {
                    totalRow += Integer.parseInt(fieldViewModel.value());
                }

                column++;
            }

            for (FieldViewModel fieldViewModel : fields)
                for (DataElementOperand compulsoryDataElement : dataTableModel.compulsoryCells())
                    if (compulsoryDataElement.categoryOptionCombo().uid().equals(fieldViewModel.categoryOptionCombo()) &&
                            compulsoryDataElement.dataElement().uid().equals(fieldViewModel.dataElement()))
                        fields.set(fields.indexOf(fieldViewModel), fieldViewModel.setMandatory());


            if (!section.uid().isEmpty() && section.showRowTotals() && isNumber) {
                setTotalRow(totalRow, fields, values, row, column);
            }

            listFields.add(fields);
            cells.add(values);
            column = 0;
            row++;

        }

        if (isNumber) {
            if (!section.uid().isEmpty() && section.showColumnTotals())
                setTotalColumn(listFields, cells, dataTableModel.rows(), row, column);
            if (!section.uid().isEmpty() && section.showRowTotals())
                for (int i = 0; i < dataTableModel.header().size(); i++) {
                    if (i == dataTableModel.header().size() - 1)
                        dataTableModel.header().get(i).add(CategoryOption.builder().uid("").displayName("Total").build());
                    else
                        dataTableModel.header().get(i).add(CategoryOption.builder().uid("").displayName("").build());
                }
        }

        boolean isEditable = accessDataWrite
                && !isExpired(dataSet)
                && dataInputPeriodModel.size() == 0 || checkHasInputPeriod() != null && DateUtils.getInstance().isInsideInputPeriod(checkHasInputPeriod())
                && !isApproval;

        view.setTableData(dataTableModel, listFields, cells, isEditable);
    }

    private Boolean isExpired(DataSet dataSet) {

        if (0 == dataSet.expiryDays()) {
            return false;
        }
        return DateUtils.getInstance().isDataSetExpired(dataSet.expiryDays(), period.endDate());
    }

    private void setTotalRow(int totalRow, ArrayList<FieldViewModel> fields, ArrayList<String> values, int row, int column) {
        FieldViewModelFactoryImpl fieldFactory = new FieldViewModelFactoryImpl(
                "",
                "");
        fields.add(fieldFactory.create("", "", ValueType.INTEGER,
                false, "", String.valueOf(totalRow), sectionName, true,
                false, null, null, "", new ArrayList<>(), "", row, column, "", ""));
        values.add(String.valueOf(totalRow));

    }

    private void setTotalColumn(List<List<FieldViewModel>> listFields, ArrayList<List<String>> cells,
                                List<DataElement> dataElements, int row, int columnPos) {
        FieldViewModelFactoryImpl fieldFactory = new FieldViewModelFactoryImpl(
                "",
                "");

        ArrayList<FieldViewModel> fields = new ArrayList<>();
        ArrayList<String> values = new ArrayList<>();
        boolean existTotal = false;
        for (DataElement data : dataElements)
            if (data.displayName().equals("Total"))
                existTotal = true;

        if (existTotal) {
            listFields.remove(listFields.size() - 1);
            cells.remove(listFields.size() - 1);
        }


        int[] totals = new int[cells.get(0).size()];
        for (List<String> dataValues : cells) {
            for (int i = 0; i < dataValues.size(); i++) {
                if (!dataValues.get(i).isEmpty())
                    totals[i] += Integer.parseInt(dataValues.get(i));
            }
        }

        for (int column : totals) {
            fields.add(fieldFactory.create("", "", ValueType.INTEGER,
                    false, "", String.valueOf(column), sectionName, true,
                    false, null, null, "", new ArrayList<>(), "", row, columnPos, "", ""));

            values.add(String.valueOf(column));
        }


        listFields.add(fields);
        cells.add(values);

        if (!existTotal)
            dataElements.add(DataElement.builder().uid("").displayName("Total").valueType(ValueType.INTEGER).build());
    }

    @Override
    public void complete() {
        if (!isApproval) {
            view.analyticsHelper().setEvent(COMPLETE_REOPEN, CLICK, COMPLETE_REOPEN);
            if (view.isOpenOrReopen()) {
                if (((!dataSet.fieldCombinationRequired()) || checkAllFieldRequired() && dataSet.fieldCombinationRequired())
                        && checkMandatoryField())
                    compositeDisposable.add(
                            repository.completeDataSet(orgUnitUid, periodId, attributeOptionCombo)
                                    .subscribeOn(schedulerProvider.io())
                                    .observeOn(schedulerProvider.ui())
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
                                .subscribeOn(schedulerProvider.io())
                                .observeOn(schedulerProvider.ui())
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

                    dataSetSectionFragment.updateData(rowAction, dataSetTableModel.catCombo());
                    return repository.updateValue(dataSetTableModel).toFlowable();
                })
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(o -> view.showSnackBar(), Timber::e));
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

    public DataInputPeriod checkHasInputPeriod() {
        DataInputPeriod inputPeriodModel = null;
        for (DataInputPeriod inputPeriod : dataInputPeriodModel) {
            if (inputPeriod.period().equals(periodId))
                inputPeriodModel = inputPeriod;
        }
        return inputPeriodModel;
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

}
