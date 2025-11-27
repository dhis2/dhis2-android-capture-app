package org.dhis2.mobile.commons.input

import org.hisp.dhis.mobile.ui.designsystem.component.CheckBoxData
import org.hisp.dhis.mobile.ui.designsystem.component.Coordinates
import org.hisp.dhis.mobile.ui.designsystem.component.RadioButtonData
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates
import org.hisp.dhis.mobile.ui.designsystem.component.UploadFileState
import org.hisp.dhis.mobile.ui.designsystem.component.model.DateTimeVisualTransformation

sealed class InputExtra {
    data class Date(
        val allowManualInput: Boolean,
        val is24HourFormat: Boolean,
        val visualTransformation: DateTimeVisualTransformation,
        val selectableDates: SelectableDates,
        val yearRange: IntRange,
    ) : InputExtra()

    data class File(
        val fileState: UploadFileState,
        val filePath: String?,
        val fileWeight: String?,
    ) : InputExtra()

    data class Coordinate(
        val coordinateValue: Coordinates?,
    ) : InputExtra()

    data object Age : InputExtra()

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
