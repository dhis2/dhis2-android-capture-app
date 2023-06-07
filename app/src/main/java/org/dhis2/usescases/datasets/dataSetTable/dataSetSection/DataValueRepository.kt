package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import android.text.TextUtils
import androidx.annotation.VisibleForTesting
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import java.util.SortedMap
import org.dhis2.Bindings.decimalFormat
import org.dhis2.commons.bindings.dataValueConflicts
import org.dhis2.commons.data.tuples.Pair
import org.dhis2.composetable.model.TableCell
import org.dhis2.data.dhislogic.AUTH_DATAVALUE_ADD
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel
import org.dhis2.data.forms.dataentry.tablefields.FieldViewModelFactoryImpl
import org.dhis2.data.forms.dataentry.tablefields.spinner.SpinnerViewModel
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel
import org.dhis2.utils.DateUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.Category
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataapproval.DataApprovalState
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.dataelement.DataElementOperand
import org.hisp.dhis.android.core.dataset.DataInputPeriod
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.dataset.DataSetElement
import org.hisp.dhis.android.core.datavalue.DataValue
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.Period

class DataValueRepository(
    private val d2: D2,
    private val dataSetUid: String,
    private val sectionUid: String,
    private val orgUnitUid: String,
    private val periodId: String,
    private val attributeOptionComboUid: String
) {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getPeriod(): Flowable<Period> =
        d2.periodModule().periods().byPeriodId().eq(periodId).one().get().toFlowable()

    fun getCatCombo(): Flowable<List<CategoryCombo>> {
        val dataSetElements =
            d2.dataSetModule().dataSets().withDataSetElements().uid(dataSetUid).blockingGet()
                .dataSetElements()
        val categoryCombos = when (sectionUid) {
            "NO_SECTION" -> {
                dataSetElements?.map {
                    it.categoryCombo()?.uid()
                        ?: d2.dataElementModule().dataElements()
                            .uid(it.dataElement().uid())
                            .blockingGet()
                            .categoryComboUid()
                }?.distinct()
            }
            else -> {
                val dataElementsSectionUid = d2.dataSetModule().sections().withDataElements()
                    .byDataSetUid().eq(dataSetUid)
                    .uid(sectionUid)
                    .blockingGet()
                    .dataElements()
                    ?.map { it.uid() }
                dataSetElements
                    ?.filter { dataElementsSectionUid?.contains(it.dataElement().uid()) == true }
                    ?.map {
                        it.categoryCombo()?.uid()
                            ?: d2.dataElementModule().dataElements()
                                .uid(it.dataElement().uid())
                                .blockingGet()
                                .categoryComboUid()
                    }?.distinct()
            }
        }
        return d2.categoryModule().categoryCombos()
            .byUid().`in`(categoryCombos)
            .withCategories()
            .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
            .get().toFlowable()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getDataSet(): Flowable<DataSet> =
        d2.dataSetModule().dataSets().uid(dataSetUid).get().toFlowable()

    private fun getCatOptions(
        catCombo: String
    ): Flowable<Map<String, List<MutableList<Pair<CategoryOption, Category>>>>> {
        val map: MutableMap<String, List<MutableList<Pair<CategoryOption, Category>>>> =
            HashMap()
        map[catCombo] = getMap(catCombo)
        return Flowable.just(
            map
        )
    }

    private fun getMap(catCombo: String): List<MutableList<Pair<CategoryOption, Category>>> {
        val finalList = mutableListOf<MutableList<Pair<CategoryOption, Category>>>()
        val categories = d2.categoryModule().categoryCombos()
            .withCategories()
            .uid(catCombo)
            .blockingGet()
            .categories()
        for (category in categories!!) {
            val catOptions = d2.categoryModule().categories()
                .withCategoryOptions()
                .uid(category.uid())
                .blockingGet().categoryOptions()
            for (catOption in catOptions!!) {
                var add = true
                for (catComboList in finalList) {
                    if (catComboList.contains(
                            Pair.create(
                                    catOption,
                                    category
                                )
                        )
                    ) {
                        add = false
                    }
                }
                if (add) {
                    if (finalList.size != 0 &&
                        finalList[finalList.size - 1][0].val1().uid() == category.uid()
                    ) {
                        finalList[finalList.size - 1].add(
                            Pair.create(
                                catOption,
                                category
                            )
                        )
                    } else {
                        val list: MutableList<Pair<CategoryOption, Category>> =
                            ArrayList()
                        list.add(
                            Pair.create(
                                catOption,
                                category
                            )
                        )
                        finalList.add(list)
                    }
                }
            }
        }
        return finalList
    }

    private fun transformDataElement(
        dataElement: DataElement,
        override: List<DataSetElement>?
    ): DataElement {
        return override
            ?.firstOrNull {
                it.dataElement().uid() == dataElement.uid() && it.categoryCombo() != null
            }?.let {
                DataElement.builder()
                    .uid(dataElement.uid())
                    .code(dataElement.code())
                    .name(dataElement.name())
                    .displayName(dataElement.displayName())
                    .shortName(dataElement.shortName())
                    .displayShortName(dataElement.displayShortName())
                    .description(dataElement.description())
                    .displayDescription(dataElement.displayDescription())
                    .valueType(dataElement.valueType())
                    .zeroIsSignificant(dataElement.zeroIsSignificant())
                    .aggregationType(dataElement.aggregationType())
                    .formName(dataElement.formName())
                    .domainType(dataElement.domainType())
                    .displayFormName(dataElement.displayFormName())
                    .optionSet(dataElement.optionSet())
                    .categoryCombo(it.categoryCombo()).build()
            }
            ?: dataElement
    }

    private fun getDataValues(): Flowable<List<DataSetTableModel>> {
        val mapDataElementCatCombo: MutableMap<String, String> =
            HashMap()
        return Flowable.just(
            d2.dataSetModule().dataSets().withDataSetElements().byUid().eq(
                dataSetUid
            ).one().blockingGet()
        )
            .flatMapIterable { dataSet: DataSet ->
                var dataElements: MutableList<DataSetElement>? =
                    ArrayList()
                if (sectionUid != "NO_SECTION") {
                    val dataElementSection =
                        d2.dataSetModule().sections().withDataElements()
                            .byDataSetUid().eq(dataSetUid)
                            .uid(sectionUid)
                            .blockingGet().dataElements()
                    for (dataElement in dataElementSection!!) {
                        dataSet.dataSetElements()!!
                            .asSequence()
                            .filter { it.dataElement().uid() == dataElement.uid() }
                            .forEach {
                                dataElements!!.add(
                                    it
                                )
                            }
                    }
                } else {
                    dataElements = dataSet.dataSetElements()
                }
                dataElements
            }
            .flatMapIterable { dataSetElement: DataSetElement ->
                if (dataSetElement.categoryCombo() != null) {
                    mapDataElementCatCombo[dataSetElement.dataElement().uid()] =
                        dataSetElement.categoryCombo()!!.uid()
                } else {
                    mapDataElementCatCombo[dataSetElement.dataElement().uid()] =
                        d2.dataElementModule().dataElements()
                            .byUid().eq(dataSetElement.dataElement().uid())
                            .one().blockingGet().categoryCombo()!!.uid()
                }
                d2.dataValueModule().dataValues().byDataElementUid()
                    .eq(dataSetElement.dataElement().uid())
                    .byAttributeOptionComboUid().eq(attributeOptionComboUid)
                    .byPeriod().eq(periodId)
                    .byOrganisationUnitUid().eq(orgUnitUid)
                    .byDeleted().isFalse
                    .blockingGet()
            }
            .map { dataValue: DataValue ->
                val categoryOptions =
                    d2.categoryModule().categoryOptionCombos().withCategoryOptions()
                        .byUid().eq(dataValue.categoryOptionCombo()).one().blockingGet()
                        .categoryOptions()
                val uidCatOptions: MutableList<String> =
                    ArrayList()
                for (catOption in categoryOptions!!) uidCatOptions.add(catOption.uid())
                val dataElement = d2.dataElementModule()
                    .dataElements()
                    .uid(dataValue.dataElement())
                    .blockingGet()
                var value = dataValue.value()
                if (dataElement.optionSetUid() != null &&
                    dataElement.optionSetUid().isNotEmpty() && !TextUtils.isEmpty(
                            value
                        )
                ) {
                    val option =
                        d2.optionModule().options()
                            .byOptionSetUid().eq(dataElement.optionSetUid())
                            .byCode().eq(value).one().blockingGet()
                    if (option != null) {
                        value = option.displayName()
                    }
                }
                DataSetTableModel(
                    dataValue.dataElement(),
                    dataValue.period(),
                    dataValue.organisationUnit(),
                    dataValue.categoryOptionCombo(),
                    dataValue.attributeOptionCombo(),
                    value,
                    dataValue.storedBy(),
                    "", // no used anywhere, remove this field
                    uidCatOptions,
                    mapDataElementCatCombo[dataValue.dataElement()]
                )
            }.toList().toFlowable()
    }

    private fun getCompulsoryDataElements(): Flowable<List<DataElementOperand>> {
        return d2.dataSetModule().dataSets().withCompulsoryDataElementOperands().uid(dataSetUid)
            .get()
            .map<List<DataElementOperand>> { obj: DataSet -> obj.compulsoryDataElementOperands() }
            .toFlowable()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getGreyFields(): Flowable<List<DataElementOperand>> = when {
        sectionUid != "NO_SECTION" ->
            d2.dataSetModule().sections()
                .withGreyedFields()
                .byDataSetUid().eq(dataSetUid)
                .uid(sectionUid)
                .get()
                .map { section -> section.greyedFields() }
                .toFlowable()
        else -> Flowable.just(ArrayList())
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun isApproval(): Flowable<Boolean> {
        return getDataSet().flatMap { dataSet ->
            dataSet.workflow()?.let { workflow ->
                Flowable.fromCallable {
                    val dataApproval = d2.dataSetModule().dataApprovals()
                        .byOrganisationUnitUid().eq(orgUnitUid)
                        .byPeriodId().eq(periodId)
                        .byAttributeOptionComboUid().eq(attributeOptionComboUid)
                        .byWorkflowUid().eq(workflow.uid())
                        .one().blockingGet()
                    val approvalStates = listOf(
                        DataApprovalState.APPROVED_ELSEWHERE,
                        DataApprovalState.APPROVED_ABOVE,
                        DataApprovalState.APPROVED_HERE,
                        DataApprovalState.ACCEPTED_ELSEWHERE,
                        DataApprovalState.ACCEPTED_HERE
                    )
                    dataApproval != null && approvalStates.contains(dataApproval.state())
                }
            } ?: Flowable.just(false)
        }
    }

    private fun getDataElements(categoryCombo: CategoryCombo): Flowable<List<DataElement>> {
        return if (sectionUid != "NO_SECTION") {
            val listDataElements =
                d2.dataSetModule().sections().withDataElements().byDataSetUid().eq(dataSetUid)
                    .uid(sectionUid).blockingGet().dataElements()
            val dataElementsOverride: MutableList<DataElement> =
                ArrayList()
            val dataSetElements =
                d2.dataSetModule().dataSets().withDataSetElements().uid(dataSetUid).blockingGet()
                    .dataSetElements()
            listDataElements
                ?.map { transformDataElement(it, dataSetElements) }
                ?.filter { it.categoryComboUid() == categoryCombo.uid() }
                ?.forEach { dataElementsOverride.add(it) }

            Flowable.just(
                dataElementsOverride
            )
        } else {
            val dataElementUids: MutableList<String> =
                ArrayList()
            val dataSetElements =
                d2.dataSetModule().dataSets().withDataSetElements().byUid().eq(dataSetUid).one()
                    .blockingGet().dataSetElements()
            for (dataSetElement in dataSetElements!!) {
                if (dataSetElement.categoryCombo() != null &&
                    categoryCombo.uid() == dataSetElement.categoryCombo()!!.uid()
                ) {
                    dataElementUids.add(dataSetElement.dataElement().uid())
                } else {
                    val uid = d2.dataElementModule().dataElements()
                        .uid(dataSetElement.dataElement().uid()).blockingGet().categoryComboUid()
                    if (categoryCombo.uid() == uid) {
                        dataElementUids.add(dataSetElement.dataElement().uid())
                    }
                }
            }
            d2.dataElementModule().dataElements()
                .byUid().`in`(dataElementUids)
                .orderByName(RepositoryScope.OrderByDirection.ASC)
                .get().toFlowable()
        }
    }

    private fun getCatOptionFromCatOptionCombo(
        categoryOptionCombo: CategoryOptionCombo
    ): List<CategoryOption> {
        return d2.categoryModule().categoryOptionCombos().withCategoryOptions()
            .uid(categoryOptionCombo.uid()).blockingGet().categoryOptions()!!
    }

    private fun getCatOptionFromUid(catOption: String): CategoryOption {
        return d2.categoryModule().categoryOptions().uid(catOption).blockingGet()
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun canWriteAny(): Flowable<Boolean> {
        return d2.dataSetModule().dataSets().uid(dataSetUid).get().toFlowable()
            .flatMap { dataSet: DataSet ->
                when {
                    dataSet.access().data().write() ->
                        d2.categoryModule()
                            .categoryOptionCombos().withCategoryOptions()
                            .byCategoryComboUid().eq(dataSet.categoryCombo()?.uid())
                            .get().toFlowable()
                            .map { categoryOptionCombos: List<CategoryOptionCombo> ->
                                val canWriteCatOption = categoryOptionCombos.any { catOptionCombo ->
                                    catOptionCombo.categoryOptions()?.any {
                                        it.access().data().write()
                                    } ?: false
                                }
                                var canWriteOrgUnit = false
                                if (canWriteCatOption) {
                                    val organisationUnits =
                                        d2.organisationUnitModule().organisationUnits()
                                            .byDataSetUids(listOf(dataSetUid))
                                            .byOrganisationUnitScope(
                                                OrganisationUnit.Scope.SCOPE_DATA_CAPTURE
                                            ).blockingGet()
                                    canWriteOrgUnit = organisationUnits.isNotEmpty()
                                }
                                val hasDataValueAuthority = !d2.userModule().authorities()
                                    .byName().eq(AUTH_DATAVALUE_ADD)
                                    .blockingIsEmpty()
                                hasDataValueAuthority && canWriteCatOption && canWriteOrgUnit
                            }
                    else -> Flowable.just(false)
                }
            }
    }

    private fun getCatOptionComboFrom(
        catComboUid: String?,
        catOptionsList: List<List<CategoryOption>>?
    ): List<CategoryOptionCombo> {
        val catOptionCombos: MutableList<CategoryOptionCombo> =
            ArrayList()
        catOptionsList?.forEach { catOptions ->
            catOptionCombos.addAll(
                d2.categoryModule().categoryOptionCombos()
                    .byCategoryOptions(UidsHelper.getUidsList(catOptions))
                    .byCategoryComboUid().eq(catComboUid)
                    .blockingGet()
            )
        }
        return catOptionCombos
    }

    fun getDataSetIndicators(): Single<SortedMap<String?, String>> {
        return d2.indicatorModule().indicators()
            .byDataSetUid(dataSetUid)
            .bySectionUid(sectionUid)
            .get().map { indicators ->
                val dataSetIndicators = hashMapOf<String?, String>()
                indicators.forEach { indicator ->
                    dataSetIndicators[indicator.displayName()] = d2.indicatorModule()
                        .dataSetIndicatorEngine()
                        .blockingEvaluate(
                            indicator.uid(),
                            dataSetUid,
                            periodId,
                            orgUnitUid,
                            attributeOptionComboUid
                        ).toInt().toString()
                }
                return@map dataSetIndicators
                    .toSortedMap(compareBy { it })
                    .takeIf { it.isNotEmpty() }
            }
    }

    private fun showColumnTotals(): Boolean {
        return when (sectionUid) {
            "NO_SECTION" -> false
            else ->
                d2.dataSetModule().sections().uid(sectionUid)
                    .blockingGet()
                    .showColumnTotals() == true
        }
    }

    private fun showRowTotals(): Boolean {
        return when (sectionUid) {
            "NO_SECTION" -> false
            else ->
                d2.dataSetModule().sections().uid(sectionUid)
                    .blockingGet()
                    .showRowTotals() == true
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun getDataInputPeriod(): DataInputPeriod? {
        val inputPeriods = d2.dataSetModule().dataSets()
            .withDataInputPeriods()
            .uid(dataSetUid)
            .blockingGet()
            ?.dataInputPeriods() ?: emptyList()
        var inputPeriodModel: DataInputPeriod? = null
        for (inputPeriod in inputPeriods) {
            if (inputPeriod.period().uid() == periodId) {
                inputPeriodModel = inputPeriod
            }
        }
        return inputPeriodModel
    }

    fun getDataTableModel(categoryComboUid: String): Observable<DataTableModel> {
        val categoryCombo = d2.categoryModule().categoryCombos().uid(categoryComboUid).blockingGet()
        return getDataTableModel(categoryCombo)
    }

    fun getDataTableModel(categoryCombo: CategoryCombo): Observable<DataTableModel> {
        return Flowable.zip<List<DataElement>,
            Map<String, List<List<Pair<CategoryOption, Category>>>>,
            List<DataSetTableModel>,
            List<DataElementOperand>,
            List<DataElementOperand>,
            DataTableModel>(
            getDataElements(categoryCombo),
            getCatOptions(categoryCombo.uid()),
            getDataValues(),
            getGreyFields(),
            getCompulsoryDataElements()
        ) { dataElements: List<DataElement>,
            optionsWithCategory: Map<String, List<List<Pair<CategoryOption,
                            Category>>>>,
            dataValues: List<DataSetTableModel>,
            disabledDataElements: List<DataElementOperand>,
            compulsoryCells: List<DataElementOperand> ->
            var options: List<List<String>> = ArrayList()
            for ((_, value) in optionsWithCategory) {
                options = getCatOptionCombos(value, 0, ArrayList(), null)
            }
            val transformCategories = mutableListOf<MutableList<CategoryOption>>()
            for ((_, value) in transformCategories(optionsWithCategory)) {
                transformCategories.addAll(value)
            }

            DataTableModel(
                periodId,
                orgUnitUid,
                attributeOptionComboUid,
                dataElements.toMutableList(),
                dataValues.toMutableList(),
                disabledDataElements,
                compulsoryCells,
                categoryCombo,
                transformCategories,
                getCatOptionOrder(options)
            )
        }.toObservable()
    }

    private fun getCatOptionCombos(
        listCategories: List<List<Pair<CategoryOption, Category>>>,
        rowPosition: Int,
        catComboUidList: MutableList<List<String>>,
        catComboIds: MutableList<String>?
    ): List<List<String>> {
        var currentCatComboIds = catComboIds
        if (rowPosition == listCategories.size) {
            currentCatComboIds?.toList()?.let { catComboUidList.add(it) }
            return catComboUidList
        }
        listCategories[rowPosition].forEach { element ->
            if (rowPosition == 0) {
                currentCatComboIds = mutableListOf()
            }
            currentCatComboIds?.let {
                removeCategoryOptionsBelowRowPosition(it, rowPosition)
                it.add(element.val0().uid())
            }
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

    fun setTableData(
        dataTableModel: DataTableModel,
        errors: MutableMap<String, String>
    ): TableData {
        val cells = ArrayList<List<String>>()
        val listFields = mutableListOf<List<FieldViewModel>>()
        var row = 0
        var column = 0
        var isNumber = false

        val fieldFactory = FieldViewModelFactoryImpl("", "")

        val categorOptionCombos = getCatOptionComboFrom(
            dataTableModel.catCombo?.uid(),
            dataTableModel.catOptionOrder
        )

        val conflicts = d2.dataValueConflicts(
            dataSetUid,
            periodId,
            orgUnitUid,
            attributeOptionComboUid
        )

        for (dataElement in dataTableModel.rows ?: emptyList()) {
            val values = ArrayList<String>()
            val fields = ArrayList<FieldViewModel>()
            var totalRow = 0.0
            val fieldIsNumber = dataElement.valueType()!!.isNumeric
            if (!isNumber) {
                isNumber = dataElement.valueType()!!.isNumeric
            }

            val options = dataElement.optionSetUid()?.let {
                d2.optionModule().options()
                    .byOptionSetUid().eq(it)
                    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                    .blockingGet()
                    .map { option -> "${option.code()}_${option.displayName()}" }
            } ?: emptyList()

            for (
            categoryOptionCombo in categorOptionCombos
            ) {
                val isEditable = validateIfIsEditable(
                    dataTableModel.dataElementDisabled!!,
                    dataElement,
                    categoryOptionCombo
                )

                val mandatory = dataTableModel.compulsoryCells?.find { compulsoryDataElement ->
                    compulsoryDataElement.categoryOptionCombo()
                        ?.uid() == categoryOptionCombo.uid() &&
                        compulsoryDataElement.dataElement()?.uid() == dataElement.uid()
                }?.let { true } ?: false

                val fieldValue = dataTableModel.dataValues?.find { dataSetTableModel ->
                    dataSetTableModel.dataElement == dataElement.uid() &&
                        dataSetTableModel.categoryOptionCombo == categoryOptionCombo.uid()
                }?.value

                var fieldViewModel = fieldFactory.create(
                    dataElement.uid() + "_" + categoryOptionCombo.uid(),
                    dataElement.displayFormName()!!,
                    dataElement.valueType()!!,
                    mandatory,
                    dataElement.optionSetUid(),
                    fieldValue,
                    sectionUid,
                    true,
                    isEditable,
                    null,
                    dataElement.displayDescription(),
                    dataElement.uid(),
                    options,
                    "android",
                    row,
                    column,
                    categoryOptionCombo.uid(),
                    dataTableModel.catCombo?.uid()
                )

                val valueStateSyncState = d2.dataValueModule().dataValues()
                    .byDataSetUid(dataSetUid)
                    .byPeriod().eq(periodId)
                    .byOrganisationUnitUid().eq(orgUnitUid)
                    .byAttributeOptionComboUid().eq(attributeOptionComboUid)
                    .byDataElementUid().eq(dataElement.uid())
                    .byCategoryOptionComboUid().eq(categoryOptionCombo.uid())
                    .blockingGet()
                    ?.find { it.dataElement() == dataElement.uid() }
                    ?.syncState()

                val conflictInField =
                    conflicts.takeIf {
                        when (valueStateSyncState) {
                            State.ERROR,
                            State.WARNING -> true
                            else -> false
                        }
                    }?.filter {
                        "${it.dataElement()}_${it.categoryOptionCombo()}" == fieldViewModel.uid()
                    }?.takeIf { it.isNotEmpty() }?.map { it.displayDescription() ?: "" }

                val error = errors[fieldViewModel.uid()]

                val errorList = when {
                    valueStateSyncState == State.ERROR &&
                        conflictInField != null &&
                        error != null ->
                        conflictInField + listOf(error)
                    valueStateSyncState == State.ERROR && conflictInField != null ->
                        conflictInField
                    error != null ->
                        listOf(error)
                    else -> null
                }

                val warningList = when {
                    valueStateSyncState == State.WARNING &&
                        conflictInField != null ->
                        conflictInField
                    else ->
                        null
                }

                fieldViewModel = errorList?.let {
                    fieldViewModel.withError(it.joinToString(".\n"))
                } ?: fieldViewModel

                fieldViewModel = warningList?.let {
                    fieldViewModel.withWarning(warningList.joinToString(".\n"))
                } ?: fieldViewModel

                fields.add(fieldViewModel)

                values.add(fieldViewModel.value().toString())

                if (showRowTotals() && fieldIsNumber &&
                    fieldViewModel.value()?.isNotEmpty() == true
                ) {
                    totalRow += fieldViewModel.value()!!.toDouble()
                }

                column++
            }

            if (showRowTotals() && fieldIsNumber) {
                setTotalRow(totalRow, fields, values, row, column)
            }

            listFields.add(fields)
            cells.add(values)
            column = 0
            row++
        }

        if (isNumber) {
            if (showColumnTotals()) {
                setTotalColumn(
                    listFields,
                    cells,
                    dataTableModel.rows ?: mutableListOf(),
                    row,
                    column
                )
            }
            if (showRowTotals()) {
                for (i in dataTableModel.header!!.indices) {
                    if (i == dataTableModel.header.size - 1) {
                        dataTableModel.header[i].add(
                            CategoryOption.builder().uid("")
                                .displayName("Total")
                                .build()
                        )
                    } else {
                        dataTableModel.header[i].add(
                            CategoryOption.builder().uid("")
                                .displayName("")
                                .build()
                        )
                    }
                }
            }
        }

        val isEditable = canWriteAny().blockingFirst() &&
            !isExpired(getDataSet().blockingFirst()) &&
            (
                getDataInputPeriod() == null || (
                    getDataInputPeriod() != null && DateUtils.getInstance()
                        .isInsideInputPeriod(
                            getDataInputPeriod()
                        )
                    )
                ) &&
            !isApproval().blockingFirst()

        val hasDataElementDecoration = getDataSet().blockingFirst().dataElementDecoration() == true

        return TableData(
            dataTableModel,
            listFields,
            cells,
            isEditable,
            showRowTotals(),
            showColumnTotals(),
            hasDataElementDecoration
        )
    }

    private fun isExpired(dataSet: DataSet?): Boolean {
        return if (0 == dataSet?.expiryDays()) {
            false
        } else {
            DateUtils.getInstance()
                .isDataSetExpired(
                    dataSet?.expiryDays()!!,
                    getPeriod().blockingFirst()!!.endDate()!!
                )
        }
    }

    private fun getCatOptionOrder(options: List<List<String>>): List<List<CategoryOption>> {
        val list = ArrayList<List<CategoryOption>>()
        for (combo in options) {
            val categoryOptions = ArrayList<CategoryOption>()
            for (option in combo) {
                categoryOptions.add(getCatOptionFromUid(option))
            }
            list.add(categoryOptions)
        }
        return list
    }

    private fun transformCategories(
        map: Map<String, List<List<Pair<CategoryOption, Category>>>>
    ): HashMap<String, MutableList<MutableList<CategoryOption>>> {
        val mapTransform = HashMap<String, MutableList<MutableList<CategoryOption>>>()
        for ((key) in map) {
            mapTransform[key] = mutableListOf()
            var repeat = 1
            var nextCategory = 0
            for (list in map.getValue(key)) {
                val catOptions = mutableListOf<CategoryOption>()
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

    private fun setTotalRow(
        totalRow: Double,
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
                "",
                "",
                ValueType.INTEGER,
                false,
                "",
                totalRow.toString(),
                sectionUid,
                true,
                false,
                null,
                null,
                "",
                ArrayList(),
                "",
                row,
                column,
                "",
                ""
            )
        )
        values.add(totalRow.decimalFormat)
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

        val totals = DoubleArray(cells[0].size)
        for (dataValues in cells) {
            for (i in dataValues.indices) {
                if (dataValues[i].isNotEmpty()) {
                    val value = dataValues[i].toDoubleOrNull() ?: 0.0
                    totals[i] += value
                }
            }
        }

        for (column in totals) {
            fields.add(
                fieldFactory.create(
                    "",
                    "",
                    ValueType.INTEGER,
                    false,
                    "",
                    column.toString(),
                    sectionUid,
                    true,
                    false,
                    null,
                    null,
                    "",
                    ArrayList(),
                    "",
                    row,
                    columnPos,
                    "",
                    ""
                )
            )

            values.add(column.decimalFormat)
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

    private fun validateIfIsEditable(
        dataElementDisabled: List<DataElementOperand>,
        dataElement: DataElement,
        categoryOptionCombo: CategoryOptionCombo
    ): Boolean {
        var editable = true
        for (disabledDataElement in dataElementDisabled) {
            if (disabledDataElement.categoryOptionCombo() != null &&
                disabledDataElement.categoryOptionCombo()!!.uid() == categoryOptionCombo.uid() &&
                disabledDataElement.dataElement()!!.uid() == dataElement.uid() &&
                disabledDataElement.categoryOptionCombo()!!.uid() == categoryOptionCombo.uid()
            ) {
                editable = false
            }
        }

        for (categoryOption in getCatOptionFromCatOptionCombo(categoryOptionCombo)) {
            if (!categoryOption.access().data().write()) {
                editable = false
            }
        }

        return editable
    }

    fun getDataElement(dataElementUid: String): DataElement {
        return d2.dataElementModule()
            .dataElements()
            .uid(dataElementUid)
            .blockingGet()
    }

    fun orgUnits(): List<OrganisationUnit> {
        return d2.organisationUnitModule().organisationUnits()
            .byDataSetUids(listOf(dataSetUid))
            .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
            .blockingGet()
    }

    fun getOrgUnitById(orgUnitUid: String): String? {
        return d2.organisationUnitModule().organisationUnits()
            .uid(orgUnitUid)
            .blockingGet()
            .displayName()
    }

    fun getOptionSetViewModel(dataElement: DataElement, cell: TableCell): SpinnerViewModel {
        return SpinnerViewModel.create(
            cell.id,
            dataElement.displayFormName()!!,
            "",
            false,
            dataElement.optionSetUid(),
            cell.value,
            sectionUid,
            null,
            dataElement.displayDescription(),
            dataElement.uid(),
            emptyList(),
            "android",
            0,
            0,
            cell.id!!.split("_")[1],
            dataElement.categoryComboUid()
        )
    }

    fun getCatOptComboOptions(catOptComboUid: String): List<String> {
        return d2.categoryModule().categoryOptionCombos().withCategoryOptions()
            .uid(catOptComboUid)
            .blockingGet()
            ?.takeIf {
                d2.categoryModule().categoryCombos()
                    .uid(it.categoryCombo()?.uid())
                    .blockingGet()
                    .isDefault == false
            }?.displayName()?.split(", ") ?: emptyList()
    }

    fun getDataSetInfo(): Triple<String, String, String> {
        return Triple(periodId, orgUnitUid, attributeOptionComboUid)
    }
}
