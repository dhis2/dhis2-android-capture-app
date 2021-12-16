package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import io.reactivex.Flowable
import io.reactivex.Single
import java.util.SortedMap
import org.dhis2.data.tuples.Pair
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel
import org.hisp.dhis.android.core.category.Category
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.dataelement.DataElementOperand
import org.hisp.dhis.android.core.dataset.DataInputPeriod
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.dataset.Section
import org.hisp.dhis.android.core.period.Period

interface DataValueRepository {
    fun getDataSet(): Flowable<DataSet>
    fun getCatCombo(section: String): Flowable<List<CategoryCombo>>
    fun getCatOptions(
        section: String,
        catCombo: String
    ): Flowable<Map<String, List<MutableList<Pair<CategoryOption, Category>>>>>

    fun getDataValues(
        orgUnitUid: String?,
        periodType: String?,
        initPeriodType: String?,
        catOptionComb: String?,
        section: String
    ): Flowable<List<DataSetTableModel>>

    fun getCompulsoryDataElements(): Flowable<List<DataElementOperand>>
    fun getGreyFields(section: String): Flowable<List<DataElementOperand>>
    fun getSectionByDataSet(section: String): Flowable<Section>
    fun getPeriod(periodId: String): Flowable<Period>
    fun getDataInputPeriod(): Flowable<List<DataInputPeriod>>
    fun completeDataSet(
        orgUnitUid: String,
        periodInitialDate: String,
        catCombo: String
    ): Flowable<Boolean>

    fun reopenDataSet(
        orgUnitUid: String,
        periodInitialDate: String,
        catCombo: String
    ): Flowable<Boolean>

    fun isCompleted(
        orgUnitUid: String,
        periodInitialDate: String,
        catCombo: String
    ): Flowable<Boolean>

    fun isApproval(
        orgUnit: String,
        period: String,
        attributeOptionCombo: String
    ): Flowable<Boolean>

    fun getDataElements(
        categoryCombo: CategoryCombo,
        section: String
    ): Flowable<List<DataElement>>

    fun getCatOptionFromUid(catOption: String): CategoryOption
    fun getCatOptionFromCatOptionCombo(
        categoryOptionCombo: CategoryOptionCombo
    ): List<CategoryOption>
    fun canWriteAny(): Flowable<Boolean>
    fun getCatOptionComboFrom(
        catComboUid: String?,
        catOptions: List<List<CategoryOption>>?
    ): List<CategoryOptionCombo>

    fun getDataSetIndicators(
        orgUnitUid: String,
        periodUid: String,
        attributeOptionCombo: String,
        sectionName: String
    ): Single<SortedMap<String?, String>>
}
