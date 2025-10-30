package org.dhis2.mobile.aggregates.ui.states

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import org.dhis2.mobile.aggregates.ui.constants.INPUT_DIALOG_NEXT_TAG
import org.dhis2.mobile.commons.input.InputExtra
import org.dhis2.mobile.commons.input.InputType
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.LegendData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData
import org.hisp.dhis.mobile.ui.designsystem.component.table.ui.TableSelection

internal sealed class CellSelectionState(
    open val tableSelection: TableSelection?,
) {
    @Stable
    internal data class Default(
        override val tableSelection: TableSelection,
    ) : CellSelectionState(tableSelection)

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
        val currentSelectedCell: TableSelection.CellSelection?,
    ) : CellSelectionState(currentSelectedCell) {
        fun dateExtras() = inputExtra as InputExtra.Date

        fun fileExtras() = inputExtra as InputExtra.File

        fun coordinateExtras() = inputExtra as InputExtra.Coordinate

        fun ageExtras() = inputExtra as InputExtra.Age

        fun multiTextExtras() = inputExtra as InputExtra.MultiText

        fun optionSetExtras() = inputExtra as InputExtra.OptionSet
    }
}

internal data class ButtonAction(
    val buttonText: String,
    val icon: ImageVector,
    val testTag: String = INPUT_DIALOG_NEXT_TAG,
    val isDoneAction: Boolean,
)
