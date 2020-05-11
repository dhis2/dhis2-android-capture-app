package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import androidx.annotation.VisibleForTesting
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function6
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.R
import org.dhis2.data.forms.dataentry.StoreResult
import org.dhis2.data.forms.dataentry.ValueStore
import org.dhis2.data.forms.dataentry.ValueStoreImpl
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModelFactoryImpl
import org.dhis2.data.forms.dataentry.tablefields.RowAction
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.tuples.Pair
import org.dhis2.data.tuples.Quartet
import org.dhis2.data.tuples.Sextet
import org.dhis2.data.tuples.Trio
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableActivity
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel
import org.dhis2.utils.DateUtils
import org.dhis2.utils.analytics.AnalyticsHelper
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.COMPLETE_REOPEN
import org.hisp.dhis.android.core.category.Category
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.dataelement.DataElementOperand
import org.hisp.dhis.android.core.dataset.DataInputPeriod
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.dataset.Section
import org.hisp.dhis.android.core.period.Period
import timber.log.Timber
import java.util.ArrayList
import java.util.HashMap

class DataValuePresenter(
    private val view: DataValueContract.View,
    private val repository: DataValueRepository,
    private val valueStore: ValueStore,
    private val schedulerProvider: SchedulerProvider,
    private val analyticsHelper: AnalyticsHelper,
    private val prefs: PreferenceProvider,
    private val dataSetUid: String
) {

    var disposable: CompositeDisposable = CompositeDisposable()

    private var orgUnitUid: String? = null
    private var periodTypeName: String? = null
    private var attributeOptionCombo: String? = null

    private var dataValuesChanged: MutableList<DataSetTableModel>? = null
    private var dataTableModel: DataTableModel? = null
    private var periodId: String? = null
    private var period: Period? = null

    private lateinit var tableCells: MutableList<List<List<FieldViewModel>>>
    private lateinit var dataInputPeriodModel: List<DataInputPeriod>
    private lateinit var processor: FlowableProcessor<RowAction>
    private var processorOptionSet: FlowableProcessor<Trio<String, String, Int>>? = null
    private var isApproval: Boolean = false
    private var accessDataWrite: Boolean = true
    private var sectionName: String? = null
    private var dataSet: DataSet? = null
    private var section: Section? = null
    private var catOptionOrder: List<List<CategoryOption>>? = null
    private var transformCategories: MutableList<List<CategoryOption>>? = null

    fun init(
        view: DataValueContract.View,
        orgUnitUid: String,
        periodTypeName: String,
        periodFinalDate: String,
        attributeOptionCombo: String,
        sectionName: String,
        periodId: String
    ) {
        processor = PublishProcessor.create()
        processorOptionSet = PublishProcessor.create()
        dataValuesChanged = ArrayList()
        this.tableCells = mutableListOf()
        this.orgUnitUid = orgUnitUid
        this.periodTypeName = periodTypeName
        this.attributeOptionCombo = attributeOptionCombo
        this.periodId = periodId
        this.sectionName = sectionName

        disposable.add(
            Flowable.zip<Boolean, DataSet, Section, Period, List<DataInputPeriod>,
                    Boolean, Sextet<Boolean, DataSet, Section, Period, List<DataInputPeriod>, Boolean>>(
                repository.canWriteAny(),
                repository.dataSet,
                repository.getSectionByDataSet(sectionName),
                repository.getPeriod(periodId),
                repository.dataInputPeriod,
                repository.isApproval(orgUnitUid, periodId, attributeOptionCombo),
                Function6 { canWrite, dataSet, section, period, dataInputPeriod, isApproval ->
                    Sextet.create(
                        canWrite,
                        dataSet,
                        section,
                        period,
                        dataInputPeriod,
                        isApproval
                    )
                }
            )
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { data ->
                        this.accessDataWrite = data.val0()
                        this.dataSet = data.val1()
                        this.section = data.val2()
                        this.period = data.val3()
                        this.dataInputPeriodModel = data.val4()
                        this.isApproval = data.val5()
                        view.setDataAccess(accessDataWrite)
                        view.setDataSet(dataSet)
                        view.setSection(section)
                        initTable()
                    },
                    { Timber.e(it) }
                )
        )

        disposable.add(
            repository.getCatCombo(sectionName).map { it.size }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { view.updateTabLayout(it) },
                    { Timber.e(it) }
                )
        )

        disposable.add(
            repository.isCompleted(orgUnitUid, periodId, attributeOptionCombo)
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    view::setCompleteReopenText
                ) { Timber.e(it) }
        )
    }

    private fun initTable() {
        disposable.add(
            repository.getCatCombo(sectionName)
                .flatMapIterable<CategoryCombo> { categoryCombos -> categoryCombos }
                .map<Sextet<CategoryCombo,
                        List<DataElement>,
                        Map<String, List<List<Pair<CategoryOption, Category>>>>,
                        List<DataSetTableModel>,
                        List<DataElementOperand>,
                        List<DataElementOperand>>>
                { categoryCombo ->
                    Flowable.zip<CategoryCombo,
                            List<DataElement>,
                            Map<String, List<List<Pair<CategoryOption, Category>>>>,
                            List<DataSetTableModel>,
                            List<DataElementOperand>,
                            List<DataElementOperand>,
                            Sextet<CategoryCombo,
                                    List<DataElement>,
                                    Map<String, List<List<Pair<CategoryOption, Category>>>>,
                                    List<DataSetTableModel>,
                                    List<DataElementOperand>,
                                    List<DataElementOperand>>>(
                        Flowable.just<CategoryCombo>(categoryCombo),
                        repository.getDataElements(categoryCombo, sectionName),
                        repository.getCatOptions(sectionName, categoryCombo.uid()),
                        repository.getDataValues(
                            orgUnitUid,
                            periodTypeName,
                            periodId,
                            attributeOptionCombo,
                            sectionName
                        ),
                        repository.getGreyFields(sectionName),
                        repository.compulsoryDataElements,
                        Function6<CategoryCombo,
                                List<DataElement>,
                                Map<String, List<List<Pair<CategoryOption, Category>>>>,
                                List<DataSetTableModel>,
                                List<DataElementOperand>,
                                List<DataElementOperand>,
                                Sextet<CategoryCombo,
                                        List<DataElement>,
                                        Map<String, List<List<Pair<CategoryOption, Category>>>>,
                                        List<DataSetTableModel>,
                                        List<DataElementOperand>,
                                        List<DataElementOperand>>>
                        { val0, val1, val2, val3, val4, val5 ->
                            Sextet.create(
                                val0,
                                val1,
                                val2,
                                val3,
                                val4,
                                val5
                            )
                        }
                    ).toObservable().blockingFirst()
                }
                .map { data ->
                    var options: List<List<String>> = ArrayList()
                    for ((_, value) in data.val2()) {
                        options = getCatOptionCombos(value, 0, ArrayList(), null)
                    }
                    transformCategories = ArrayList()
                    catOptionOrder = getCatOptionOrder(options)
                    for ((_, value) in transformCategories(data.val2())) {
                        transformCategories!!.addAll(value)
                    }

                    dataTableModel = DataTableModel.create(
                        data.val1(), data.val3(), data.val4(),
                        data.val5(), data.val0(), transformCategories
                    )

                    setTableData(dataTableModel!!)
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { quartet ->
                        view.setTableData(
                            quartet.val0(),
                            quartet.val1(),
                            quartet.val2(),
                            quartet.val3()
                        )
                    },
                    { Timber.e(it) }
                )
        )
    }

    private fun getCatOptionOrder(options: List<List<String>>): List<List<CategoryOption>> {
        val list = ArrayList<List<CategoryOption>>()
        for (combo in options) {
            val categoryOptions = ArrayList<CategoryOption>()
            for (option in combo) {
                categoryOptions.add(repository.getCatOptionFromUid(option))
            }
            list.add(categoryOptions)
        }
        return list
    }

    private fun getCatOptionComboOrder(catOptionCombos: List<CategoryOptionCombo>?):
            List<CategoryOptionCombo> {
        val categoryOptionCombosOrder = ArrayList<CategoryOptionCombo>()
        for (catOptions in catOptionOrder!!) {
            for (categoryOptionCombo in catOptionCombos!!) {
                if (catOptions.containsAll(
                        repository.getCatOptionFromCatOptionCombo(
                            categoryOptionCombo
                        )
                    )
                ) {
                    categoryOptionCombosOrder.add(categoryOptionCombo)
                }
            }
        }
        return categoryOptionCombosOrder
    }

    private fun setTableData(dataTableModel: DataTableModel):
            Quartet<DataTableModel, List<List<FieldViewModel>>, ArrayList<List<String>>, Boolean> {
        val cells = ArrayList<List<String>>()
        val listFields = ArrayList<List<FieldViewModel>>()
        var row = 0
        var column = 0
        var isNumber = false

        for (dataElement in dataTableModel.rows()!!) {
            val values = ArrayList<String>()
            val fields = ArrayList<FieldViewModel>()
            var totalRow = 0
            var fieldIsNumber = dataElement.valueType()!!.isNumeric
            if(!isNumber) {
                isNumber = dataElement.valueType()!!.isNumeric
            }
            val fieldFactory = FieldViewModelFactoryImpl("", "")

            for (
            categoryOptionCombo in
            getCatOptionComboOrder(
                repository.getCatOptionComboFrom(
                    dataTableModel.catCombo()?.uid(), catOptionOrder
                )
            )
            ) {
                var editable = true
                for (disabledDataElement in dataTableModel.dataElementDisabled()!!)
                    if (disabledDataElement.categoryOptionCombo() != null &&
                        disabledDataElement.categoryOptionCombo()!!.uid()
                        == categoryOptionCombo.uid() &&
                        disabledDataElement.dataElement()!!.uid() == dataElement.uid() ||
                        disabledDataElement.dataElement()!!.uid() == dataElement.uid()
                    ) {
                        editable = false
                    }

                for (
                categoryOption in
                repository.getCatOptionFromCatOptionCombo(categoryOptionCombo)
                )
                    if (!categoryOption.access().data().write()) {
                        editable = false
                    }

                var fieldViewModel: FieldViewModel? = null
                for (dataValue in dataTableModel.dataValues()!!)
                    if (dataValue.dataElement() == dataElement.uid() &&
                        dataValue.categoryOptionCombo() == categoryOptionCombo.uid()
                    ) {
                        fieldViewModel = fieldFactory.create(
                            dataValue.id()!!.toString(),
                            dataElement.displayFormName()!!,
                            dataElement.valueType()!!,
                            false,
                            dataElement.optionSetUid(),
                            dataValue.value(),
                            sectionName,
                            true,
                            editable,
                            null,
                            categoryOptionCombo.displayName(),
                            dataElement.uid(),
                            ArrayList(),
                            "android",
                            row,
                            column,
                            dataValue.categoryOptionCombo(),
                            dataValue.catCombo()
                        )
                    }

                if (fieldViewModel == null) {
                    fieldViewModel = fieldFactory.create(
                        "",
                        dataElement.displayFormName()!!,
                        dataElement.valueType()!!,
                        false,
                        dataElement.optionSetUid(),
                        "",
                        sectionName,
                        true,
                        editable,
                        null,
                        categoryOptionCombo.displayName(),
                        dataElement.uid(),
                        ArrayList(),
                        "android",
                        row,
                        column,
                        categoryOptionCombo.uid(),
                        dataTableModel.catCombo()!!.uid()
                    )
                }

                fields.add(fieldViewModel)
                values.add(fieldViewModel.value().toString())

                if (section!!.uid().isNotEmpty() && section!!.showRowTotals()!! &&
                    fieldIsNumber && fieldViewModel.value()!!.isNotEmpty()
                ) {
                    totalRow += Integer.parseInt(fieldViewModel.value()!!)
                }

                column++
            }

            for (fieldViewModel in fields)
                for (compulsoryDataElement in dataTableModel.compulsoryCells()!!)
                    if (compulsoryDataElement.categoryOptionCombo()!!.uid() ==
                        fieldViewModel.categoryOptionCombo() &&
                        compulsoryDataElement.dataElement()!!.uid() ==
                        fieldViewModel.dataElement()
                    ) {
                        fields[fields.indexOf(fieldViewModel)] = fieldViewModel.setMandatory()
                    }

            if (section!!.uid().isNotEmpty() && section!!.showRowTotals()!! && fieldIsNumber) {
                setTotalRow(totalRow, fields, values, row, column)
            }

            listFields.add(fields)
            cells.add(values)
            column = 0
            row++
        }

        tableCells.add(listFields)

        if (isNumber) {
            if (section!!.uid().isNotEmpty() && section!!.showColumnTotals()!!) {
                setTotalColumn(listFields, cells, dataTableModel.rows()!!, row, column)
            }
            if (section!!.uid().isNotEmpty() && section!!.showRowTotals()!!) {
                for (i in 0 until dataTableModel.header()!!.size) {
                    if (i == dataTableModel.header()!!.size - 1) {
                        dataTableModel.header()!![i].add(
                            CategoryOption.builder().uid("").displayName(
                                "Total"
                            ).build()
                        )
                    } else {
                        dataTableModel.header()!![i].add(
                            CategoryOption.builder().uid("").displayName(
                                ""
                            ).build()
                        )
                    }
                }
            }
        }

        val isEditable = accessDataWrite &&
                !isExpired(dataSet) &&
                dataInputPeriodModel.isEmpty() || (
                checkHasInputPeriod() != null && DateUtils.getInstance().isInsideInputPeriod(
                    checkHasInputPeriod()
                )
                ) &&
                !isApproval

        return Quartet.create(dataTableModel, listFields, cells, isEditable)
    }

    private fun isExpired(dataSet: DataSet?): Boolean {
        return if (0 == dataSet?.expiryDays()) {
            false
        } else DateUtils.getInstance()
            .isDataSetExpired(dataSet?.expiryDays()!!, period!!.endDate()!!)
    }

    private fun setTotalRow(
        totalRow: Int,
        fields: ArrayList<FieldViewModel>,
        values: ArrayList<String>,
        row: Int,
        column: Int
    ) {
        val fieldFactory = FieldViewModelFactoryImpl(
            "",
            ""
        )
        fields.add(
            fieldFactory.create(
                "", "", ValueType.INTEGER,
                false, "", totalRow.toString(), sectionName, true,
                false, null, null, "", ArrayList(), "", row, column, "", ""
            )
        )
        values.add(totalRow.toString())
    }

    private fun setTotalColumn(
        listFields: MutableList<List<FieldViewModel>>,
        cells: ArrayList<List<String>>,
        dataElements: MutableList<DataElement>,
        row: Int,
        columnPos: Int
    ) {
        val fieldFactory = FieldViewModelFactoryImpl(
            "",
            ""
        )

        val fields = ArrayList<FieldViewModel>()
        val values = ArrayList<String>()
        var existTotal = false
        for (data in dataElements)
            if (data.displayName() == "Total") {
                existTotal = true
            }

        if (existTotal) {
            listFields.removeAt(listFields.size - 1)
            cells.removeAt(listFields.size - 1)
        }

        val totals = IntArray(cells[0].size)
        for (dataValues in cells) {
            for (i in dataValues.indices) {
                if (dataValues[i].isNotEmpty()) {
                    totals[i] += Integer.parseInt(dataValues[i])
                }
            }
        }

        for (column in totals) {
            fields.add(
                fieldFactory.create(
                    "", "", ValueType.INTEGER,
                    false, "", column.toString(), sectionName, true,
                    false, null, null, "", ArrayList(), "", row, columnPos, "", ""
                )
            )

            values.add(column.toString())
        }

        listFields.add(fields)
        cells.add(values)

        if (!existTotal) {
            dataElements.add(
                DataElement.builder()
                    .uid("")
                    .displayName("Total")
                    .valueType(ValueType.INTEGER)
                    .build()
            )
        }
    }

    fun complete() {
        if ((!isApproval)) {
            analyticsHelper.setEvent(COMPLETE_REOPEN, CLICK, COMPLETE_REOPEN)
            if (view.isOpenOrReopen) {
                if (checkAllCombinedRequiredAndMandatoryFields()) {
                    if(needsValidationRules()) {
                        checkValidationRules()
                    } else {
                        completeDataSet()
                    }
                } else if (!checkMandatoryField(tableCells, dataTableModel?.dataValues())) {
                    view.showAlertDialog(
                        view.context.getString(R.string.missing_mandatory_fields_title),
                        view.context.resources.getString(R.string.field_mandatory)
                    )
                } else {
                    view.showAlertDialog(
                        view.context.getString(R.string.missing_mandatory_fields_title),
                        view.context.resources.getString(R.string.field_required)
                    )
                }
            } else {
                view.isOpenOrReopen
                disposable.add(
                    repository.reopenDataSet(orgUnitUid, periodId, attributeOptionCombo)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                            { reopen ->
                                view.setCompleteReopenText(false)
                                view.update(reopen!!)
                            },
                            { Timber.e(it) }
                        )
                )
            }
        }
    }

    private fun checkValidationRules() {
        if(isValidationRuleOptional()) {
            view.showValidationRuleDialog()
        } else {
            executeValidationRules()
        }
    }

    fun executeValidationRules(): Function0<Unit> {
        return {
            var isOk = false
            // TODO: ValidationRules - execute mandatory validation rules
            isOk = true

            if (isOk) {
                view.showSuccessValidationDialog()
            } else {
                view.showErrorsValidationDialog()
            }
        }
    }

    fun completeDataSet(): Function0<Unit> {
        return {
            disposable.add(
                repository.completeDataSet(orgUnitUid, periodId, attributeOptionCombo)
                    .subscribeOn(schedulerProvider.io())
                    .observeOn(schedulerProvider.ui())
                    .subscribe(
                        { completed ->
                            view.setCompleteReopenText(true)
                            view.update(completed!!)
                        },
                        { Timber.e(it) }
                    )
            )
        }
    }

    // TODO: ValidationRules- This is temporary until the SDK has a method to ask for this
    private fun needsValidationRules() = true
    private fun isValidationRuleOptional() = true

    private fun checkAllCombinedRequiredAndMandatoryFields(): Boolean {
        return (!dataSet!!.fieldCombinationRequired()!! ||
            checkAllFieldRequired(tableCells, dataTableModel?.dataValues()) &&
            dataSet!!.fieldCombinationRequired()!!) &&
            checkMandatoryField(tableCells, dataTableModel?.dataValues())
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun checkAllFieldRequired(
        tableCells: MutableList<List<List<FieldViewModel>>>,
        dataValues: List<DataSetTableModel>?
    ): Boolean {
        var allFields = true
        tableCells.forEach { table ->
            table.forEach { row ->
                val fieldsWithValue = dataValues
                    ?.filter { dataSetTableModel ->
                        dataSetTableModel.dataElement() == row.first().dataElement()
                    }

                if (!fieldsWithValue.isNullOrEmpty() && fieldsWithValue.size != row.size) {
                    allFields = false
                    view.highligthHeaderRow(
                        tableCells.indexOf(table),
                        table.indexOf(row),
                        false
                    )
                }
            }
        }
        return allFields
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun checkMandatoryField(
        tableCells: MutableList<List<List<FieldViewModel>>>,
        dataValues: List<DataSetTableModel>?
    ): Boolean {
        var mandatoryOk = true
        tableCells.forEach { table ->
            table.forEach { row ->
                row.forEach { field ->
                    val fieldWithValue = dataValues
                        ?.filter { dataSetTableModel ->
                            dataSetTableModel.dataElement() == field.dataElement() &&
                                    dataSetTableModel.categoryOptionCombo() ==
                                    field.categoryOptionCombo()
                        }

                    if (field.editable()!! && field.mandatory() && fieldWithValue.isNullOrEmpty()) {
                        mandatoryOk = false
                        view.highligthHeaderRow(
                            tableCells.indexOf(table),
                            table.indexOf(row),
                            true
                        )
                    }
                }
            }
        }
        return mandatoryOk
    }

    fun onDettach() {}

    fun displayMessage(message: String) {
    }

    fun initializeProcessor(dataSetSectionFragment: DataSetSectionFragment) {
        disposable.add(
            dataSetSectionFragment.rowActions()
                .flatMap { rowAction ->

                    var dataSetTableModel: DataSetTableModel? = null
                    val dataValue = dataTableModel?.dataValues()?.firstOrNull {
                        it.dataElement() == rowAction.dataElement()
                                && it.categoryOptionCombo() == rowAction.catOptCombo()
                    }
                    when(dataValue) {
                        null -> if(!rowAction.value().isNullOrEmpty()) {
                            dataSetTableModel = DataSetTableModel.create(
                                java.lang.Long.parseLong("0"),
                                rowAction.dataElement(),
                                periodId,
                                orgUnitUid,
                                rowAction.catOptCombo(),
                                attributeOptionCombo,
                                rowAction.value(),
                                "",
                                "",
                                rowAction.listCategoryOption(),
                                rowAction.catCombo()
                            ).also {
                                dataTableModel?.dataValues()?.add(it)
                            }
                        }
                        else ->  {
                            dataSetTableModel = dataValue.setValue(rowAction.value())
                            if(rowAction.value().isNullOrEmpty()) {
                                dataTableModel?.dataValues()?.remove(dataValue)
                            }
                        }
                    }

                    if ((dataSetSectionFragment.activity as DataSetTableActivity).isBackPressed) {
                        dataSetSectionFragment.abstractActivity.back()
                    }

                    dataSetTableModel?.let{
                        dataSetSectionFragment.updateData(rowAction, it.catCombo())
                        valueStore.save(it)
                    } ?: Flowable.just(
                        StoreResult("", ValueStoreImpl.ValueStoreResult.VALUE_HAS_NOT_CHANGED)
                    )
                }
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    { storeResult ->
                        val valueChange = ValueStoreImpl.ValueStoreResult.VALUE_CHANGED
                        if (storeResult.valueStoreResult == valueChange) {
                            view.showSnackBar()
                        }
                    },
                    { Timber.e(it) }
                )
        )
    }

    fun transformCategories(map: Map<String, List<List<Pair<CategoryOption, Category>>>>):
            Map<String, List<List<CategoryOption>>> {
        val mapTransform = HashMap<String, MutableList<List<CategoryOption>>>()
        for ((key) in map) {
            mapTransform[key] = mutableListOf()
            var repeat = 1
            var nextCategory = 0
            for (list in map.getValue(key)) {
                val catOptions = ArrayList<CategoryOption>()
                for (x in 0 until repeat) {
                    for (pair in list) {
                        catOptions.add(pair.val0())
                        nextCategory++
                    }
                }
                repeat = nextCategory
                nextCategory = 0
                mapTransform[key]?.add(catOptions)
            }
        }
        return mapTransform
    }

    fun getCatOptionCombos(
        listCategories: List<List<Pair<CategoryOption, Category>>>,
        rowPosition: Int,
        catComboUidList: MutableList<List<String>>,
        currentCatComboIds: MutableList<String>?
    ): List<List<String>> {
        var currentCatComboIds = currentCatComboIds
        if (rowPosition == listCategories.size) {
            val resultHelp = ArrayList(currentCatComboIds!!)
            catComboUidList.add(resultHelp)
            return catComboUidList
        }
        for (element in listCategories[rowPosition]) {
            if (rowPosition == 0) {
                currentCatComboIds = ArrayList()
            }
            removeCategoryOptionsBelowRowPosition(currentCatComboIds!!, rowPosition)
            currentCatComboIds.add(element.val0().uid())
            getCatOptionCombos(listCategories, rowPosition + 1, catComboUidList, currentCatComboIds)
        }

        return catComboUidList
    }

    private fun removeCategoryOptionsBelowRowPosition(
        currentCatComboIds: MutableList<String>,
        rowPosition: Int
    ) {
        for (currentRowPosition in currentCatComboIds.size downTo 1) {
            if (currentCatComboIds.size == rowPosition + currentRowPosition) {
                currentCatComboIds.removeAt(currentCatComboIds.size - currentRowPosition)
            }
        }
    }

    fun checkHasInputPeriod(): DataInputPeriod? {
        var inputPeriodModel: DataInputPeriod? = null
        for (inputPeriod in dataInputPeriodModel) {
            if (inputPeriod.period().uid() == periodId) {
                inputPeriodModel = inputPeriod
            }
        }
        return inputPeriodModel
    }

    fun getProcessor(): FlowableProcessor<RowAction> {
        return processor
    }

    fun getProcessorOptionSet(): FlowableProcessor<Trio<String, String, Int>>? {
        return processorOptionSet
    }

    fun saveCurrentSectionMeasures(rowHeaderWidth: Int, columnHeaderHeight: Int) {
        section?.let {
            prefs.setValue("W${dataSetUid}${it.uid()}", rowHeaderWidth)
            prefs.setValue("H${dataSetUid}${it.uid()}", columnHeaderHeight)
        }
    }

    fun getCurrentSectionMeasure(): kotlin.Pair<Int,Int> {
        return section?.let {
            Pair(
                prefs.getInt("W${dataSetUid}${it.uid()}", 0),
                prefs.getInt("H${dataSetUid}${it.uid()}", 0)
            )
        } ?: Pair(0, 0)
    }
}
