package org.dhis2.mobile.aggregates.data

import org.dhis2.mobile.aggregates.model.CellElement
import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetInstanceConfiguration
import org.dhis2.mobile.aggregates.model.DataSetInstanceSectionConfiguration
import org.dhis2.mobile.aggregates.model.DataSetRenderingConfig
import org.dhis2.mobile.aggregates.model.DataSetSection
import org.dhis2.mobile.aggregates.model.TableGroup

internal interface DataSetInstanceRepository {
    fun getDataSetInstance(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
    ): DataSetDetails

    fun getDataSetInstanceSections(
        dataSetUid: String,
    ): List<DataSetSection>

    fun getRenderingConfig(
        dataSetUid: String,
    ): DataSetRenderingConfig

    fun dataSetInstanceConfiguration(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
        sectionUid: String,
    ): DataSetInstanceConfiguration

    fun getDataSetInstanceSectionCells(
        dataSetElements: List<CellElement>,
        dataSetUid: String,
        sectionUid: String,
    ): List<TableGroup>

    fun getTableGroupHeaders(categoryUids: List<String>): List<List<String>>
    fun dataSetInstanceSectionConfiguration(sectionUid: String): DataSetInstanceSectionConfiguration?
    fun conflicts(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
        dataElementUid: String,
        categoryOptionComboUid: String,
    ): Pair<List<String>, List<String>>

    fun cellValue(
        periodId: String,
        orgUnitUid: String,
        dataElementUid: String,
        categoryOptionComboUid: String,
        attrOptionComboUid: String,
    ): String?

    fun categoryOptionCombinations(categoryUids: List<String>): List<String>
}
