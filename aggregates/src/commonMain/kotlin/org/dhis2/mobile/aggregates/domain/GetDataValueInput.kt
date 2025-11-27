package org.dhis2.mobile.aggregates.domain

import kotlinx.coroutines.supervisorScope
import org.dhis2.mobile.aggregates.data.DataSetInstanceRepository
import org.dhis2.mobile.aggregates.data.OptionRepository
import org.dhis2.mobile.aggregates.model.CellInfo
import org.dhis2.mobile.aggregates.model.CellValueExtra
import org.dhis2.mobile.commons.extensions.getFormattedFileSize
import org.dhis2.mobile.commons.extensions.userFriendlyValue
import org.dhis2.mobile.commons.input.InputType

internal class GetDataValueInput(
    private val dataSetUid: String,
    private val periodId: String,
    private val orgUnitUid: String,
    private val attrOptionComboUid: String,
    private val repository: DataSetInstanceRepository,
    private val optionRepository: OptionRepository,
) : ValueValidator(repository) {
    suspend operator fun invoke(
        dataElementUid: String,
        categoryOptionComboUidData: Pair<String?, List<String>>,
        fetchOptions: Boolean = false,
    ): CellInfo =
        supervisorScope {
            val categoryOptionComboUid =
                categoryOptionComboUidData.first
                    ?: repository.categoryOptionComboFromCategoryOptions(
                        dataSetUid,
                        dataElementUid,
                        categoryOptionComboUidData.second,
                    )

            val dataElementInfo =
                repository.dataElementInfo(
                    dataSetUid = dataSetUid,
                    dataElementUid = dataElementUid,
                    categoryOptionComboUid = categoryOptionComboUid,
                )

            val value =
                repository.value(
                    periodId = periodId,
                    orgUnitUid = orgUnitUid,
                    attrOptionComboUid = attrOptionComboUid,
                    dataElementUid = dataElementUid,
                    categoryOptionComboUid = categoryOptionComboUid,
                )
            val conflicts =
                repository.conflicts(
                    dataSetUid = dataSetUid,
                    periodId = periodId,
                    orgUnitUid = orgUnitUid,
                    attrOptionComboUid = attrOptionComboUid,
                    dataElementUid = dataElementUid,
                    categoryOptionComboUid = categoryOptionComboUid,
                )

            val legendColorAndLabel =
                repository.getLegend(
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
                inputExtra =
                    when (dataElementInfo.inputType) {
                        InputType.Coordinates ->
                            value?.let {
                                val (lat, long) = repository.getCoordinatesFrom(it)
                                CellValueExtra.Coordinates(
                                    lat = lat,
                                    lon = long,
                                )
                            }

                        InputType.MultiText, InputType.OptionSet -> {
                            val optionCount = optionRepository.optionCount(dataElementUid)
                            CellValueExtra.Options(
                                optionCount = optionCount,
                                options =
                                    if (fetchOptions || optionCount < 7) {
                                        optionRepository.options(dataElementUid)
                                    } else {
                                        emptyList()
                                    },
                                optionsFetched = fetchOptions || optionCount < 7,
                            )
                        }
                        InputType.FileResource, InputType.Image -> {
                            value?.let {
                                val filePath = repository.getFilePath(value)
                                CellValueExtra.FileResource(
                                    filePath = filePath,
                                    fileWeight = filePath?.let { getFormattedFileSize(it) },
                                )
                            }
                        }

                        else -> null
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
