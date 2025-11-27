package org.dhis2.mobile.aggregates.model

import org.dhis2.mobile.commons.input.InputType

internal data class CellInfo(
    val label: String,
    val value: String?,
    val displayValue: String?,
    val inputType: InputType,
    val inputExtra: CellValueExtra?,
    val supportingText: List<String>,
    val errors: List<String>,
    val warnings: List<String>,
    val legendColor: String?,
    val legendLabel: String?,
    val isRequired: Boolean,
)

internal sealed class CellValueExtra {
    data class Coordinates(
        val lat: Double,
        val lon: Double,
    ) : CellValueExtra()

    data class Options(
        val optionCount: Int,
        val options: List<OptionData>,
        val optionsFetched: Boolean,
    ) : CellValueExtra()

    data class FileResource(
        val filePath: String?,
        val fileWeight: String?,
    ) : CellValueExtra()
}
