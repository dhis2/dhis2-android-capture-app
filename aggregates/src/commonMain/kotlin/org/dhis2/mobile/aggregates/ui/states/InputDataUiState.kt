package org.dhis2.mobile.aggregates.ui.states

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import org.dhis2.mobile.aggregates.model.InputType
import org.dhis2.mobile.aggregates.ui.constants.INPUT_DIALOG_DONE_TAG
import org.dhis2.mobile.aggregates.ui.constants.INPUT_DIALOG_NEXT_TAG
import org.hisp.dhis.mobile.ui.designsystem.component.CheckBoxData
import org.hisp.dhis.mobile.ui.designsystem.component.Coordinates
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.LegendData
import org.hisp.dhis.mobile.ui.designsystem.component.RadioButtonData
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.model.DateTimeVisualTransformation

@Stable
internal data class InputDataUiState(
    val id: String,
    val label: String,
    val value: String?,
    val displayValue: String?,
    val inputType: InputType,
    private val inputExtra: InputExtra,
    val inputShellState: InputShellState,
    val inputStyle: InputStyle = InputStyle.DataInputStyle(),
    val supportingText: List<SupportingTextData>?,
    val legendData: LegendData?,
    val isRequired: Boolean,
    val buttonAction: ButtonAction,
) {
    fun dateExtras() = inputExtra as InputExtra.Date
    fun fileExtras() = inputExtra as InputExtra.File
    fun coordinateExtras() = inputExtra as InputExtra.Coordinate
    fun ageExtras() = inputExtra as InputExtra.Age
    fun multiTextExtras() = inputExtra as InputExtra.MultiText
    fun optionSetExtras() = inputExtra as InputExtra.OptionSet
}

internal sealed class InputExtra {
    data class Date(
        val allowManualInput: Boolean,
        val is24HourFormat: Boolean,
        val visualTransformation: DateTimeVisualTransformation,
        val selectableDates: SelectableDates,
        val yearRange: IntRange,
    ) : InputExtra()

    data class File(
        val filePath: String?,
        val fileWeight: String?,
    ) : InputExtra()

    data class Coordinate(
        val coordinateValue: Coordinates?,
    ) : InputExtra()

    data class Age(
        val selectableDates: SelectableDates,
    ) : InputExtra()

    data class MultiText(
        val numberOfOptions: Int,
        val options: List<CheckBoxData>,
        val optionsFetched: Boolean,
    ) : InputExtra()

    data class OptionSet(
        val numberOfOptions: Int,
        val options: List<RadioButtonData>,
        val optionsFetched: Boolean,
    ) : InputExtra()

    data object None : InputExtra()
}

internal sealed class ButtonAction(
    open val buttonText: String,
    open val icon: ImageVector,
    open val testTag: String,
    open val action: () -> Unit,
) {
    data class Next(
        override val buttonText: String,
        override val icon: ImageVector,
        override val action: () -> Unit,
    ) : ButtonAction(buttonText, icon, INPUT_DIALOG_NEXT_TAG, action)

    data class Done(
        override val buttonText: String,
        override val icon: ImageVector,
        override val action: () -> Unit,
    ) : ButtonAction(buttonText, icon, INPUT_DIALOG_DONE_TAG, action)
}
