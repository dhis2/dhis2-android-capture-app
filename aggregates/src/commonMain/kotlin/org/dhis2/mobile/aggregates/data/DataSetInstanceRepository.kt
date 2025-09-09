package org.dhis2.mobile.aggregates.data

import org.dhis2.mobile.aggregates.model.CellElement
import org.dhis2.mobile.aggregates.model.DataElementInfo
import org.dhis2.mobile.aggregates.model.DataSetDetails
import org.dhis2.mobile.aggregates.model.DataSetInstanceConfiguration
import org.dhis2.mobile.aggregates.model.DataSetInstanceSectionConfiguration
import org.dhis2.mobile.aggregates.model.DataSetRenderingConfig
import org.dhis2.mobile.aggregates.model.DataSetSection
import org.dhis2.mobile.aggregates.model.TableGroup
import org.dhis2.mobile.aggregates.model.ValidationRulesResult
import java.util.SortedMap

typealias ColorString = String
typealias LegendLabel = String

internal interface DataSetInstanceRepository {
    suspend fun getDataSetInstance(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
    ): DataSetDetails

    suspend fun getDataSetInstanceSections(dataSetUid: String): List<DataSetSection>

    suspend fun getRenderingConfig(dataSetUid: String): DataSetRenderingConfig

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

    suspend fun getInitialSectionToLoad(
        openErrorLocation: Boolean,
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        catOptCombo: String,
    ): Int

    suspend fun dataSetInstanceSectionConfiguration(sectionUid: String): DataSetInstanceSectionConfiguration?

    suspend fun conflicts(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
        dataElementUid: String,
        categoryOptionComboUid: String,
    ): Pair<List<String>, List<String>>

    suspend fun getDataSetIndicator(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attributeOptionComboUid: String,
        sectionUid: String,
    ): SortedMap<String, String>?

    suspend fun values(
        periodId: String,
        orgUnitUid: String,
        dataElementUids: List<String>,
        attrOptionComboUid: String,
        pivotedCategoryUid: String?,
    ): List<Pair<Pair<String, String>, String?>>

    suspend fun dataElementInfo(
        dataSetUid: String,
        dataElementUid: String,
        categoryOptionComboUid: String,
    ): DataElementInfo

    suspend fun value(
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
        dataElementUid: String,
        categoryOptionComboUid: String,
    ): String?

    suspend fun updateValue(
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
        dataElementUid: String,
        categoryOptionComboUid: String,
        value: String?,
    ): Result<Unit>

    suspend fun categoryOptionComboFromCategoryOptions(
        dataSetUid: String,
        dataElementUid: String,
        categoryOptions: List<String>,
    ): String

    suspend fun getCoordinatesFrom(coordinatesValue: String): Pair<Double, Double>

    suspend fun checkIfHasValidationRules(dataSetUid: String): Boolean

    suspend fun areValidationRulesMandatory(dataSetUid: String): Boolean

    suspend fun isComplete(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
    ): Boolean

    suspend fun isEditable(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
    ): Boolean

    suspend fun checkIfHasMissingMandatoryFields(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attributeOptionComboUid: String,
    ): Boolean

    suspend fun checkIfHasMissingMandatoryFieldsCombination(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attributeOptionComboUid: String,
    ): Boolean

    suspend fun completeDataset(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attributeOptionComboUid: String,
    ): Result<Unit>

    suspend fun reopenDataSet(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attributeOptionComboUid: String,
    )

    suspend fun runValidationRules(
        dataSetUid: String,
        periodId: String,
        orgUnitUid: String,
        attrOptionComboUid: String,
    ): ValidationRulesResult

    suspend fun getLegend(
        dataElementUid: String,
        periodId: String,
        orgUnitUid: String,
        categoryOptionComboUid: String,
        attrOptionComboUid: String,
    ): Pair<ColorString?, LegendLabel?>?

    suspend fun uploadFile(
        path: String,
        isImage: Boolean,
    ): Result<String?>

    suspend fun getFilePath(fileUid: String): String?
}
