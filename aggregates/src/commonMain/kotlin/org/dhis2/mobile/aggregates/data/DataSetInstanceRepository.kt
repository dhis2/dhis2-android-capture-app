package org.dhis2.mobile.aggregates.data

import org.dhis2.mobile.aggregates.model.CellElement
import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetInstanceConfiguration
import org.dhis2.mobile.aggregates.model.DataSetInstanceSectionConfiguration
import org.dhis2.mobile.aggregates.model.DataSetRenderingConfig
import org.dhis2.mobile.aggregates.model.DataSetSection
import org.dhis2.mobile.aggregates.model.TableGroup
import java.util.SortedMap

internal interface DataSetInstanceRepository {
    suspend fun getDataSetInstance(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
    ): DataSetDetails

    suspend fun getDataSetInstanceSections(
        dataSetUid: String,
    ): List<DataSetSection>

    suspend fun getRenderingConfig(
        dataSetUid: String,
    ): DataSetRenderingConfig

    suspend fun dataSetInstanceConfiguration(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
        sectionUid: String,
    ): DataSetInstanceConfiguration

    suspend fun getDataSetInstanceSectionCells(
        dataSetElements: List<CellElement>,
        dataSetUid: String,
        sectionUid: String,
    ): List<TableGroup>

    suspend fun getTableGroupHeaders(categoryUids: List<String>): List<List<String>>
    suspend fun dataSetInstanceSectionConfiguration(sectionUid: String): DataSetInstanceSectionConfiguration?
    suspend fun conflicts(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
        dataElementUid: String,
        categoryOptionComboUid: String,
    ): Pair<List<String>, List<String>>

    suspend fun cellValue(
        periodId: String,
        orgUnitUid: String,
        dataElementUid: String,
        categoryOptionComboUid: String,
        attrOptionComboUid: String,
    ): String?

    suspend fun categoryOptionCombinations(categoryUids: List<String>): List<String>

    suspend fun getDataSetIndicator(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attributeOptionComboUid: String,
        sectionUid: String,
    ): SortedMap<String, String>?
}
