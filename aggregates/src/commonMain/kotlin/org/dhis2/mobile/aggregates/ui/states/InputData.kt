package org.dhis2.mobile.aggregates.ui.states

import androidx.compose.runtime.Stable
import org.dhis2.mobile.aggregates.model.InputType
import org.hisp.dhis.mobile.ui.designsystem.component.Coordinates
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.LegendData
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.model.DateTimeVisualTransformation

@Stable
internal data class InputData(
    val id: String,
    val label: String,
    val value: String?,
    val inputType: InputType,
    private val inputExtra: InputExtra,
    val inputShellState: InputShellState,
    val inputStyle: InputStyle = InputStyle.DataInputStyle(),
    val supportingText: List<SupportingTextData>?,
    val legendData: LegendData?,
    val isRequired: Boolean,
) {
    fun dateExtras() = inputExtra as InputExtra.Date
    fun fileExtras() = inputExtra as InputExtra.File
    fun coordinateExtras() = inputExtra as InputExtra.Coordinate
    fun ageExtras() = inputExtra as InputExtra.Age
}

internal sealed class InputExtra() {
    data class Date(
        val allowManualInput: Boolean,
        val is24HourFormat: Boolean,
        val visualTransformation: DateTimeVisualTransformation,
        val selectableDates: SelectableDates,
        val yearRange: IntRange,
    ) : InputExtra()

    data class File(
        val fileWeight: String?,
    ) : InputExtra()

    data class Coordinate(
        val coordinateValue: Coordinates?,
    ) : InputExtra()

    data class Age(
        val selectableDates: SelectableDates,
    ) : InputExtra()

    data object None : InputExtra()
}
