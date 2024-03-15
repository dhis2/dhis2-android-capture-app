package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownInputField
import org.hisp.dhis.mobile.ui.designsystem.component.DropdownItem
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle

@Composable
fun ProvidePeriodSelector(
    modifier: Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
    focusRequester: FocusRequester,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
) {
    var selectedItem by remember(fieldUiModel.displayName) {
        mutableStateOf(
            fieldUiModel.displayName,
        )
    }

    DropdownInputField(
        modifier = modifier,
        title = fieldUiModel.label,
        state = fieldUiModel.inputState(),
        inputStyle = inputStyle,
        legendData = fieldUiModel.legend(),
        supportingTextData = fieldUiModel.supportingText(),
        isRequiredField = fieldUiModel.mandatory,
        selectedItem = DropdownItem(selectedItem ?: ""),
        onResetButtonClicked = {
            selectedItem = null
            fieldUiModel.onClear()
        },
        onDropdownIconClick = {
            uiEventHandler(
                RecyclerViewUiEvents.SelectPeriod(
                    uid = fieldUiModel.uid,
                    title = fieldUiModel.label,
                    periodType = fieldUiModel.periodSelector!!.type,
                    minDate = fieldUiModel.periodSelector!!.minDate,
                    maxDate = fieldUiModel.periodSelector!!.maxDate,
                ),
            )
        },
        onFocusChanged = {},
        focusRequester = focusRequester,
        expanded = false,
    )
}
