package org.dhis2.form.ui.provider.inputfield

import androidx.compose.foundation.background
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.OptionSetConfiguration
import org.dhis2.form.model.UiEventType
import org.hisp.dhis.mobile.ui.designsystem.component.InputDropDown
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProvideDropdownInput(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
) {
    var selectedItem by remember(fieldUiModel) {
        mutableStateOf(fieldUiModel.displayName)
    }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {},
    ) {
        InputDropDown(
            modifier = modifier,
            title = fieldUiModel.label,
            state = fieldUiModel.inputState(),
            selectedItem = selectedItem,
            supportingTextData = fieldUiModel.supportingText(),
            legendData = fieldUiModel.legend(),
            isRequiredField = fieldUiModel.mandatory,
            onResetButtonClicked = { fieldUiModel.onClear() },
            onArrowDropDownButtonClicked = {
                fieldUiModel.onItemClick()
                expanded = !expanded
            },
        )
        if (expanded) {
            when (val optionSetConfig = fieldUiModel.optionSetConfiguration) {
                is OptionSetConfiguration.BigOptionSet -> {
                    fieldUiModel.invokeUiEvent(UiEventType.OPTION_SET)
                    expanded = false
                }
                is OptionSetConfiguration.DefaultOptionSet -> {
                    DropdownMenu(
                        modifier = modifier.exposedDropdownSize(),
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        optionSetConfig.optionsToDisplay().forEach {
                            val isSelected = it.displayName() == fieldUiModel.displayName
                            DropdownMenuItem(
                                modifier = Modifier.background(
                                    when {
                                        isSelected -> SurfaceColor.PrimaryContainer
                                        else -> Color.Transparent
                                    },
                                ),
                                content = {
                                    Text(
                                        text = it.displayName() ?: it.code() ?: "",
                                        color = when {
                                            isSelected -> TextColor.OnPrimaryContainer
                                            else -> TextColor.OnSurface
                                        },
                                    )
                                },
                                onClick = {
                                    expanded = false
                                    selectedItem = it.displayName()
                                    fieldUiModel.onSave(it.code())
                                },
                            )
                        }
                    }
                }
                null -> throw IllegalArgumentException("Unsupported OptionSetConfiguration")
            }
        }
    }
}
