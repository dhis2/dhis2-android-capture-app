package org.dhis2.usescases.settings.ui

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.dhis2.R
import org.dhis2.usescases.settings.SettingItem
import org.dhis2.usescases.settings.models.ReservedValueSettingsViewModel
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.InputPositiveIntegerOrZero
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState

@Composable
internal fun ReservedValuesSettingItem(
    reservedValuesSettings: ReservedValueSettingsViewModel,
    isOpened: Boolean,
    onClick: () -> Unit,
    onReservedValuesToDownloadUpdate: (Int) -> Unit,
    onManageReservedValuesClick: () -> Unit,
) {
    SettingItem(
        modifier = Modifier.testTag(SettingItem.RESERVED_VALUES.name),
        title = stringResource(id = R.string.settingsReservedValues),
        subtitle = stringResource(id = R.string.settingsReservedValues_descr),
        icon = Icons.AutoMirrored.Outlined.List,
        extraActions = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = spacedBy(8.dp),
            ) {
                if (reservedValuesSettings.canBeEdited) {
                    var inputReservedValuesState =
                        remember {
                            InputShellState.UNFOCUSED
                        }
                    var value =
                        remember {
                            TextFieldValue(text = reservedValuesSettings.numberOfReservedValuesToDownload.toString())
                        }
                    InputPositiveIntegerOrZero(
                        title = stringResource(R.string.reserved_values_hint),
                        state = inputReservedValuesState,
                        inputTextFieldValue = value,
                        onValueChanged = { fieldValue ->
                            value = fieldValue ?: TextFieldValue()
                            onReservedValuesToDownloadUpdate(
                                fieldValue?.text?.toIntOrNull() ?: 0,
                            )
                        },
                        imeAction = ImeAction.Done,
                        onFocusChanged = { isFocused ->
                            inputReservedValuesState =
                                if (isFocused) {
                                    InputShellState.FOCUSED
                                } else {
                                    InputShellState.UNFOCUSED
                                }
                        },
                        showDeleteButton = false,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.rv_no_editable),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.manage_reserved_values_button),
                    style = ButtonStyle.TONAL,
                    enabled = true,
                    onClick = onManageReservedValuesClick,
                )
            }
        },
        showExtraActions = isOpened,
        onClick = onClick,
    )
}
