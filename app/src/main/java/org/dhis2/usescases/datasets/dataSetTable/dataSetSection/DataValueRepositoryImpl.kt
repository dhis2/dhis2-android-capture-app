package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import android.text.TextUtils
import io.reactivex.Flowable
import java.util.ArrayList
import java.util.HashMap
import org.dhis2.data.tuples.Pair
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.helpers.UidsHelper
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.category.Category
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.dataapproval.DataApprovalState
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.dataelement.DataElementOperand
import org.hisp.dhis.android.core.dataset.DataInputPeriod
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.dataset.DataSetElement
import org.hisp.dhis.android.core.dataset.Section
import org.hisp.dhis.android.core.datavalue.DataValue
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.Period

class DataValueRepositoryImpl(private val d2: D2, private val dataSetUid: String) :
    DataValueRepository {
    override fun getPeriod(periodId: String): Flowable<Period> =
        d2.periodModule().periods().byPeriodId().eq(periodId).one().get().toFlowable()

    override fun getDataInputPeriod(): Flowable<List<DataInputPeriod>> {
        return Flowable.fromCallable {
            d2.dataSetModule().dataSets().withDataInputPeriods().uid(dataSetUid).blockingGet()
                .dataInputPeriods()
        }
    }

    override fun getCatCombo(sectionName: String): Flowable<List<CategoryCombo>> {
        val categoryCombos: MutableList<String> = ArrayList()
        val dataSetElements =
            d2.dataSetModule().dataSets().withDataSetElements().uid(dataSetUid).blockingGet()
                .dataSetElements()
        when {
            sectionName != "NO_SECTION" -> {
                val dataElements = d2.dataSetModule()
                    .sections()
                    .withDataElements()
                    .byDataSetUid().eq(dataSetUid)
                    .byDisplayName().eq(sectionName)
                    .one().blockingGet()
                    .dataElements()
                dataSetElements?.forEach { dataSetElement ->
                    dataElements
                        ?.asSequence()
                        ?.filter { dataSetElement.dataElement().uid() == it.uid() }
                        ?.forEach { dataElement ->
                            when {
                                dataSetElement.categoryCombo()
                                    ?.let { it.uid() !in categoryCombos } ?: false ->
                                    categoryCombos.add(dataSetElement.categoryCombo()!!.uid())
                                dataElement.categoryCombo()
                                    ?.let { it.uid() !in categoryCombos } ?: false ->
                                    categoryCombos.add(dataElement.categoryComboUid())
                            }
                        }
                }
            }
            else ->
                dataSetElements?.map {
                    it.categoryCombo()?.uid()
                        ?: d2.dataElementModule()
                            .dataElements()
                            .uid(it.dataElement().uid())
                            .blockingGet()
                            .categoryComboUid()
                }?.toList()?.let {
                    categoryCombos.addAll(it)
                }
        }
        return d2.categoryModule().categoryCombos().byUid().`in`(categoryCombos).withCategories()
            .withCategoryOptionCombos().orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
            .get().toFlowable()
    }

    override fun getDataSet(): Flowable<DataSet> =
        d2.dataSetModule().dataSets().uid(dataSetUid).get().toFlowable()

    override fun getCatOptions(
        sectionName: String,
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
        val finalList: MutableList<MutableList<Pair<CategoryOption, Category>>> =
            ArrayList()
        val categories =
            d2.categoryModule().categoryCombos().withCategories().withCategoryOptionCombos().byUid()
                .eq(catCombo).one().blockingGet().categories()
        for (category in categories!!) {
            val catOptions =
                d2.categoryModule().categories().withCategoryOptions().byUid().eq(category.uid())
                    .one().blockingGet().categoryOptions()
            for (catOption in catOptions!!) {
                var add = true
                for (catComboList in finalList) {
                    if (catComboList.contains(
                            Pair.create(
                                catOption,
                                category
                            )
                        )
                    ) add = false
                }
                if (add) {
                    if (finalList.size != 0 && finalList[finalList.size - 1][0].val1().uid() == category.uid()) {
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

    override fun getDataValues(
        orgUnitUid: String,
        periodType: String,
        initPeriodType: String,
        catOptionComb: String,
        sectionName: String
    ): Flowable<List<DataSetTableModel>> {
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
                if (sectionName != "NO_SECTION") {
                    val dataElementSection =
                        d2.dataSetModule().sections().withDataElements()
                            .byDataSetUid().eq(dataSetUid).byDisplayName().eq(sectionName).one()
                            .blockingGet().dataElements()
                    for (dataElement in dataElementSection!!) {
                        for (dataSetElement in dataSet.dataSetElements()!!) if (dataSetElement.dataElement().uid() == dataElement.uid()) dataElements!!.add(
                            dataSetElement
                        )
                    }
                } else dataElements = dataSet.dataSetElements()
                dataElements
            }
            .flatMapIterable { dataSetElement: DataSetElement ->
                if (dataSetElement.categoryCombo() != null) mapDataElementCatCombo[dataSetElement.dataElement().uid()] =
                    dataSetElement.categoryCombo()!!.uid() else mapDataElementCatCombo[dataSetElement.dataElement().uid()] =
                    d2.dataElementModule().dataElements().byUid().eq(dataSetElement.dataElement().uid()).one().blockingGet().categoryCombo()!!.uid()
                d2.dataValueModule().dataValues().byDataElementUid()
                    .eq(dataSetElement.dataElement().uid())
                    .byAttributeOptionComboUid().eq(catOptionComb)
                    .byPeriod().eq(initPeriodType)
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
                    !dataElement.optionSetUid().isEmpty() && !TextUtils.isEmpty(
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
                DataSetTableModel.create(
                    dataValue.id(),
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

    override fun getCompulsoryDataElements(): Flowable<List<DataElementOperand>> {
        return d2.dataSetModule().dataSets().withCompulsoryDataElementOperands().uid(dataSetUid)
            .get()
            .map<List<DataElementOperand>> { obj: DataSet -> obj.compulsoryDataElementOperands() }
            .toFlowable()
    }

    override fun getGreyFields(sectionName: String): Flowable<List<DataElementOperand>> =
        when {
            sectionName.isNotEmpty() && sectionName != "NO_SECTION" ->
                d2.dataSetModule().sections()
                    .withGreyedFields()
                    .byDataSetUid().eq(dataSetUid)
                    .byDisplayName().eq(sectionName)
                    .one().get()
                    .map<List<DataElementOperand>> { obj: Section -> obj.greyedFields() }
                    .toFlowable()
            else -> Flowable.just(ArrayList())
        }

    override fun getSectionByDataSet(section: String): Flowable<Section> =
        when {
            section.isNotEmpty() && section != "NO_SECTION" ->
                d2.dataSetModule().sections()
                    .byDataSetUid().eq(dataSetUid)
                    .byDisplayName().eq(section)
                    .one().get().toFlowable()
            else -> Flowable.just(Section.builder().uid("").build())
        }

    override fun completeDataSet(
        orgUnitUid: String,
        periodInitialDate: String,
        catCombo: String
    ): Flowable<Boolean> {
        d2.dataSetModule().dataSetCompleteRegistrations().value(
            periodInitialDate,
            orgUnitUid,
            dataSetUid,
            catCombo
        ).blockingSet()
        return d2.dataSetModule().dataSetCompleteRegistrations().value(
            periodInitialDate,
            orgUnitUid,
            dataSetUid,
            catCombo
        )
            .exists()
            .toFlowable()
    }

    override fun reopenDataSet(
        orgUnitUid: String,
        periodInitialDate: String,
        catCombo: String
    ): Flowable<Boolean> {
        d2.dataSetModule().dataSetCompleteRegistrations().value(
            periodInitialDate,
            orgUnitUid,
            dataSetUid,
            catCombo
        ).blockingDeleteIfExist()
        return d2.dataSetModule().dataSetCompleteRegistrations().value(
            periodInitialDate,
            orgUnitUid,
            dataSetUid,
            catCombo
        ).exists()
            .map { exist: Boolean? -> !exist!! }
            .toFlowable()
    }

    override fun isCompleted(
        orgUnitUid: String,
        periodInitialDate: String,
        catCombo: String
    ): Flowable<Boolean> {
        return Flowable.fromCallable {
            val completeRegistration =
                d2.dataSetModule().dataSetCompleteRegistrations()
                    .byDataSetUid().eq(dataSetUid)
                    .byAttributeOptionComboUid().eq(catCombo)
                    .byPeriod().eq(periodInitialDate)
                    .byOrganisationUnitUid().eq(orgUnitUid)
                    .one().blockingGet()
            completeRegistration != null && !completeRegistration.deleted()!!
        }
    }

    override fun isApproval(
        orgUnit: String,
        period: String,
        attributeOptionCombo: String
    ): Flowable<Boolean> {
        return Flowable.fromCallable {
            val dataApproval = d2.dataSetModule().dataApprovals()
                .byOrganisationUnitUid().eq(orgUnit)
                .byPeriodId().eq(period)
                .byAttributeOptionComboUid().eq(attributeOptionCombo)
                .one().blockingGet()
            dataApproval != null && dataApproval.state() == DataApprovalState.APPROVED_HERE
        }
    }

    override fun getDataElements(
        categoryCombo: CategoryCombo,
        sectionName: String
    ): Flowable<List<DataElement>> {
        return if (sectionName != "NO_SECTION") {
            val listDataElements =
                d2.dataSetModule().sections().withDataElements().byDataSetUid().eq(dataSetUid)
                    .byDisplayName().eq(sectionName).one().blockingGet().dataElements()
            val dataElementsOverride: MutableList<DataElement> =
                ArrayList()
            val dataSetElements =
                d2.dataSetModule().dataSets().withDataSetElements().uid(dataSetUid).blockingGet()
                    .dataSetElements()
            for (de in listDataElements!!) {
                val override = transformDataElement(de, dataSetElements)
                if (override.categoryComboUid() == categoryCombo.uid()) dataElementsOverride.add(
                    override
                )
            }
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
                if (dataSetElement.categoryCombo() != null && categoryCombo.uid() == dataSetElement.categoryCombo()!!.uid()) dataElementUids.add(
                    dataSetElement.dataElement().uid()
                ) else {
                    val uid = d2.dataElementModule().dataElements()
                        .uid(dataSetElement.dataElement().uid()).blockingGet().categoryComboUid()
                    if (categoryCombo.uid() == uid) dataElementUids.add(dataSetElement.dataElement().uid())
                }
            }
            d2.dataElementModule().dataElements()
                .byUid().`in`(dataElementUids)
                .orderByName(RepositoryScope.OrderByDirection.ASC)
                .get().toFlowable()
        }
    }

    override fun getCatOptionFromCatOptionCombo(categoryOptionCombo: CategoryOptionCombo): List<CategoryOption> {
        return d2.categoryModule().categoryOptionCombos().withCategoryOptions()
            .uid(categoryOptionCombo.uid()).blockingGet().categoryOptions()!!
    }

    override fun getCatOptionFromUid(catOption: String): CategoryOption {
        return d2.categoryModule().categoryOptions().uid(catOption).blockingGet()
    }

    override fun canWriteAny(): Flowable<Boolean> {
        return d2.dataSetModule().dataSets().uid(dataSetUid).get().toFlowable()
            .flatMap { dataSet: DataSet ->
                if (dataSet.access().data().write()) return@flatMap d2.categoryModule()
                    .categoryOptionCombos().withCategoryOptions()
                    .byCategoryComboUid().eq(dataSet.categoryCombo()!!.uid()).get().toFlowable()
                    .map { categoryOptionCombos: List<CategoryOptionCombo> ->
                        var canWriteCatOption = false
                        for (categoryOptionCombo in categoryOptionCombos) {
                            for (categoryOption in categoryOptionCombo.categoryOptions()!!) if (categoryOption.access().data().write()) {
                                canWriteCatOption = true
                                break
                            }
                        }
                        var canWriteOrgUnit = false
                        if (canWriteCatOption) {
                            val organisationUnits =
                                d2.organisationUnitModule().organisationUnits()
                                    .byDataSetUids(listOf(dataSetUid))
                                    .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                                    .blockingGet()
                            canWriteOrgUnit = !organisationUnits.isEmpty()
                        }
                        canWriteCatOption && canWriteOrgUnit
                    } else return@flatMap Flowable.just(false)
            }
    }

    override fun getCatOptionComboFrom(
        catComboUid: String?,
        catOptionsList: List<List<CategoryOption>>
    ): List<CategoryOptionCombo> {
        val catOptionCombos: MutableList<CategoryOptionCombo> =
            ArrayList()
        catOptionsList.forEach { catOptions ->
            catOptionCombos.addAll(
                d2.categoryModule().categoryOptionCombos()
                    .byCategoryOptions(UidsHelper.getUidsList(catOptions))
                    .byCategoryComboUid().eq(catComboUid)
                    .blockingGet()
            )
        }
        return catOptionCombos
    }
}
