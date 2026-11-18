package org.dhis2.mobile.login.pin.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.dhis2.mobile.login.resources.Res
import org.dhis2.mobile.login.resources.manage_pin
import org.dhis2.mobile.login.resources.manage_pin_description
import org.dhis2.mobile.login.resources.remove_pin
import org.dhis2.mobile.login.resources.update_pin
import org.hisp.dhis.mobile.ui.designsystem.component.BottomSheetShell
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonState
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.ColorStyle
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellUIState
import org.jetbrains.compose.resources.stringResource

@Composable
fun ManagePinDialog(
    onUpdateClick: () -> Unit,
    onRemoveClick: () -> Unit,
    onDismiss: () -> Unit,
) {
    BottomSheetShell(
        icon = {
            Icon(
                imageVector = Icons.Outlined.Pin,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        uiState =
            BottomSheetShellUIState(
                title = stringResource(Res.string.manage_pin),
                description = stringResource(Res.string.manage_pin_description),
                headerTextAlignment = TextAlign.Center,
                showTopSectionDivider = false,
                showBottomSectionDivider = false,
            ),
        buttonBlock = {
            VerticalButtonBlock(
                primaryButton = {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        state =
                            ButtonState(
                                text = stringResource(Res.string.update_pin),
                                style = ButtonStyle.TONAL,
                                colorStyle = ColorStyle.DEFAULT,
                            ),
                        onClick = onUpdateClick,
                    )
                },
                secondaryButton = {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        state =
                            ButtonState(
                                text = stringResource(Res.string.remove_pin),
                                style = ButtonStyle.OUTLINED,
                                colorStyle = ColorStyle.ERROR,
                            ),
                        onClick = onRemoveClick,
                    )
                },
            )
        },
        content = null,
        onDismiss = onDismiss,
    )
}
