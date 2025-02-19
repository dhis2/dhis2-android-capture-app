package org.dhis2.mobile.aggregates.domain

import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.model.CellInfo
import org.dhis2.mobile.aggregates.model.InputType
import org.dhis2.mobile.aggregates.ui.inputs.TableId
import org.dhis2.mobile.aggregates.ui.inputs.TableIdType
import org.dhis2.mobile.aggregates.ui.states.InputExtra
import org.dhis2.mobile.commons.extensions.getFormattedFileSize
import org.hisp.dhis.mobile.ui.designsystem.component.Coordinates
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates
import org.hisp.dhis.mobile.ui.designsystem.component.model.DateTimeTransformation

internal class GetDataValueInput(
    private val dataSetUid: String,
    private val periodId: String,
    private val orgUnitUid: String,
    private val attrOptionComboUid: String,
    private val repository: DataSetInstanceRepository,
) {
    suspend operator fun invoke(
        rowIds: List<TableId>,
        columnIds: List<TableId>,
    ): CellInfo {
        val dataElementUid = checkOnlyOneDataElementIsProvided(rowIds, columnIds)
        val categoryOptionComboUid = checkedCategoryOptionCombos(rowIds, columnIds)

        val dataElementInfo = repository.dataElementInfo(
            dataSetUid = dataSetUid,
            dataElementUid = dataElementUid,
            categoryOptionComboUid = categoryOptionComboUid,
        )

        val value = repository.value(
            periodId = periodId,
            orgUnitUid = orgUnitUid,
            attrOptionComboUid = attrOptionComboUid,
            dataElementUid = dataElementUid,
            categoryOptionComboUid = categoryOptionComboUid,
        )
        val conflicts = repository.conflicts(
            dataSetUid = dataSetUid,
            periodId = periodId,
            orgUnitUid = orgUnitUid,
            attrOptionComboUid = attrOptionComboUid,
            dataElementUid = dataElementUid,
            categoryOptionComboUid = categoryOptionComboUid,

        )

        return CellInfo(
            label = dataElementInfo.label,
            value = value,
            inputType = dataElementInfo.inputType,
            inputExtra = when (dataElementInfo.inputType) {
                InputType.Age -> InputExtra.Age(
                    selectableDates = SelectableDates("01011940", "12312300"),
                )

                InputType.Date, InputType.Time, InputType.DateTime ->
                    InputExtra.Date(
                        allowManualInput = true,
                        is24HourFormat = true,
                        visualTransformation = DateTimeTransformation(),
                        selectableDates = SelectableDates("01011940", "12312300"),
                        yearRange = IntRange(1940, 2300),
                    )

                InputType.Coordinates -> InputExtra.Coordinate(
                    coordinateValue = value?.let {
                        val (lat, long) = repository.getCoordinatesFrom(it)
                        Coordinates(
                            latitude = lat,
                            longitude = long,
                        )
                    },
                )

                InputType.FileResource -> InputExtra.File(
                    fileWeight = value?.let { getFormattedFileSize(value) },
                )

                else -> InputExtra.None
            },
            supportingText = dataElementInfo.description?.let { listOf(it) } ?: emptyList(),
            errors = conflicts.first,
            warnings = conflicts.second,
            isRequired = dataElementInfo.isRequired,
        )
    }

    private fun checkOnlyOneDataElementIsProvided(
        rowIds: List<TableId>,
        columnIds: List<TableId>,
    ): String {
        val dataElementUids = rowIds.filter { it.type is TableIdType.DataElement }.map { it.id } +
            columnIds.filter { it.type is TableIdType.DataElement }.map { it.id }

        if (dataElementUids.size != 1) throw IllegalStateException("Only one data element can be provided")

        return dataElementUids.first()
    }

    private suspend fun checkedCategoryOptionCombos(
        rowIds: List<TableId>,
        columnIds: List<TableId>,
    ): String {
        val categoryOptions =
            rowIds.filter { it.type is TableIdType.CategoryOption }.map { it.id } +
                columnIds.filter { it.type is TableIdType.CategoryOption }.map { it.id }
        val categoryOptionCombos =
            rowIds.filter { it.type is TableIdType.CategoryOptionCombo }.map { it.id } +
                columnIds.filter { it.type is TableIdType.CategoryOptionCombo }.map { it.id }

        if (categoryOptions.isNotEmpty() && categoryOptionCombos.isNotEmpty()) {
            throw IllegalStateException(
                "Category options and category option combos cannot be provided at the same time",
            )
        }
        if (categoryOptionCombos.size > 1) throw IllegalStateException("Only one category option combo can be provided")

        return when {
            categoryOptionCombos.isNotEmpty() -> categoryOptionCombos.first()
            else -> repository.categoryOptionComboFromCategoryOptions(categoryOptions)
        }
    }
}
