package org.dhis2.mobile.aggregates.domain

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.data.OptionRepository
import org.dhis2.mobile.aggregates.model.CellInfo
import org.dhis2.mobile.aggregates.model.InputType
import org.dhis2.mobile.aggregates.ui.inputs.TableId
import org.dhis2.mobile.aggregates.ui.states.InputExtra
import org.dhis2.mobile.commons.extensions.getFormattedFileSize
import org.dhis2.mobile.commons.extensions.userFriendlyValue
import org.hisp.dhis.mobile.ui.designsystem.component.CheckBoxData
import org.hisp.dhis.mobile.ui.designsystem.component.Coordinates
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates
import org.hisp.dhis.mobile.ui.designsystem.component.model.DateTimeTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.model.DateTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.model.TimeTransformation

internal class GetDataValueInput(
    private val dataSetUid: String,
    private val periodId: String,
    private val orgUnitUid: String,
    private val attrOptionComboUid: String,
    private val repository: DataSetInstanceRepository,
    private val optionRepository: OptionRepository,
) : ValueValidator(repository) {
    suspend operator fun invoke(
        rowIds: List<TableId>,
        columnIds: List<TableId>,
        fetchOptions: Boolean = false,
    ): CellInfo = supervisorScope {
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

        val legendColorAndLabel = repository.getLegend(
            dataElementUid = dataElementUid,
            periodId = periodId,
            orgUnitUid = orgUnitUid,
            categoryOptionComboUid = categoryOptionComboUid,
            attrOptionComboUid = attrOptionComboUid,
        )

        CellInfo(
            label = dataElementInfo.label,
            value = value,
            displayValue = value?.userFriendlyValue(dataElementUid),
            inputType = dataElementInfo.inputType,
            inputExtra = when (dataElementInfo.inputType) {
                InputType.Age -> InputExtra.Age(
                    selectableDates = SelectableDates("01011940", "12312300"),
                )

                InputType.Date, InputType.Time, InputType.DateTime ->
                    InputExtra.Date(
                        allowManualInput = true,
                        is24HourFormat = true,
                        visualTransformation = when (dataElementInfo.inputType) {
                            InputType.Date -> DateTransformation()
                            InputType.DateTime -> DateTimeTransformation()
                            InputType.Time -> TimeTransformation()
                            else -> throw IllegalArgumentException("Invalid input type")
                        },
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

                InputType.MultiText -> InputExtra.MultiText(
                    numberOfOptions = optionRepository.optionCount(dataElementUid),
                    options = if (fetchOptions) {
                        optionRepository.options(dataElementUid).map { optionData ->
                            async {
                                CheckBoxData(
                                    uid = optionData.code ?: optionData.uid,
                                    checked = optionData.code?.let {
                                        value?.contains(it) == true
                                    } ?: false,
                                    enabled = true,
                                    textInput = optionData.label,
                                )
                            }
                        }.awaitAll()
                    } else {
                        emptyList()
                    },
                    optionsFetched = fetchOptions,
                )

                else -> InputExtra.None
            },
            supportingText = dataElementInfo.description?.let { listOf(it) } ?: emptyList(),
            errors = conflicts.first,
            warnings = conflicts.second,
            isRequired = dataElementInfo.isRequired,
            legendColor = legendColorAndLabel?.first,
            legendLabel = legendColorAndLabel?.second,
        )
    }
}
