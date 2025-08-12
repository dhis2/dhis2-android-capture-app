package org.dhis2.mobile.commons.input

import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.LegendData
import org.hisp.dhis.mobile.ui.designsystem.component.SupportingTextData

data class InputUiState(
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
    val focused: Boolean = false,
    val editable: Boolean,
) {
    fun dateExtras() = inputExtra as InputExtra.Date
    fun fileExtras() = inputExtra as InputExtra.File
    fun coordinateExtras() = inputExtra as InputExtra.Coordinate
    fun ageExtras() = inputExtra as InputExtra.Age
    fun multiTextExtras() = inputExtra as InputExtra.MultiText
    fun optionSetExtras() = inputExtra as InputExtra.OptionSet

    fun scanCodeExtras() = inputExtra as InputExtra.ScanCode
}
